package com.bmstu.iu3.automanagement.race

import com.bmstu.iu3.automanagement.models.Tactic

/**
 * Интерфейс для расчёта бонуса тактики по погоде и болиду.
 */
interface TacticResolver {
    /**
     * Возвращает множитель к времени (например, -0.05 = -5% к итоговому времени).
     */
    fun resolve(tactic: Tactic?, weatherType: String?, carId: String?): Double
}


