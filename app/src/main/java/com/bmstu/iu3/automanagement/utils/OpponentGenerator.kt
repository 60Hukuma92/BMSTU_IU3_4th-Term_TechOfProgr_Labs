package com.bmstu.iu3.automanagement.utils

import com.bmstu.iu3.automanagement.models.*
import com.bmstu.iu3.automanagement.data.GameState
import kotlin.random.Random

object OpponentGenerator {

    private val teamNames = listOf(
        "Red Bullseye", "Ferrous Ferrari", "Mercy Benz", "Alpine Echo", "Aston Martinis",
        "Mclaren Soup", "Haas Tag", "Williams Wallet", "Sauber Safe", "Alpha Tauri"
    )

    fun generateOpponents(count: Int): List<OpponentTeam> {
        val opponents = mutableListOf<OpponentTeam>()
        for (i in 0 until count) {
            generateRandomTeam(teamNames.getOrElse(i) { "Team ${i+1}" })?.let {
                opponents.add(it)
            }
        }
        return opponents
    }

    private fun generateRandomTeam(name: String): OpponentTeam? {
        var aiBudget = Random.nextDouble(20000.0, 35000.0)

        val engineer = GameState.getMarketEngineers()
            .filter { it.getSalary() <= aiBudget * 0.4 }
            .randomOrNull() ?: return null
        
        GameState.aiTakeEngineer(engineer)
        aiBudget -= engineer.getSalary()

        val pilot = GameState.getMarketPilots()
            .filter { it.getSalary() <= aiBudget * 0.4 }
            .randomOrNull() ?: return null
            
        GameState.aiTakePilot(pilot)
        aiBudget -= pilot.getSalary()

        val car = assembleCarFromMarket(engineer, aiBudget) ?: return null

        return OpponentTeam().apply {
            setName(name)
            setCar(car)
            setEngineer(engineer)
            setPilot(pilot)
        }
    }

    private fun assembleCarFromMarket(engineer: Engineer, budget: Double): Car? {
        val market = GameState.getMarketComponents()
        var currentBudget = budget
        
        val engine = market.filterIsInstance<Engine>()
            .filter { it.getPrice() <= currentBudget * 0.4 }
            .randomOrNull() ?: return null
        GameState.aiTakeComponent(engine)
        currentBudget -= engine.getPrice()

        val gearbox = market.filterIsInstance<Gearbox>()
            .filter { it.getType() == engine.getType() && it.getPrice() <= currentBudget * 0.3 }
            .randomOrNull() ?: return null
        GameState.aiTakeComponent(gearbox)
        currentBudget -= gearbox.getPrice()

        val chassis = market.filterIsInstance<Chassis>()
            .filter { it.getMaxEngineWeight() >= engine.getWeight() && it.getPrice() <= currentBudget * 0.3 }
            .randomOrNull() ?: return null
        GameState.aiTakeComponent(chassis)
        currentBudget -= chassis.getPrice()

        val suspension = market.filterIsInstance<Suspension>()
            .filter { it.getType() == chassis.getSuspensionType() && it.getPrice() <= currentBudget * 0.2 }
            .randomOrNull() ?: return null
        GameState.aiTakeComponent(suspension)
        currentBudget -= suspension.getPrice()

        val aero = market.filterIsInstance<Aerodynamics>().filter { it.getPrice() <= currentBudget }.randomOrNull() ?: return null
        GameState.aiTakeComponent(aero)
        currentBudget -= aero.getPrice()

        val tyres = market.filterIsInstance<Tyres>().filter { it.getPrice() <= currentBudget }.randomOrNull() ?: return null
        GameState.aiTakeComponent(tyres)

        return Car().apply {
            setName("AI Challenger")
            setEngine(engine)
            setGearbox(gearbox)
            setChassis(chassis)
            setSuspension(suspension)
            setAerodynamics(aero)
            setTyres(tyres)

            val skillBonus = engineer.getSkill() / 100.0
            setPerformance(getTotalPerformance() * (1.0 + skillBonus))
        }
    }
}
