package com.bmstu.iu3.automanagement.race

import com.bmstu.iu3.automanagement.models.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.CopyOnWriteArrayList

class ClassicRaceEngine(
    private val clock: RaceClock,
    private val tacticResolver: TacticResolver,
    private val pitStopManager: PitStopManager,
    private val eventSink: RaceEventSink
) {
    private var raceJob: Job? = null
    private val running = AtomicBoolean(false)

    fun startRace(
        track: Track,
        players: List<String>,
        pilotSkillsByName: Map<String, Int> = emptyMap(),
        carPerformanceByName: Map<String, Double> = emptyMap(),
        onCommentary: (CommentatorMessage) -> Unit = {},
        onFinished: (ClassicRaceOutcome) -> Unit = {}
    ) {
        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        running.set(false)
        raceJob = scope.launch {
            val outcome = runRaceInternal(
                track, players, 250L,
                pilotSkillsByName, carPerformanceByName,
                onCommentary
            )
            withContext(Dispatchers.Main) { onFinished(outcome) }
        }
    }

    private suspend fun runRaceInternal(
        track: Track,
        players: List<String>,
        tickDelayMs: Long,
        pilotSkillsByName: Map<String, Int>,
        carPerformanceByName: Map<String, Double>,
        onCommentary: (CommentatorMessage) -> Unit
    ): ClassicRaceOutcome {
        if (!running.compareAndSet(false, true)) return ClassicRaceOutcome("busy", emptyList())

        clock.start()
        val totalTicks = 40
        val sessionId = UUID.randomUUID().toString()
        val deltaChannel = Channel<RaceDelta>(Channel.UNLIMITED)
        val commentaryChannel = Channel<RaceLogEntry>(Channel.UNLIMITED)
        
        val progressMap = ConcurrentHashMap<String, Double>()
        val participants = ConcurrentHashMap<String, RaceParticipant>()
        val retired = ConcurrentHashMap.newKeySet<String>()
        val finishedIds = ConcurrentHashMap.newKeySet<String>()
        val pilotFines = ConcurrentHashMap<String, Double>()
        val participantIncidents = ConcurrentHashMap<String, Incident>()
        val commentaryMessages = CopyOnWriteArrayList<CommentatorMessage>()
        
        var lastStandingsOrder = listOf<String>()

        val commentatorWorker = CommentatorWorker(commentaryChannel, mutableListOf()) { msg ->
            commentaryMessages.add(msg)
            onCommentary(msg)
        }

        commentaryChannel.trySend(RaceLogEntry(clock.nowMs(), "Race", message = "STATUS_RACE_START|${track.getName()}"))

        supervisorScope {
            launch { commentatorWorker.start() }

            players.forEach { name ->
                val pId = if (name == "YOU") "player-car" else "car-${UUID.randomUUID().toString().take(4)}"
                val participant = RaceParticipant(
                    pId, name, 
                    calculateBasePace(carPerformanceByName[name] ?: 50.0, pilotSkillsByName[name] ?: 50, track.getStraightsRatio(), 0.1),
                    calculateVariance(carPerformanceByName[name] ?: 50.0, pilotSkillsByName[name] ?: 50)
                )
                participants[pId] = participant
                progressMap[pId] = 0.0
                launch { 
                    CarWorker(participant, totalTicks, tickDelayMs, deltaChannel, 0.0, pitStopManager, 
                        isRaceRunning = { running.get() && !retired.contains(pId) }
                    ).start() 
                }
            }

            launch { WeatherWorker(totalTicks, tickDelayMs, deltaChannel, { running.get() }).start() }
            launch { 
                IncidentsWorker(
                    participantIds = participants.keys.toList(),
                    totalTicks = totalTicks,
                    tickDelayMs = tickDelayMs,
                    eventChannel = deltaChannel,
                    pilotSkillByParticipantId = participants.mapValues { pilotSkillsByName[it.value.displayName] ?: 50 },
                    track = track,
                    isRetired = { retired.contains(it) },
                    isRaceRunning = { running.get() }
                ).start() 
            }

            while (running.get() && finishedIds.size < players.size) {
                val delta = try { withTimeout(5000) { deltaChannel.receive() } } catch (e: Exception) { break }
                when (delta) {
                    is RaceDelta.Progress -> {
                        if (retired.contains(delta.participantId)) continue
                        progressMap[delta.participantId] = delta.progress
                        
                        val currentOrder = progressMap.entries
                            .filter { !retired.contains(it.key) }
                            .sortedByDescending { it.value }
                            .map { it.key }
                        
                        if (lastStandingsOrder.isNotEmpty() && currentOrder != lastStandingsOrder) {
                            for (i in 0 until minOf(currentOrder.size, lastStandingsOrder.size)) {
                                if (currentOrder[i] != lastStandingsOrder[i]) {
                                    val overName = participants[currentOrder[i]]?.displayName ?: "A driver"
                                    val underName = participants[lastStandingsOrder[i]]?.displayName ?: "someone"
                                    commentaryChannel.trySend(RaceLogEntry(clock.nowMs(), "Race", message = "STATUS_OVERTAKE|$overName|$underName"))
                                    break 
                                }
                            }
                        }
                        lastStandingsOrder = currentOrder
                    }
                    is RaceDelta.IncidentPenalty -> {
                        if (retired.contains(delta.participantId)) continue
                        val name = participants[delta.participantId]?.displayName ?: "Unknown"
                        
                        val incident = Incident().apply {
                            setReason(delta.reason)
                            setSeverity(if (delta.isTerminal) "Terminal" else "Minor")
                            setFineAmount(delta.fineAmount)
                        }
                        participantIncidents[delta.participantId] = incident

                        if (delta.isTerminal) {
                            retired.add(delta.participantId)
                            finishedIds.add(delta.participantId)
                            progressMap[delta.participantId] = -1.0 
                            commentaryChannel.trySend(RaceLogEntry(clock.nowMs(), "Incident", message = "STATUS_DNF|$name|${delta.reason}"))
                        } else {
                            if (delta.reason == "Speeding Fine") {
                                pilotFines[name] = delta.fineAmount
                                commentaryChannel.trySend(RaceLogEntry(clock.nowMs(), "Incident", message = "STATUS_FINE|$name|${String.format(Locale.US, "%.0f", delta.fineAmount)}"))
                            } else {
                                commentaryChannel.trySend(RaceLogEntry(clock.nowMs(), "Incident", message = "STATUS_ISSUE|$name|${delta.reason}"))
                            }
                        }
                    }
                    is RaceDelta.Finished -> { finishedIds.add(delta.participantId) }
                    is RaceDelta.WeatherChanged -> {
                        commentaryChannel.trySend(RaceLogEntry(clock.nowMs(), "Weather", message = "STATUS_WEATHER|${delta.weatherCode}"))
                    }
                    is RaceDelta.WorkerMessage -> {
                        val name = participants[delta.participantId]?.displayName ?: "Driver"
                        commentaryChannel.trySend(RaceLogEntry(clock.nowMs(), "Car:${delta.participantId}", message = "STATUS_WORKER|$name|${delta.message}"))
                    }
                }
            }
            
            running.set(false)
            clock.stop()
            commentaryChannel.close()
        }

        val finalSortedIds = participants.keys.sortedWith(
            compareByDescending<String> { !retired.contains(it) }.thenByDescending { progressMap[it] ?: 0.0 }
        )

        val finalStandings = finalSortedIds.mapIndexed { index, pId ->
            ClassicRaceStanding(pId, participants[pId]?.displayName ?: "", progressMap[pId] ?: 0.0, index + 1, participantIncidents[pId])
        }
        return ClassicRaceOutcome(sessionId, finalStandings, commentaryMessages.toList(), pilotFines.toMap())
    }

    private fun calculateBasePace(p: Double, s: Int, str: Double, w: Double): Double = 
        ((4.0 + p/15.0 + s/10.0) * (1.0 + str*0.2) * (1.0 - w*0.2)).coerceAtLeast(1.0)

    private fun calculateVariance(p: Double, s: Int): Double = (2.0 / (p/100.0 + s/40.0)).coerceIn(0.5, 2.5)
}
