package com.bmstu.iu3.automanagement.utils

import com.bmstu.iu3.automanagement.models.Chassis
import com.bmstu.iu3.automanagement.models.Engine
import com.bmstu.iu3.automanagement.models.Gearbox
import com.bmstu.iu3.automanagement.models.Suspension

object ComponentCompatibility {
    const val MIN_ENGINE_WEIGHT = 80
    const val MAX_ENGINE_WEIGHT = 190
    const val DEFAULT_ENGINE_WEIGHT = 120

    const val MIN_CHASSIS_ENGINE_LIMIT = 100
    const val MAX_CHASSIS_ENGINE_LIMIT = 220
    const val DEFAULT_CHASSIS_ENGINE_LIMIT = 150

    data class ValidationResult(val isValid: Boolean, val message: String? = null)

    fun normalizeEngineWeight(rawWeight: Int?): Int {
        return (rawWeight ?: DEFAULT_ENGINE_WEIGHT).coerceIn(MIN_ENGINE_WEIGHT, MAX_ENGINE_WEIGHT)
    }

    fun normalizeChassisEngineLimit(rawLimit: Int?): Int {
        return (rawLimit ?: DEFAULT_CHASSIS_ENGINE_LIMIT).coerceIn(MIN_CHASSIS_ENGINE_LIMIT, MAX_CHASSIS_ENGINE_LIMIT)
    }

    fun validateAssembly(engine: Engine, gearbox: Gearbox, chassis: Chassis, suspension: Suspension): ValidationResult {
        if (engine.getType() != gearbox.getType()) {
            return ValidationResult(false, "Engine and Gearbox mismatch (${engine.getType()} vs ${gearbox.getType()})")
        }

        if (engine.getWeight() <= 0 || chassis.getMaxEngineWeight() <= 0) {
            return ValidationResult(false, "Invalid mass data: engine/chassis")
        }

        if (engine.getWeight() > chassis.getMaxEngineWeight()) {
            return ValidationResult(false, "Engine too heavy for this chassis")
        }

        if (suspension.getType() != chassis.getSuspensionType()) {
            return ValidationResult(
                false,
                "Suspension not compatible with chassis (got ${suspension.getType()} required ${chassis.getSuspensionType()})"
            )
        }

        return ValidationResult(true)
    }
}

