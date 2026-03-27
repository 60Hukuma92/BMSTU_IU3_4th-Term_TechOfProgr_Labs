package com.bmstu.iu3.automanagement.utils

import com.bmstu.iu3.automanagement.models.*
import kotlin.random.Random

object RaceCalculator {

    fun calculateRaceTime(car: Car, pilot: Pilot, track: Track, weather: Weather): Double {
        var time = track.getLength() * 100.0
        val carBonus = (car.getPerformance() / 2000.0).coerceAtMost(0.3)
        time *= (1.0 - carBonus)
        val pilotBonus = (pilot.getSkill() / 100.0) * 0.15
        time *= (1.0 - pilotBonus)
        val straightsSpeed = (car.getEngine()?.getPower() ?: 0) / 1000.0
        time -= (track.getStraightsRatio() * straightsSpeed * 10.0)
        val corneringAbility = (car.getAerodynamics()?.getPerformance() ?: 0.0) + (car.getTyres()?.getGrip() ?: 0.0)
        time -= (track.getCornersRatio() * (corneringAbility / 100.0) * 5.0)
        val weatherImpact = 1.0 / weather.gripMultiplier
        val weatherMitigation = (pilot.getSkill() / 100.0) * 0.5
        time *= (1.0 + (weatherImpact - 1.0) * (1.0 - weatherMitigation))
        return time * Random.nextDouble(0.98, 1.02)
    }

    fun checkIncident(car: Car, pilot: Pilot, track: Track, weather: Weather): Incident? {
        // Шанс технической поломки
        var techRisk = 0.01
        listOf(car.getEngine(), car.getGearbox(), car.getChassis()).forEach { 
            if (it != null && it.getWear() > 0.5) techRisk += (it.getWear() - 0.5) * 0.2
        }
        if (weather == Weather.RAINY) techRisk += 0.03
        if (weather == Weather.STORM) techRisk += 0.10

        if (Random.nextDouble() < techRisk) {
            return Incident().apply {
                setReason("Technical failure")
                setSeverity(if (Random.nextDouble() < 0.3) "Terminal" else "Minor")
            }
        }

        // Шанс штрафа за превышение скорости
        // "Про пилот всегда превышает скорость на лёгкой трассе (высокий StraightsRatio)"
        val isPro = pilot.getSkill() > 70
        val isEasyTrack = track.getStraightsRatio() > 0.6
        
        val speedingChance = when {
            isPro && isEasyTrack -> 0.40 // Высокий шанс для профи на быстрой трассе
            isPro -> 0.15
            isEasyTrack -> 0.05
            else -> 0.01
        }

        if (Random.nextDouble() < speedingChance) {
            return Incident().apply {
                setReason("Speeding Fine")
                setSeverity("Fine")
                setFineAmount(500.0 + (pilot.getSkill() * 10.0))
            }
        }

        return null
    }

    fun applyPostRaceConsequences(car: Car, incident: Incident?) {
        val components = listOf(car.getEngine(), car.getGearbox(), car.getChassis(), 
                               car.getSuspension(), car.getAerodynamics(), car.getTyres())

        components.forEach { it?.let { it.setWear((it.getWear() + Random.nextDouble(0.05, 0.15)).coerceAtMost(1.0)) } }

        if (incident != null && incident.getReason() == "Technical failure") {
            val brokenCount = if (incident.getSeverity() == "Terminal") 2 else 1
            components.filterNotNull().filter { !it.isDestroyed() }.shuffled().take(brokenCount).forEach {
                it.setDestroyed(true)
                it.setWear(1.0)
            }
        }
    }
}
