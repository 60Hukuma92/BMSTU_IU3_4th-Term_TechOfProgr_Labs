package com.bmstu.iu3.automanagement.race

import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.SendChannel
import kotlin.random.Random

class WeatherWorker(
    private val totalTicks: Int,
    private val tickDelayMs: Long,
    private val eventChannel: SendChannel<RaceDelta>,
    private val isRaceRunning: () -> Boolean,
    private val random: Random = Random.Default
) : RaceWorker {

    override val id: String = "weather"
    @Volatile
    private var active: Boolean = true

    override suspend fun start() {
        var tick = 0
        while (active && isRaceRunning() && tick < totalTicks) {
            delay((tickDelayMs * 3).coerceAtLeast(1L))
            tick += 3
            val (code, multiplier) = randomWeather()
            eventChannel.trySend(
                RaceDelta.WeatherChanged(
                    tick = tick,
                    weatherCode = code,
                    speedMultiplier = multiplier
                )
            )
        }
    }

    override suspend fun stop() {
        active = false
    }

    private fun randomWeather(): Pair<String, Double> {
        return when (random.nextInt(4)) {
            0 -> "SUNNY" to 1.00
            1 -> "CLOUDY" to 0.98
            2 -> "RAIN" to 0.93
            else -> "WIND" to 0.96
        }
    }
}

