package com.bmstu.iu3.automanagement.race

/**
 * Общий интерфейс для воркеров гонки (CarWorker, WeatherWorker и т.д.).
 */
interface RaceWorker {
    val id: String
    suspend fun start()
    suspend fun stop()
}


