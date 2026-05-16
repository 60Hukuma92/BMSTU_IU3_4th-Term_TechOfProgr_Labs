package com.bmstu.iu3.automanagement.race

import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.SendChannel
import kotlin.random.Random

class CarWorker(
    private val participant: RaceParticipant,
    private val totalTicks: Int,
    private val tickDelayMs: Long,
    private val eventChannel: SendChannel<RaceDelta>,
    private val tacticBoost: Double,
    private val pitStopManager: PitStopManager,
    private val isRaceRunning: () -> Boolean,
    private val random: Random = Random.Default
) : RaceWorker {

    override val id: String = participant.id
    @Volatile
    private var active: Boolean = true

    override suspend fun start() {
        var progress = 0.0

        eventChannel.trySend(RaceDelta.WorkerMessage(participant.id, "STATUS_STARTING"))

        for (tick in 1..totalTicks) {
            if (!active || !isRaceRunning()) break

            delay(tickDelayMs)

            val eventRoll = random.nextDouble()
            when {
                eventRoll < 0.05 -> eventChannel.trySend(RaceDelta.WorkerMessage(participant.id, "STATUS_PUSHING"))
                eventRoll < 0.10 -> eventChannel.trySend(RaceDelta.WorkerMessage(participant.id, "STATUS_DEFENDING"))
                eventRoll < 0.12 -> eventChannel.trySend(RaceDelta.WorkerMessage(participant.id, "STATUS_LOCKUP"))
            }

            var gain = participant.basePace + random.nextDouble(-1.0, participant.variance)
            gain *= (1.0 + tacticBoost)

            if (tick == totalTicks / 2) {
                eventChannel.trySend(RaceDelta.WorkerMessage(participant.id, "STATUS_PIT_IN"))
                val pitStopGranted = pitStopManager.requestPitStop(participant.id)
                if (pitStopGranted) {
                    delay(tickDelayMs) // Задержка на обслуживание механиками
                    gain *= 1.25 // Значительный бонус к скорости на свежей резине
                    pitStopManager.releasePitStop(participant.id)
                    eventChannel.trySend(RaceDelta.WorkerMessage(participant.id, "STATUS_PIT_OUT_FAST"))
                } else {
                    eventChannel.trySend(RaceDelta.WorkerMessage(participant.id, "STATUS_PIT_DENIED"))
                }
            }

            progress += gain
            eventChannel.trySend(
                RaceDelta.Progress(
                    participantId = participant.id,
                    tick = tick,
                    progress = progress
                )
            )
        }

        eventChannel.trySend(
            RaceDelta.Finished(
                participantId = participant.id,
                finalProgress = progress,
                completed = active && isRaceRunning()
            )
        )
    }

    override suspend fun stop() {
        active = false
    }
}
