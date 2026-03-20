package com.bmstu.iu3.automanagement

import com.bmstu.iu3.automanagement.data.GameState
import com.bmstu.iu3.automanagement.models.*
import com.bmstu.iu3.automanagement.utils.RaceCalculator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GameLogicTest {

    @Before
    fun setup() {
        GameState.setBudget(10000.0)
    }

    @Test
    fun `buy component should subtract money and move item to inventory`() {
        val engineToBuy = GameState.getMarketComponents()
            .filterIsInstance<Engine>()
            .minByOrNull { it.getPrice() }

        assertNotNull("Market should have at least one engine", engineToBuy)

        val initialBudget = GameState.getBudgetObject().getAmount()
        val price = engineToBuy!!.getPrice()
        
        val success = GameState.buyComponent(engineToBuy)
        
        assertTrue("Purchase should be successful", success)
        assertEquals(
            "Budget should be subtracted correctly",
            initialBudget - price, 
            GameState.getBudgetObject().getAmount(), 
            0.1
        )
        assertTrue(
            "Component should be in owned list", 
            GameState.getOwnedComponents().contains(engineToBuy)
        )
        assertFalse(
            "Component should be removed from market", 
            GameState.getMarketComponents().contains(engineToBuy)
        )
    }

    @Test
    fun `cannot hire if budget is low`() {
        GameState.setBudget(10.0)
        // Находим любого пилота на рынке
        val expensivePilot = GameState.getMarketPilots().firstOrNull()
        
        assertNotNull("Market should have pilots", expensivePilot)
        
        val success = GameState.hirePilot(expensivePilot!!)
        
        assertFalse("Should not be able to hire with 10$", success)
        assertFalse(GameState.getHiredPilots().contains(expensivePilot))
    }

    @Test
    fun `car should be incomplete if missing parts`() {
        val car = Car().apply {
            setEngine(Engine())
            setGearbox(Gearbox())
        }
        assertFalse("Car without tyres and chassis should be incomplete", car.isComplete())
    }

    @Test
    fun `rainy weather should increase race time`() {
        val car = Car().apply { setPerformance(500.0) }
        val pilot = Pilot().apply { setSkill(50) }
        val track = Track().apply { setLength(5.0); setStraightsRatio(0.5); setCornersRatio(0.5) }

        val timeSunny = RaceCalculator.calculateRaceTime(car, pilot, track, Weather.SUNNY)
        val timeRainy = RaceCalculator.calculateRaceTime(car, pilot, track, Weather.RAINY)

        assertTrue("Time in rain ($timeRainy) should be more than in sun ($timeSunny)", timeRainy > timeSunny)
    }

    @Test
    fun `skilled pilot should drive faster than rookie`() {
        val car = Car().apply { setPerformance(500.0) }
        val track = Track().apply { setLength(5.0); setStraightsRatio(0.5); setCornersRatio(0.5) }
        
        val proPilot = Pilot().apply { setSkill(95) }
        val rookiePilot = Pilot().apply { setSkill(10) }

        val timePro = RaceCalculator.calculateRaceTime(car, proPilot, track, Weather.SUNNY)
        val timeRookie = RaceCalculator.calculateRaceTime(car, rookiePilot, track, Weather.SUNNY)

        assertTrue("Pro pilot ($timePro) should be faster than rookie ($timeRookie)", timePro < timeRookie)
    }

    @Test
    fun `high wear should increase incident risk`() {
        val pilot = Pilot().apply { setSkill(50) }
        val goodCar = Car().apply { 
            setEngine(Engine().apply { setWear(0.0) }) 
        }
        val brokenCar = Car().apply { 
            setEngine(Engine().apply { setWear(0.9) })
        }

        var incidentsGood = 0
        var incidentsBroken = 0
        repeat(100) {
            if (RaceCalculator.checkIncident(goodCar, pilot, Weather.SUNNY) != null) incidentsGood++
            if (RaceCalculator.checkIncident(brokenCar, pilot, Weather.SUNNY) != null) incidentsBroken++
        }

        assertTrue("Broken car should have more incidents than good car", incidentsBroken > incidentsGood)
    }
}
