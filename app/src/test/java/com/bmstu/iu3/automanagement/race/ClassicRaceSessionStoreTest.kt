package com.bmstu.iu3.automanagement.race

import com.bmstu.iu3.automanagement.data.GameState
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertTrue

class ClassicRaceSessionStoreTest {

    @Before
    fun setUp() {
        GameState.clearInventory()
        GameState.resetTracksToDefault()
        GameState.setBudget(50000.0)
    }

    @Test
    fun `cannot start two races simultaneously`() {
        val track = GameState.getTracks().first()
        val pilot = com.bmstu.iu3.automanagement.models.Pilot().apply { setSkill(50) }
        val car = com.bmstu.iu3.automanagement.models.Car().apply { setPerformance(500.0) }

        val start1 = ClassicRaceSessionStore.startClassicRace(car, pilot, track, com.bmstu.iu3.automanagement.models.Weather.SUNNY)
        assertTrue("First race should start", start1)

        Thread.sleep(50)

        val start2 = ClassicRaceSessionStore.startClassicRace(car, pilot, track, com.bmstu.iu3.automanagement.models.Weather.SUNNY)
        assertTrue("Second race should fail", !start2)

        ClassicRaceSessionStore.stopCurrentRace()
    }

    @Test
    fun `stop clears isRunning flag`() {
        val track = GameState.getTracks().first()
        val pilot = com.bmstu.iu3.automanagement.models.Pilot().apply { setSkill(50) }
        val car = com.bmstu.iu3.automanagement.models.Car().apply { setPerformance(500.0) }

        ClassicRaceSessionStore.startClassicRace(car, pilot, track, com.bmstu.iu3.automanagement.models.Weather.SUNNY)
        Thread.sleep(50)
        ClassicRaceSessionStore.stopCurrentRace()
        Thread.sleep(100)

        assertTrue("Race should be stopped", !ClassicRaceSessionStore.state.value.isRunning)
    }

    @Test
    fun `tracks exist and can be selected`() {
        val tracks = GameState.getTracks()
        assertTrue("Should have default tracks", tracks.isNotEmpty())
        assertTrue("Should have at least 3 default tracks", tracks.size >= 3)
    }

    @Test
    fun `budget is initialized and can be spent`() {
        val initialBudget = GameState.getBudgetObject().getAmount()
        assertTrue("Should have initial budget", initialBudget > 0)

        GameState.spendMoney(1000.0)
        val afterSpend = GameState.getBudgetObject().getAmount()
        assertTrue("Budget should decrease", afterSpend < initialBudget)
    }
}

