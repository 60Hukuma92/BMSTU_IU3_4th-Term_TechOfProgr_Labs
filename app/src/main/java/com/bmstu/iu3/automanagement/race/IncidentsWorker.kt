package com.bmstu.iu3.automanagement.race

import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.SendChannel
import kotlin.math.min
import kotlin.random.Random

class IncidentsWorker(
    private val participantIds: List<String>,
    private val totalTicks: Int,
    private val tickDelayMs: Long,
    private val eventChannel: SendChannel<RaceDelta>,
    private val pilotSkillByParticipantId: Map<String, Int>,
    private val trackDifficulty: Double,
    private val weatherSeverityProvider: () -> Double,
    private val isRaceRunning: () -> Boolean,
    private val random: Random = Random.Default
) : RaceWorker {

    override val id: String = "incidents"
    @Volatile
    private var active: Boolean = true

    override suspend fun start() {
        if (participantIds.isEmpty()) return

        var tick = 0
        while (active && isRaceRunning() && tick < totalTicks) {
            delay((tickDelayMs * 2).coerceAtLeast(1L))
            tick = min(totalTicks, tick + 2)

            val participantId = participantIds[random.nextInt(participantIds.size)]
            val pilotSkill = pilotSkillByParticipantId[participantId] ?: 50
            val weatherSeverity = weatherSeverityProvider().coerceIn(0.0, 1.0)
            val clampedTrackDifficulty = trackDifficulty.coerceIn(0.0, 1.0)
            val skillRisk = ((100 - pilotSkill).coerceIn(0, 100)) / 100.0
            val isPro = pilotSkill > 70
            val isEasyTrack = clampedTrackDifficulty < 0.40

            // 1) Технический инцидент (условно аналог Technical failure)
            val technicalChance = (
                0.01 +
                    weatherSeverity * 0.12 +
                    clampedTrackDifficulty * 0.08 +
                    skillRisk * 0.04
                ).coerceIn(0.01, 0.55)

            // 2) Штраф за скорость (сохраняем вашу старую идею: профи + лёгкая трасса = больше шанс)
            val speedingChance = when {
                isPro && isEasyTrack -> 0.40
                isPro -> 0.15
                isEasyTrack -> 0.05
                else -> 0.01
            }

            val roll = random.nextDouble()

            if (roll < technicalChance) {
                val terminalChance = (0.30 + weatherSeverity * 0.20).coerceIn(0.10, 0.80)
                val terminal = random.nextDouble() < terminalChance
                val technicalPenalty = if (terminal) 100_000.0 else random.nextDouble(4.0, 16.0)

                eventChannel.trySend(
                    RaceDelta.IncidentPenalty(
                        participantId = participantId,
                        tick = tick,
                        penalty = technicalPenalty,
                        reason = "Technical failure",
                        isTerminal = terminal,
                        fineAmount = 0.0
                    )
                )
                continue
            }

            if (roll < technicalChance + speedingChance) {
                val fineAmount = 500.0 + (pilotSkill * 10.0)
                eventChannel.trySend(
                    RaceDelta.IncidentPenalty(
                        participantId = participantId,
                        tick = tick,
                        penalty = 0.0,
                        reason = "Speeding Fine",
                        isTerminal = false,
                        fineAmount = fineAmount
                    )
                )
            }
        }
    }

    override suspend fun stop() {
        active = false
    }
}

