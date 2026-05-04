package com.bmstu.iu3.automanagement.race

import com.bmstu.iu3.automanagement.models.PitStopBox

/**
 * API менеджера пит-стопов.
 */
interface PitStopManager {
    fun getBoxes(): List<PitStopBox>
    /**
     * Попытка захватить пит-бокс. Возвращает true если успешно, false — если нет мест.
     */
    suspend fun requestPitStop(carId: String, timeoutMs: Long = 0): Boolean
    fun releasePitStop(carId: String)
}


