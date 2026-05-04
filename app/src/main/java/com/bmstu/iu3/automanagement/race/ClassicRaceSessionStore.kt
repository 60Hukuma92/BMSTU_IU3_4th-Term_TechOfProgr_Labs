package com.bmstu.iu3.automanagement.race

import com.bmstu.iu3.automanagement.data.GameState
import com.bmstu.iu3.automanagement.models.Car
import com.bmstu.iu3.automanagement.models.Pilot
import com.bmstu.iu3.automanagement.models.PitStopBox
import com.bmstu.iu3.automanagement.models.RaceResult
import com.bmstu.iu3.automanagement.models.Track
import com.bmstu.iu3.automanagement.models.Weather
import com.bmstu.iu3.automanagement.utils.RaceCalculator
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

object ClassicRaceSessionStore {
    data class State(
        val isRunning: Boolean = false,
        val logLines: List<String> = emptyList(),
        val finished: Boolean = false,
        val hasResult: Boolean = false,
        val errorMessage: String? = null
    )

    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private val stateMutex = Mutex()
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()
    private var currentJob: Job? = null
    private val running = AtomicBoolean(false)
    // map from internal participant id (e.g. "car-1") -> display name (e.g. "YOU" or opponent name)
    private var participantIdToDisplayName: Map<String, String> = emptyMap()

    fun startClassicRace(car: Car, pilot: Pilot, track: Track, weather: Weather): Boolean {
        if (!running.compareAndSet(false, true)) return false
        if (currentJob?.isActive == true) {
            running.set(false)
            return false
        }

        GameState.generateOpponents()
        val opponents = GameState.getOpponentTeams()

        val eventSink = InMemoryEventSink()
        val engine = ClassicRaceEngine(
            clock = SystemRaceClock(),
            tacticResolver = SimpleTacticResolver(),
            pitStopManager = SemaphorePitStopManager(
                boxes = listOf(PitStopBox(id = "BOX-1"), PitStopBox(id = "BOX-2"))
            ),
            eventSink = eventSink
        )

        val participantNames = buildList {
            add("YOU")
            addAll(opponents.map { it.getName() })
        }
        // prepare mapping for nicer log output (car-1 -> displayName)
        participantIdToDisplayName = participantNames.mapIndexed { idx, name -> "car-${idx + 1}" to name }.toMap()
        val pilotSkillsByName = buildMap {
            put("YOU", pilot.getSkill())
            opponents.forEach { team ->
                put(team.getName(), team.getPilot()?.getSkill() ?: 50)
            }
        }
        val carPerformanceByName = buildMap {
            put("YOU", maxOf(car.getPerformance(), car.getTotalPerformance()))
            opponents.forEach { team ->
                val opponentCar = team.getCar()
                put(team.getName(), maxOf(opponentCar?.getPerformance() ?: 0.0, opponentCar?.getTotalPerformance() ?: 0.0))
            }
        }
        val trackDifficulty = (
            track.getCornersRatio() * 0.6 +
                (track.getElevationChange() / 120.0).coerceIn(0.0, 1.0) * 0.4
            ).coerceIn(0.0, 1.0)
        val weatherSeverity = when (weather) {
            Weather.SUNNY -> 0.10
            Weather.CLOUDY -> 0.22
            Weather.RAINY -> 0.62
            Weather.STORM -> 0.85
        }

        currentJob = scope.launch {
            val raceFinished = CompletableDeferred<ClassicRaceOutcome>()
            setState(State(isRunning = true))

            engine.startRace(
                trackId = track.getName(),
                players = participantNames,
                tacticId = null,
                pilotSkillsByName = pilotSkillsByName,
                carPerformanceByName = carPerformanceByName,
                trackDifficulty = trackDifficulty,
                initialWeatherSeverity = weatherSeverity,
                onFinished = { outcome ->
                    if (!raceFinished.isCompleted) raceFinished.complete(outcome)
                }
            )

            while (isActive && !raceFinished.isCompleted) {
                publishLogs(eventSink.getEvents())
                delay(350)
            }

            val outcome = raceFinished.await()
            publishLogs(eventSink.getEvents())

            GameState.addRaceCommentary(outcome.commentary)

            val results = buildResults(outcome, track)
            val playerIncident = RaceCalculator.checkIncident(car, pilot, track, weather)
            results.firstOrNull { it.getTeamName() == "YOU" }?.setIncident(playerIncident)

            if (playerIncident?.getSeverity() == "Terminal") {
                results.firstOrNull { it.getTeamName() == "YOU" }?.setTime(999999.0)
                results.sortBy { it.getTime() }
                results.forEachIndexed { index, res -> res.setPosition(index + 1) }
            }

            if (playerIncident?.getReason() == "Speeding Fine") {
                pilot.setFineAmount(playerIncident.getFineAmount())
                pilot.setFineDeadline(3)
            }

            results.forEachIndexed { index, res ->
                res.setPosition(index + 1)
                if (res.getTeamName() == "YOU") {
                    val prize = when (index) {
                        0 -> 5000.0
                        1 -> 3000.0
                        2 -> 1500.0
                        else -> 0.0
                    }
                    res.setPrizeMoney(prize)
                    GameState.addMoney(prize)
                }
            }

            RaceCalculator.applyPostRaceConsequences(car, playerIncident)
            GameState.addRaceResult(results)
            GameState.processRaceEndUpdates()
            publishLogs(eventSink.getEvents())
            setState(State(isRunning = false, logLines = _state.value.logLines, finished = true, hasResult = true))
            // clear temporary mapping to avoid stale references
            participantIdToDisplayName = emptyMap()
            running.set(false)
        }

        return true
    }

    fun stopCurrentRace() {
        currentJob?.cancel()
        running.set(false)
        scope.launch { setState(_state.value.copy(isRunning = false, finished = true)) }
    }

    private suspend fun publishLogs(entries: List<com.bmstu.iu3.automanagement.models.RaceLogEntry>) {
        val formatted = entries.map { entry ->
            val seconds = entry.timestampMs / 1000
            val minutes = seconds / 60
            val sec = seconds % 60
            val millis = entry.timestampMs % 1000
            String.format(Locale.US, "[%02d:%02d.%03d] [%s] %s", minutes, sec, millis, entry.source, entry.message)
        }
        setState(_state.value.copy(logLines = formatted))
    }

    private suspend fun setState(newState: State) {
        stateMutex.withLock {
            _state.value = newState
        }
    }

    private fun buildResults(outcome: ClassicRaceOutcome, track: Track): MutableList<RaceResult> {
        val standings = outcome.standings.sortedBy { it.position }
        val leaderProgress = standings.maxOfOrNull { it.finalProgress }?.coerceAtLeast(1.0) ?: 1.0
        val baseSeconds = track.getLength() * 60.0 + (track.getCornersRatio() * 45.0) + (track.getElevationChange() * 0.7)

        return standings.map { standing ->
            val timeSeconds = if (standing.finalProgress <= 0.0) {
                999999.0
            } else {
                baseSeconds * (leaderProgress / standing.finalProgress)
            }
            RaceResult().apply {
                setTeamName(standing.displayName)
                setPosition(standing.position)
                setTime(timeSeconds)
            }
        }.toMutableList()
    }
}


