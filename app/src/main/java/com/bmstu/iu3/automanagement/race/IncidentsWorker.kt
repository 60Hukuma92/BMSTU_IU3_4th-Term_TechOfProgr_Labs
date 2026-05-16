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
    private val track: com.bmstu.iu3.automanagement.models.Track,
    private val isRetired: (String) -> Boolean,
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
            // Редкая проверка инцидентов для реализма
            delay(tickDelayMs * 8)
            tick = min(totalTicks, tick + 8)

            // Выбираем только тех, кто еще в гонке
            val aliveOnes = participantIds.filter { !isRetired(it) }
            if (aliveOnes.isEmpty()) break
            
            val pId = aliveOnes.random()
            val skill = pilotSkillByParticipantId[pId] ?: 50
            val isPro = skill > 75
            val isEasyTrack = track.getStraightsRatio() > 0.6

            val roll = random.nextDouble()
            
            // Снижаем риск поломки: базовый 1% + сложность трассы
            val techRisk = 0.01 + (track.getElevationChange() / 1000.0)
            
            // Шанс штрафа (Про-пилот на быстрой трассе превышает)
            val speedingChance = when {
                isPro && isEasyTrack -> 0.60 // Высокий шанс для профи
                isPro -> 0.10
                else -> 0.03
            }

            if (roll < techRisk) {
                // Только 20% поломок фатальны (DNF)
                val terminal = random.nextDouble() < 0.20
                eventChannel.trySend(RaceDelta.IncidentPenalty(
                    participantId = pId,
                    tick = tick,
                    penalty = if (terminal) 1000.0 else 10.0,
                    reason = if (terminal) "Engine Failure" else "Minor Technical Issue",
                    isTerminal = terminal
                ))
            } else if (roll < techRisk + speedingChance) {
                eventChannel.trySend(RaceDelta.IncidentPenalty(
                    participantId = pId,
                    tick = tick,
                    penalty = 2.0,
                    reason = "Speeding Fine",
                    fineAmount = 500.0 + (skill * 10)
                ))
            }
        }
    }

    override suspend fun stop() { active = false }
}
