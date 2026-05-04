package com.bmstu.iu3.automanagement.race

import com.bmstu.iu3.automanagement.models.PitStopBox
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Простая реализация IPitStopManager с немедленным захватом/освобождением боксов.
 * Не блокирует — возвращает false если нет свободных мест.
 */
class SemaphorePitStopManager(private val boxes: List<PitStopBox>) : PitStopManager {
    private val capacity: Int = boxes.sumOf { it.capacity }
    private val occupied = AtomicInteger(0)
    private val occupantToBox = ConcurrentHashMap<String, String>()

    override fun getBoxes(): List<PitStopBox> = boxes

    override suspend fun requestPitStop(carId: String, timeoutMs: Long): Boolean {
        // immediate non-blocking attempt
        while (true) {
            val current = occupied.get()
            if (current >= capacity) return false
            if (occupied.compareAndSet(current, current + 1)) {
                occupantToBox[carId] = "box_${current + 1}"
                return true
            }
        }
    }

    override fun releasePitStop(carId: String) {
        val removed = occupantToBox.remove(carId)
        if (removed != null) {
            occupied.decrementAndGet()
        }
    }
}


