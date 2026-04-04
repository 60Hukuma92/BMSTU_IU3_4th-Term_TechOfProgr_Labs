package com.bmstu.iu3.automanagement

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bmstu.iu3.automanagement.data.GameSaveManager
import com.bmstu.iu3.automanagement.data.GameState
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
        // Очищаем все предыдущие сохранения для чистоты тестов
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

        // Set initial budget
        val testBudget = 5000.0
        GameState.setBudget(testBudget)
        GameState.setCurrentPlayer(playerName)

        // Save the game
        saveManager.saveGame(playerName)

        // Change state to verify load works
        GameState.setBudget(0.0)

        // Load the game
        val success = saveManager.loadGame(playerName)
        assertTrue("Game should load successfully", success)
        assertEquals("Budget should be restored", testBudget, GameState.getBudgetObject().getAmount(), 0.1)
    }

    @Test
    fun testMultiplePlayerProfiles() {
        val player1 = "Player1"
        val player2 = "Player2"

        // Create first player
        saveManager.createNewGame(player1)
        GameState.setBudget(3000.0)
        GameState.setCurrentPlayer(player1)
        saveManager.saveGame(player1)

        // Create second player
        saveManager.createNewGame(player2)
        GameState.setBudget(7000.0)
        GameState.setCurrentPlayer(player2)
        saveManager.saveGame(player2)

        // Verify both players exist
        val players = saveManager.getAllPlayers()
        assertTrue("Player1 should exist", players.contains(player1))
        assertTrue("Player2 should exist", players.contains(player2))

        // Load first player and verify budget
        saveManager.loadGame(player1)
        assertEquals("Player1 budget should be 3000", 3000.0, GameState.getBudgetObject().getAmount(), 0.1)

        // Load second player and verify budget
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
}

