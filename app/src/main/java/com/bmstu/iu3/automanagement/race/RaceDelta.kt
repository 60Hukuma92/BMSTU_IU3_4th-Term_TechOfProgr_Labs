package com.bmstu.iu3.automanagement.race

/**
 * Immutable-дельты от воркеров к координатору. Координатор — единственный писатель итогового состояния.
 */
sealed class RaceDelta {
    data class Progress(
        val participantId: String,
        val tick: Int,
        val progress: Double
    ) : RaceDelta()

    data class WorkerMessage(
        val participantId: String,
        val message: String
    ) : RaceDelta()

    data class Finished(
        val participantId: String,
        val finalProgress: Double,
        val completed: Boolean
    ) : RaceDelta()

    data class WeatherChanged(
        val tick: Int,
        val weatherCode: String,
        val speedMultiplier: Double
    ) : RaceDelta()

    data class IncidentPenalty(
        val participantId: String,
        val tick: Int,
        val penalty: Double,
        val reason: String,
        val isTerminal: Boolean = false,
        val fineAmount: Double = 0.0
    ) : RaceDelta()
}



