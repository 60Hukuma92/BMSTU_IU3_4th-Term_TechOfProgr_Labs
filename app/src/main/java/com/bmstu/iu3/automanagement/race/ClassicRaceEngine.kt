package com.bmstu.iu3.automanagement.race

import com.bmstu.iu3.automanagement.models.Tactic
import com.bmstu.iu3.automanagement.models.RaceLogEntry
import com.bmstu.iu3.automanagement.models.Severity
import com.bmstu.iu3.automanagement.models.CommentatorMessage
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlin.math.max
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference

class ClassicRaceEngine(
    private val clock: RaceClock,
    private val tacticResolver: TacticResolver,
    private val pitStopManager: PitStopManager,
    private val eventSink: RaceEventSink
) {
    private var raceScope: CoroutineScope? = null
    private var raceJob: Job? = null
    private val running = AtomicBoolean(false)

    fun startRace(
        trackId: String,
        players: List<String>,
        tacticId: String?,
        pilotSkillsByName: Map<String, Int> = emptyMap(),
        carPerformanceByName: Map<String, Double> = emptyMap(),
        trackDifficulty: Double = 0.5,
        initialWeatherSeverity: Double = 0.2,
        onFinished: (ClassicRaceOutcome) -> Unit = {}
    ) {
        val scope = CoroutineScope(Dispatchers.Default + Job())
        raceScope = scope
        raceJob = scope.launch(start = CoroutineStart.DEFAULT) {
            val outcome = runRaceInternal(
                trackId = trackId,
                players = players,
                tacticId = tacticId,
                tickDelayMs = 220L,
                pilotSkillsByName = pilotSkillsByName,
                carPerformanceByName = carPerformanceByName,
                trackDifficulty = trackDifficulty,
                initialWeatherSeverity = initialWeatherSeverity
            )
            onFinished(outcome)
        }
    }

    fun runRaceBlocking(
        trackId: String,
        players: List<String>,
        tacticId: String?,
        pilotSkillsByName: Map<String, Int> = emptyMap(),
        carPerformanceByName: Map<String, Double> = emptyMap(),
        trackDifficulty: Double = 0.5,
        initialWeatherSeverity: Double = 0.2
    ): ClassicRaceOutcome {
        return runBlocking {
            runRaceInternal(
                trackId = trackId,
                players = players,
                tacticId = tacticId,
                tickDelayMs = 0L,
                pilotSkillsByName = pilotSkillsByName,
                carPerformanceByName = carPerformanceByName,
                trackDifficulty = trackDifficulty,
                initialWeatherSeverity = initialWeatherSeverity
            )
        }
    }

    private suspend fun runRaceInternal(
        trackId: String,
        players: List<String>,
        tacticId: String?,
        tickDelayMs: Long,
        pilotSkillsByName: Map<String, Int>,
        carPerformanceByName: Map<String, Double>,
        trackDifficulty: Double,
        initialWeatherSeverity: Double
    ): ClassicRaceOutcome {
        if (players.isEmpty()) {
            eventSink.publish(
                RaceLogEntry(
                    timestampMs = 0L,
                    source = "Race",
                    severity = Severity.WARNING,
                    message = "Race start rejected: no participants"
                )
            )
            return ClassicRaceOutcome(sessionId = "invalid", standings = emptyList())
        }
        if (!running.compareAndSet(false, true)) {
            eventSink.publish(
                RaceLogEntry(
                    timestampMs = clock.nowMs(),
                    source = "Race",
                    severity = Severity.WARNING,
                    message = "Race start ignored: race is already running"
                )
            )
            return ClassicRaceOutcome(sessionId = "busy", standings = emptyList())
        }

        clock.start()
        val sessionId = UUID.randomUUID().toString()
        val deltaChannel = Channel<RaceDelta>(capacity = Channel.UNLIMITED)
        val commentaryChannel = Channel<RaceLogEntry>(capacity = Channel.UNLIMITED)
        val progressByParticipant = linkedMapOf<String, Double>()
        val rawProgressByParticipant = linkedMapOf<String, Double>()
        val participantsById = linkedMapOf<String, RaceParticipant>()
        val pilotSkillByParticipantId = linkedMapOf<String, Int>()
        val retiredParticipants = mutableSetOf<String>()
        var weatherMultiplier = 1.0
        val commentaryMessages = CopyOnWriteArrayList<CommentatorMessage>()
        var finalOutcome: ClassicRaceOutcome? = null
        val weatherSeverityRef = AtomicReference(initialWeatherSeverity.coerceIn(0.0, 1.0))

        val syntheticTactic = tacticId?.let {
            Tactic(
                id = it,
                name = it,
                weatherModifiers = mapOf("DEFAULT" to 0.03)
            )
        }
        val tacticBoost = tacticResolver.resolve(syntheticTactic, "DEFAULT", null)

        val commentatorWorker = CommentatorWorker(
            sourceEvents = commentaryChannel,
            outputMessages = commentaryMessages
        )

        eventSink.publish(
            RaceLogEntry(
                timestampMs = clock.nowMs(),
                source = "Race",
                message = "Race started: track=$trackId participants=${players.size}"
            )
        )

        commentaryChannel.trySend(
            RaceLogEntry(
                timestampMs = clock.nowMs(),
                source = "Race",
                message = "Race started: track=$trackId participants=${players.size}"
            )
        )

        supervisorScope {
            val commentatorJob = launch { commentatorWorker.start() }

            players.forEachIndexed { index, name ->
                val carPerformance = (carPerformanceByName[name] ?: 0.0).coerceAtLeast(0.0)
                val pilotSkill = (pilotSkillsByName[name] ?: 50).coerceIn(1, 100)
                val participant = RaceParticipant(
                    id = "car-${index + 1}",
                    displayName = name,
                    basePace = calculateBasePace(carPerformance, pilotSkill, trackDifficulty, initialWeatherSeverity),
                    variance = calculateVariance(carPerformance, pilotSkill)
                )
                participantsById[participant.id] = participant
                pilotSkillByParticipantId[participant.id] = pilotSkill
                progressByParticipant[participant.id] = 0.0
                rawProgressByParticipant[participant.id] = 0.0
                val worker = CarWorker(
                    participant = participant,
                    totalTicks = 16,
                    tickDelayMs = tickDelayMs,
                    eventChannel = deltaChannel,
                    tacticBoost = tacticBoost,
                    pitStopManager = pitStopManager,
                    isRaceRunning = { running.get() }
                )
                launch { worker.start() }
            }

            val weatherWorker = WeatherWorker(
                totalTicks = 16,
                tickDelayMs = tickDelayMs,
                eventChannel = deltaChannel,
                isRaceRunning = { running.get() }
            )
            val incidentsWorker = IncidentsWorker(
                participantIds = participantsById.keys.toList(),
                totalTicks = 16,
                tickDelayMs = tickDelayMs,
                eventChannel = deltaChannel,
                pilotSkillByParticipantId = pilotSkillByParticipantId,
                trackDifficulty = trackDifficulty,
                weatherSeverityProvider = { weatherSeverityRef.get() },
                isRaceRunning = { running.get() }
            )
            val weatherJob = launch { weatherWorker.start() }
            val incidentsJob = launch { incidentsWorker.start() }

            var finishedWorkers = 0
            while (running.get() && finishedWorkers < players.size) {
                val delta = try {
                    deltaChannel.receive()
                } catch (_: ClosedReceiveChannelException) {
                    break
                }

                when (delta) {
                    is RaceDelta.Progress -> {
                            if (retiredParticipants.contains(delta.participantId)) {
                                continue
                            }

                            val prevRaw = rawProgressByParticipant[delta.participantId] ?: 0.0
                            val rawStep = max(0.0, delta.progress - prevRaw)
                            val adjustedStep = rawStep * weatherMultiplier
                            val prevAdjusted = progressByParticipant[delta.participantId] ?: 0.0
                            val newAdjusted = prevAdjusted + adjustedStep

                            rawProgressByParticipant[delta.participantId] = delta.progress
                            progressByParticipant[delta.participantId] = newAdjusted
                        eventSink.publish(
                            RaceLogEntry(
                                timestampMs = clock.nowMs(),
                                source = "Car:${delta.participantId}",
                                raceTick = delta.tick,
                                    message = "progress=${String.format(Locale.US, "%.2f", newAdjusted)} weatherX=${String.format(Locale.US, "%.2f", weatherMultiplier)}"
                            )
                        )
                            commentaryChannel.trySend(
                                RaceLogEntry(
                                    timestampMs = clock.nowMs(),
                                    source = "Car:${delta.participantId}",
                                    raceTick = delta.tick,
                                    message = "progress=${String.format(Locale.US, "%.2f", newAdjusted)} weatherX=${String.format(Locale.US, "%.2f", weatherMultiplier)}"
                                )
                            )
                    }

                    is RaceDelta.WorkerMessage -> {
                        eventSink.publish(
                            RaceLogEntry(
                                timestampMs = clock.nowMs(),
                                source = "Car:${delta.participantId}",
                                message = delta.message
                            )
                        )
                            commentaryChannel.trySend(
                                RaceLogEntry(
                                    timestampMs = clock.nowMs(),
                                    source = "Car:${delta.participantId}",
                                    message = delta.message
                                )
                            )
                    }

                    is RaceDelta.Finished -> {
                            if (!retiredParticipants.contains(delta.participantId)) {
                                progressByParticipant[delta.participantId] = max(
                                    progressByParticipant[delta.participantId] ?: 0.0,
                                    rawProgressByParticipant[delta.participantId] ?: delta.finalProgress
                                )
                            }
                        finishedWorkers += 1
                        eventSink.publish(
                            RaceLogEntry(
                                timestampMs = clock.nowMs(),
                                source = "Car:${delta.participantId}",
                                    message = "finished progress=${String.format(Locale.US, "%.2f", progressByParticipant[delta.participantId] ?: delta.finalProgress)}"
                            )
                        )
                            commentaryChannel.trySend(
                                RaceLogEntry(
                                    timestampMs = clock.nowMs(),
                                    source = "Car:${delta.participantId}",
                                    message = "finished progress=${String.format(Locale.US, "%.2f", progressByParticipant[delta.participantId] ?: delta.finalProgress)}"
                                )
                            )
                    }

                        is RaceDelta.WeatherChanged -> {
                            weatherMultiplier = delta.speedMultiplier.coerceIn(0.7, 1.2)
                            val severity = ((1.0 - weatherMultiplier) / 0.3).coerceIn(0.0, 1.0)
                            weatherSeverityRef.set(severity)
                            eventSink.publish(
                                RaceLogEntry(
                                    timestampMs = clock.nowMs(),
                                    source = "Weather",
                                    raceTick = delta.tick,
                                    message = "weather=${delta.weatherCode} speedX=${String.format(Locale.US, "%.2f", weatherMultiplier)} severity=${String.format(Locale.US, "%.2f", severity)}"
                                )
                            )
                            commentaryChannel.trySend(
                                RaceLogEntry(
                                    timestampMs = clock.nowMs(),
                                    source = "Weather",
                                    raceTick = delta.tick,
                                    message = "weather=${delta.weatherCode} speedX=${String.format(Locale.US, "%.2f", weatherMultiplier)} severity=${String.format(Locale.US, "%.2f", severity)}"
                                )
                            )
                        }

                        is RaceDelta.IncidentPenalty -> {
                            if (retiredParticipants.contains(delta.participantId)) {
                                continue
                            }

                            if (delta.reason == "Speeding Fine") {
                                eventSink.publish(
                                    RaceLogEntry(
                                        timestampMs = clock.nowMs(),
                                        source = "Incident",
                                        severity = Severity.WARNING,
                                        raceTick = delta.tick,
                                        message = "${delta.participantId} speeding fine=${String.format(Locale.US, "%.2f", delta.fineAmount)}"
                                    )
                                )
                                commentaryChannel.trySend(
                                    RaceLogEntry(
                                        timestampMs = clock.nowMs(),
                                        source = "Incident",
                                        severity = Severity.WARNING,
                                        raceTick = delta.tick,
                                        message = "${delta.participantId} speeding fine=${String.format(Locale.US, "%.2f", delta.fineAmount)}"
                                    )
                                )
                                continue
                            }

                            if (delta.isTerminal) {
                                retiredParticipants.add(delta.participantId)
                                progressByParticipant[delta.participantId] = -1.0
                                eventSink.publish(
                                    RaceLogEntry(
                                        timestampMs = clock.nowMs(),
                                        source = "Incident",
                                        severity = Severity.ERROR,
                                        raceTick = delta.tick,
                                        message = "${delta.participantId} retired: ${delta.reason}"
                                    )
                                )
                                commentaryChannel.trySend(
                                    RaceLogEntry(
                                        timestampMs = clock.nowMs(),
                                        source = "Incident",
                                        severity = Severity.ERROR,
                                        raceTick = delta.tick,
                                        message = "${delta.participantId} retired: ${delta.reason}"
                                    )
                                )
                            } else {
                                val prev = progressByParticipant[delta.participantId] ?: 0.0
                                val reduced = max(0.0, prev - delta.penalty)
                                progressByParticipant[delta.participantId] = reduced
                                eventSink.publish(
                                    RaceLogEntry(
                                        timestampMs = clock.nowMs(),
                                        source = "Incident",
                                        severity = Severity.WARNING,
                                        raceTick = delta.tick,
                                        message = "${delta.participantId} penalty=${String.format(Locale.US, "%.2f", delta.penalty)} reason=${delta.reason}"
                                    )
                                )
                                commentaryChannel.trySend(
                                    RaceLogEntry(
                                        timestampMs = clock.nowMs(),
                                        source = "Incident",
                                        severity = Severity.WARNING,
                                        raceTick = delta.tick,
                                        message = "${delta.participantId} penalty=${String.format(Locale.US, "%.2f", delta.penalty)} reason=${delta.reason}"
                                    )
                                )
                            }
                        }
                }
            }

                commentatorJob.cancel()
                weatherJob.cancel()
                incidentsJob.cancel()

            val sorted = progressByParticipant.entries
                .sortedByDescending { it.value }
                .mapIndexed { index, entry ->
                    val participant = participantsById[entry.key]
                    ClassicRaceStanding(
                        participantId = entry.key,
                        displayName = participant?.displayName ?: entry.key,
                        finalProgress = entry.value,
                        position = index + 1
                    )
                }

            val winner = sorted.firstOrNull()
            eventSink.publish(
                RaceLogEntry(
                    timestampMs = clock.nowMs(),
                    source = "Race",
                    message = "Race finished: winner=${winner?.displayName ?: "unknown"}"
                )
            )
            commentaryChannel.trySend(
                RaceLogEntry(
                    timestampMs = clock.nowMs(),
                    source = "Race",
                    message = "Race finished: winner=${winner?.displayName ?: "unknown"}"
                )
            )
            running.set(false)
            deltaChannel.close()
            commentaryChannel.close()
            clock.stop()

            finalOutcome = ClassicRaceOutcome(sessionId = sessionId, standings = sorted, commentary = commentaryMessages.toList())
        }

        return finalOutcome ?: ClassicRaceOutcome(sessionId = sessionId, standings = emptyList(), commentary = commentaryMessages.toList())
    }

    fun stopRace() {
        if (!running.compareAndSet(true, false)) return

        raceJob?.cancel()
        raceScope?.cancel()
        clock.stop()

        eventSink.publish(
            RaceLogEntry(
                timestampMs = clock.nowMs(),
                source = "Race",
                severity = Severity.WARNING,
                message = "Race stopped by request"
            )
        )
    }

    private fun calculateBasePace(
        carPerformance: Double,
        pilotSkill: Int,
        trackDifficulty: Double,
        weatherSeverity: Double
    ): Double {
        val performanceFactor = (carPerformance / 140.0).coerceIn(0.0, 8.0)
        val skillFactor = (pilotSkill / 18.0).coerceIn(0.0, 6.0)
        val base = 4.0 + performanceFactor + skillFactor
        val difficultyPenalty = 1.0 - (trackDifficulty * 0.25)
        val weatherPenalty = 1.0 - (weatherSeverity * 0.18)
        return (base * difficultyPenalty * weatherPenalty).coerceAtLeast(2.0)
    }

    private fun calculateVariance(carPerformance: Double, pilotSkill: Int): Double {
        val quality = ((carPerformance / 200.0) + (pilotSkill / 25.0)).coerceAtLeast(1.0)
        return (4.0 / quality).coerceIn(0.6, 2.5)
    }
}









