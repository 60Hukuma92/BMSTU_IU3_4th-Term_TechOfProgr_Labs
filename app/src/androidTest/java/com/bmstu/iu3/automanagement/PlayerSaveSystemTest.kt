package com.bmstu.iu3.automanagement

import android.content.Context
import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bmstu.iu3.automanagement.data.GameSaveManager
import com.bmstu.iu3.automanagement.data.GameState
import com.bmstu.iu3.automanagement.models.*
import com.bmstu.iu3.automanagement.survival.SurvivalRaceEngine
import com.bmstu.iu3.automanagement.survival.SurvivalRandom
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayerSaveSystemTest {

    private lateinit var saveManager: GameSaveManager
    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        saveManager = GameSaveManager(context)
        saveManager.getAllPlayers().forEach { player ->
            saveManager.deleteGame(player)
        }
    }

    @Test
    fun testCreateNewGame() {
        val playerName = "TestPlayer1"
        val success = saveManager.createNewGame(playerName)
        
        assertTrue("Game should be created successfully", success)
        assertTrue("Game should exist after creation", saveManager.gameExists(playerName))
        assertTrue("Player should be in the list", saveManager.getAllPlayers().contains(playerName))
    }

    @Test
    fun testSaveAndLoadGameState() {
        val playerName = "TestPlayer2"
        saveManager.createNewGame(playerName)

        val testBudget = 5000.0
        GameState.setBudget(testBudget)
        GameState.setCurrentPlayer(playerName)

        saveManager.saveGame(playerName)

        GameState.setBudget(0.0)

        val success = saveManager.loadGame(playerName)
        assertTrue("Game should load successfully", success)
        assertEquals("Budget should be restored", testBudget, GameState.getBudgetObject().getAmount(), 0.1)
    }

    @Test
    fun testMultiplePlayerProfiles() {
        val player1 = "Player1"
        val player2 = "Player2"

        saveManager.createNewGame(player1)
        GameState.setBudget(3000.0)
        GameState.setCurrentPlayer(player1)
        saveManager.saveGame(player1)

        saveManager.createNewGame(player2)
        GameState.setBudget(7000.0)
        GameState.setCurrentPlayer(player2)
        saveManager.saveGame(player2)

        val players = saveManager.getAllPlayers()
        assertTrue("Player1 should exist", players.contains(player1))
        assertTrue("Player2 should exist", players.contains(player2))

        saveManager.loadGame(player1)
        assertEquals("Player1 budget should be 3000", 3000.0, GameState.getBudgetObject().getAmount(), 0.1)

        saveManager.loadGame(player2)
        assertEquals("Player2 budget should be 7000", 7000.0, GameState.getBudgetObject().getAmount(), 0.1)
    }

    @Test
    fun testDeleteGame() {
        val playerName = "PlayerToDelete"
        saveManager.createNewGame(playerName)
        
        assertTrue("Game should exist", saveManager.gameExists(playerName))
        
        val success = saveManager.deleteGame(playerName)
        assertTrue("Delete should succeed", success)
        assertFalse("Game should not exist after deletion", saveManager.gameExists(playerName))
        assertFalse("Player should not be in list", saveManager.getAllPlayers().contains(playerName))
    }

    @Test
    fun testCurrentPlayerTracking() {
        val player1 = "Player1"
        val player2 = "Player2"
        
        saveManager.createNewGame(player1)
        assertEquals("Current player should be player1", player1, saveManager.getCurrentPlayer())
        
        saveManager.createNewGame(player2)
        assertEquals("Current player should be player2", player2, saveManager.getCurrentPlayer())
    }

    @Test
    fun testTracksAreSavedAndLoaded() {
        val playerName = "TrackPlayer"
        saveManager.createNewGame(playerName)

        val customTrack = Track().apply {
            setName("Player Circuit")
            setLength(4.8)
            setStraightsRatio(0.65)
            setCornersRatio(0.35)
            setElevationChange(18.0)
        }

        GameState.addTrack(customTrack)
        saveManager.saveGame(playerName)

        GameState.resetTracksToDefault()
        assertFalse(GameState.getTracks().any { it.getName() == "Player Circuit" })

        val success = saveManager.loadGame(playerName)
        assertTrue("Game should load successfully", success)
        assertTrue(
            "Custom track should be restored from save",
            GameState.getTracks().any { it.getName() == "Player Circuit" }
        )
    }

    @Test
    fun testCompromisingEvidenceRoundTripAndEncryption() {
        val playerName = "EvidencePlayer"
        saveManager.createNewGame(playerName)

        val awarded = saveManager.awardCompromisingEvidenceToPlayer(playerName, 11)
        assertNotNull("Compromising evidence should be awarded", awarded)
        assertEquals(playerName, awarded!!.getPlayerName())
        assertEquals(11, awarded.getPushBackValue())

        val loaded = saveManager.getCompromisingEvidenceForPlayer(playerName)
        assertNotNull("Compromising evidence should be loadable", loaded)
        assertEquals(playerName, loaded!!.getPlayerName())
        assertEquals(11, loaded.getPushBackValue())

        val rawKey = "compromisingEvidence_" + Base64.encodeToString(
            playerName.toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP or Base64.URL_SAFE
        )
        val rawValue = context.getSharedPreferences("compromising_evidence_store", Context.MODE_PRIVATE)
            .getString(rawKey, null)

        assertNotNull("Encrypted payload should be stored", rawValue)
        val storedValue = rawValue.orEmpty()
        assertFalse(storedValue.contains(playerName))
        assertFalse(storedValue.contains("<compromisingEvidence>"))
        assertFalse(storedValue.contains("pushBackValue"))
    }

    @Test
    fun testCompromisingEvidenceRemovedWithProfile() {
        val playerName = "DeleteEvidencePlayer"
        saveManager.createNewGame(playerName)

        saveManager.awardCompromisingEvidenceToPlayer(playerName, 8)
        assertTrue(saveManager.hasCompromisingEvidenceForPlayer(playerName))

        saveManager.deleteGame(playerName)

        assertFalse(saveManager.gameExists(playerName))
        assertFalse(saveManager.hasCompromisingEvidenceForPlayer(playerName))
    }

    @Test
    fun testCompromisingEvidenceActionWorksInSurvivalRace() {
        val playerCar = Car().apply {
            setName("Player Car")
            setPerformance(250.0)
        }
        val playerPilot = Pilot().apply {
            setName("Player Pilot")
            setSkill(40)
            setSalary(0.0)
        }
        val botCar = Car().apply {
            setName("Bot Car")
            setPerformance(350.0)
        }
        val botPilot = Pilot().apply {
            setName("Bot Pilot")
            setSkill(55)
            setSalary(0.0)
        }
        val opponent = OpponentTeam().apply {
            setName("Bot Team")
            setCar(botCar)
            setPilot(botPilot)
        }
        val track = Track().apply {
            setName("Test Track")
            setLength(5.0)
            setStraightsRatio(0.6)
            setCornersRatio(0.4)
            setElevationChange(12.0)
        }

        val engine = SurvivalRaceEngine(
            track = track,
            weather = Weather.SUNNY,
            playerCar = playerCar,
            playerPilot = playerPilot,
            opponents = listOf(opponent),
            random = object : SurvivalRandom {
                override fun nextDouble(): Double = 1.0
            }
        )

        val targetIndex = engine.getStandings().indexOfFirst { !it.isPlayer }
        assertTrue(targetIndex >= 0)

        val result = engine.performPlayerCompromisingEvidence(targetIndex, 7)

        assertTrue(result.logs.any { it.contains("used compromising evidence") })
        assertTrue(result.logs.any { it.contains("Turn 1 completed after compromising evidence.") })
    }
}

