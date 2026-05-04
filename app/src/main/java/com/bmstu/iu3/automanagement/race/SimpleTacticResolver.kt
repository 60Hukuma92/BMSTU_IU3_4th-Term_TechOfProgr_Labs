package com.bmstu.iu3.automanagement.race

import com.bmstu.iu3.automanagement.models.Tactic

/**
 * Простая реализация TacticResolver: возвращает модификатор из тактики по погоде
 * или 0.0 если тактика/погода отсутствует.
 */
class SimpleTacticResolver : TacticResolver {
    override fun resolve(tactic: Tactic?, weatherType: String?, carId: String?): Double {
        if (tactic == null || weatherType == null) return 0.0
        return tactic.weatherModifiers[weatherType] ?: 0.0
    }
}


