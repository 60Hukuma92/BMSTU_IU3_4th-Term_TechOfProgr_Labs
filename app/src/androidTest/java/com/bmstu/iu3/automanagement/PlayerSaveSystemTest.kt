package com.bmstu.iu3.automanagement

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bmstu.iu3.automanagement.data.GameSaveManager
import com.bmstu.iu3.automanagement.data.GameState
import com.bmstu.iu3.automanagement.models.Track
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayerSaveSystemTest {

    private lateinit var saveManager: GameSaveManager

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
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
}

