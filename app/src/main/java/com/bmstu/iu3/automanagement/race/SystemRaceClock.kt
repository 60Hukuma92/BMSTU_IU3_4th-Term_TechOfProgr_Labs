package com.bmstu.iu3.automanagement.race

import java.util.concurrent.atomic.AtomicLong

/**
 * Простейшая реализация RaceClock, опирающаяся на System.currentTimeMillis.
 * start() фиксирует стартовую отметку, nowMs() возвращает смещение от старта.
 */
class SystemRaceClock : RaceClock {
    private val startMs = AtomicLong(0)

    override fun nowMs(): Long {
        val s = startMs.get()
        return if (s == 0L) 0L else System.currentTimeMillis() - s
    }

    override fun start() {
        startMs.set(System.currentTimeMillis())
    }

    override fun stop() {
        startMs.set(0)
    }
}

