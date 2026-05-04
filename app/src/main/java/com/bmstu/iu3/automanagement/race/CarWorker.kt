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
        eventChannel.trySend(RaceDelta.WorkerMessage(participant.id, "${participant.displayName} started"))

        for (tick in 1..totalTicks) {
            if (!active || !isRaceRunning()) break

            delay(tickDelayMs)

            var gain = participant.basePace + random.nextDouble(0.0, participant.variance)
            gain *= (1.0 + tacticBoost)

            if (tick == totalTicks / 2) {
                val pitStopGranted = pitStopManager.requestPitStop(participant.id)
                if (pitStopGranted) {
                    gain *= 1.05
                    pitStopManager.releasePitStop(participant.id)
                    eventChannel.trySend(RaceDelta.WorkerMessage(participant.id, "${participant.displayName} completed pit-stop (+bonus)"))
                } else {
                    eventChannel.trySend(RaceDelta.WorkerMessage(participant.id, "${participant.displayName} pit-stop denied (all boxes busy)"))
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

