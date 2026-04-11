package com.bmstu.iu3.automanagement.utils

import com.bmstu.iu3.automanagement.models.Chassis
import com.bmstu.iu3.automanagement.models.Car
import com.bmstu.iu3.automanagement.models.Engine
import com.bmstu.iu3.automanagement.models.Gearbox
import com.bmstu.iu3.automanagement.models.Suspension
import com.bmstu.iu3.automanagement.models.Weapon

object ComponentComparator {
    const val MIN_ENGINE_WEIGHT = 70
    const val MAX_ENGINE_WEIGHT = 180
    const val DEFAULT_ENGINE_WEIGHT = 110

    const val MIN_CHASSIS_ENGINE_LIMIT = 160
    const val MAX_CHASSIS_ENGINE_LIMIT = 240
    const val DEFAULT_CHASSIS_ENGINE_LIMIT = 180

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

    fun validateWeaponLoad(
        chassis: Chassis,
        engine: Engine?,
        currentWeaponWeight: Int,
        weaponToInstall: Weapon
    ): ValidationResult {
        if (chassis.getMaxEngineWeight() <= 0) {
            return ValidationResult(false, "Invalid chassis mass limit")
        }

        val baseEngineWeight = engine?.getWeight() ?: 0
        val freeCapacity = chassis.getMaxEngineWeight() - baseEngineWeight
        if (freeCapacity <= 0) {
            return ValidationResult(false, "No mass capacity left for weapons")
        }

        val totalWeaponWeight = currentWeaponWeight + weaponToInstall.getWeight()
        return if (totalWeaponWeight <= freeCapacity) {
            ValidationResult(true)
        } else {
            ValidationResult(false, "Weapons are too heavy for this car (${totalWeaponWeight}/${freeCapacity} kg)")
        }
    }

    fun validateCarForRace(car: Car): ValidationResult {
        val engine = car.getEngine() ?: return ValidationResult(false, "Car is incomplete: engine missing")
        val gearbox = car.getGearbox() ?: return ValidationResult(false, "Car is incomplete: gearbox missing")
        val chassis = car.getChassis() ?: return ValidationResult(false, "Car is incomplete: chassis missing")
        val suspension = car.getSuspension() ?: return ValidationResult(false, "Car is incomplete: suspension missing")
        val aero = car.getAerodynamics() ?: return ValidationResult(false, "Car is incomplete: aero missing")
        val tyres = car.getTyres() ?: return ValidationResult(false, "Car is incomplete: tyres missing")

        val broken = listOf(engine, gearbox, chassis, suspension, aero, tyres).firstOrNull { it.isDestroyed() }
        if (broken != null) {
            return ValidationResult(false, "Repair car first: ${broken.getName()} is destroyed")
        }

        val assemblyValidation = validateAssembly(engine, gearbox, chassis, suspension)
        if (!assemblyValidation.isValid) return assemblyValidation

        val weapons = listOfNotNull(car.getMeleeWeapon1(), car.getMeleeWeapon2(), car.getRangedWeapon())
        var weight = 0
        weapons.forEach { weapon ->
            val weaponValidation = validateWeaponLoad(chassis, engine, weight, weapon)
            if (!weaponValidation.isValid) return weaponValidation
            weight += weapon.getWeight()
        }

        return ValidationResult(true)
    }
}

