package com.bmstu.iu3.automanagement.race

import com.bmstu.iu3.automanagement.models.RaceLogEntry

/**
 * Приёмник событий гонки. Работает в координаторе и получает immutable события от воркеров.
 */
interface RaceEventSink {
    fun publish(event: RaceLogEntry)
}


