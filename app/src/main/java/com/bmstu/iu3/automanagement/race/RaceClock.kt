package com.bmstu.iu3.automanagement.race

/**
 * Простой интерфейс часов гонки. Позволяет воркерам получать время от старта (ms).
 */
interface RaceClock {
    fun nowMs(): Long
    fun start()
    fun stop()
}

