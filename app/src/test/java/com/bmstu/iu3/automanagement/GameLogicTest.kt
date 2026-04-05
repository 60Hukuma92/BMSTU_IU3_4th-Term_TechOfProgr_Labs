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
        GameState.clearPersonnel()
        GameState.resetTracksToDefault()
    }

    @Test
    fun `player can create update and delete track`() {
        val initialSize = GameState.getTracks().size
        val customTrack = Track().apply {
            setName("Custom Ring")
            setLength(6.2)
            setStraightsRatio(0.6)
            setCornersRatio(0.4)
            setElevationChange(25.0)
        }

        assertTrue(GameState.addTrack(customTrack))
        assertEquals(initialSize + 1, GameState.getTracks().size)

        val updatedTrack = Track().apply {
            setName("Custom Ring GP")
            setLength(6.5)
            setStraightsRatio(0.55)
            setCornersRatio(0.45)
            setElevationChange(30.0)
        }

        assertTrue(GameState.updateTrack(initialSize, updatedTrack))
        assertEquals("Custom Ring GP", GameState.getTracks()[initialSize].getName())

        assertTrue(GameState.removeTrack(initialSize))
        assertEquals(initialSize, GameState.getTracks().size)
    }

    @Test
    fun `invalid track should be rejected`() {
        val invalidTrack = Track().apply {
            setName("Bad")
            setLength(4.0)
            setStraightsRatio(0.8)
            setCornersRatio(0.5)
            setElevationChange(10.0)
        }

        assertFalse(GameState.addTrack(invalidTrack))
    }

    @Test
    fun `speeding fine risk should be higher for pro on easy track`() {
        val proPilot = Pilot().apply { setSkill(90) }
        val rookiePilot = Pilot().apply { setSkill(20) }
        val easyTrack = Track().apply { setStraightsRatio(0.8) }
        val hardTrack = Track().apply { setStraightsRatio(0.2) }
        val car = Car()

        var proEasyFines = 0
        var rookieEasyFines = 0
        var proHardFines = 0

        repeat(1000) {
            if (RaceCalculator.checkIncident(car, proPilot, easyTrack, Weather.SUNNY)?.getReason() == "Speeding Fine") proEasyFines++
            if (RaceCalculator.checkIncident(car, rookiePilot, easyTrack, Weather.SUNNY)?.getReason() == "Speeding Fine") rookieEasyFines++
            if (RaceCalculator.checkIncident(car, proPilot, hardTrack, Weather.SUNNY)?.getReason() == "Speeding Fine") proHardFines++
        }

        assertTrue("Pro on easy track should have more fines than rookie ($proEasyFines vs $rookieEasyFines)", proEasyFines > rookieEasyFines)
        assertTrue("Pro on easy track should have more fines than pro on hard track ($proEasyFines vs $proHardFines)", proEasyFines > proHardFines)
    }

    @Test
    fun `pilot should go to jail after 3 unpaid races`() {
        val pilot = Pilot().apply { 
            setName("Test Pilot")
            setFineAmount(1000.0)
            setFineDeadline(3) 
        }
        GameState.addPilotDirectly(pilot)

        // Гонка 1: дедлайн 3 -> 2
        GameState.processRaceEndUpdates()
        assertEquals(2, pilot.getFineDeadline())
        
        // Гонка 2: дедлайн 2 -> 1
        GameState.processRaceEndUpdates()
        assertEquals(1, pilot.getFineDeadline())

        // Гонка 3: дедлайн 1 -> 0, попадает в тюрьму
        GameState.processRaceEndUpdates()
        
        assertFalse("Pilot should be removed from hired list", GameState.getHiredPilots().contains(pilot))
        assertTrue("Pilot should be in jail list", GameState.getJailedPilots().contains(pilot))
        assertEquals("Sentence should be exactly 3 races", 3, pilot.getJailSentence())
    }

    @Test
    fun `pilot should be released from jail after 3 races`() {
        val pilot = Pilot().apply { 
            setName("Jailed Pilot")
            setJailSentence(3)
        }
        GameState.addJailedPilotDirectly(pilot)

        // 3 гонки отсидки
        repeat(3) { GameState.processRaceEndUpdates() }

        assertTrue("Pilot should be back in hired list", GameState.getHiredPilots().contains(pilot))
        assertFalse("Pilot should not be in jail", GameState.getJailedPilots().contains(pilot))
        assertEquals(0, pilot.getJailSentence())
    }

    @Test
    fun `buy component should subtract money and move item to inventory`() {
        val initialBudget = 10000.0
        GameState.setBudget(initialBudget)
        
        val engineToBuy = GameState.getMarketComponents().filterIsInstance<Engine>().first()
        val price = engineToBuy.getPrice()
        
        val success = GameState.buyComponent(engineToBuy)
        
        assertTrue(success)
        assertEquals(initialBudget - price, GameState.getBudgetObject().getAmount(), 0.1)
        assertTrue(GameState.getOwnedComponents().contains(engineToBuy))
    }

    @Test
    fun `cannot buy component with insufficient budget`() {
        GameState.setBudget(10.0)
        val expensiveEngine = GameState.getMarketComponents().filterIsInstance<Engine>().first()
        
        val success = GameState.buyComponent(expensiveEngine)
        
        assertFalse(success)
    }

    @Test
    fun `car should be incomplete if missing essential parts`() {
        val car = Car().apply {
            setEngine(Engine())
            setGearbox(Gearbox())
        }
        assertFalse(car.isComplete())
    }

    @Test
    fun `rainy weather impact on race time`() {
        val car = Car().apply { setPerformance(500.0) }
        val pilot = Pilot().apply { setSkill(50) }
        val track = Track().apply { setLength(5.0); setStraightsRatio(0.5); setCornersRatio(0.5) }

        val timeSunny = RaceCalculator.calculateRaceTime(car, pilot, track, Weather.SUNNY)
        val timeRainy = RaceCalculator.calculateRaceTime(car, pilot, track, Weather.RAINY)

        assertTrue("Rainy time ($timeRainy) should be slower than sunny ($timeSunny)", timeRainy > timeSunny)
    }

    @Test
    fun `pro pilot vs rookie performance`() {
        val car = Car().apply { setPerformance(500.0) }
        val track = Track().apply { setLength(5.0); setStraightsRatio(0.5); setCornersRatio(0.5) }
        
        val proPilot = Pilot().apply { setSkill(95) }
        val rookiePilot = Pilot().apply { setSkill(10) }

        val timePro = RaceCalculator.calculateRaceTime(car, proPilot, track, Weather.SUNNY)
        val timeRookie = RaceCalculator.calculateRaceTime(car, rookiePilot, track, Weather.SUNNY)

        assertTrue("Pro ($timePro) should be significantly faster than rookie ($timeRookie)", timePro < timeRookie)
    }

    @Test
    fun `high wear increases incident risk`() {
        val pilot = Pilot().apply { setSkill(50) }
        val track = Track()
        val brokenCar = Car().apply { 
            setEngine(Engine().apply { setWear(0.95) })
        }
        val goodCar = Car().apply { 
            setEngine(Engine().apply { setWear(0.0) })
        }

        var incidentsBroken = 0
        var incidentsGood = 0
        repeat(100) {
            if (RaceCalculator.checkIncident(brokenCar, pilot, track, Weather.SUNNY) != null) incidentsBroken++
            if (RaceCalculator.checkIncident(goodCar, pilot, track, Weather.SUNNY) != null) incidentsGood++
        }

        assertTrue(incidentsBroken > incidentsGood)
    }

    @Test
    fun `bail release should return pilot to team and spend money`() {
        val initialBudget = 5000.0
        GameState.setBudget(initialBudget)
        val pilot = Pilot().apply { 
            setName("Prisoner")
            setSalary(2000.0)
            setJailSentence(3)
        }
        GameState.addJailedPilotDirectly(pilot)

        val success = GameState.releaseFromJail(pilot)
        
        assertTrue(success)
        assertTrue(GameState.getHiredPilots().contains(pilot))
        assertFalse(GameState.getJailedPilots().contains(pilot))
        assertEquals(initialBudget - 1000.0, GameState.getBudgetObject().getAmount(), 0.1)
    }
}
