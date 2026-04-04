package com.bmstu.iu3.automanagement.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bmstu.iu3.automanagement.data.GameState
import com.bmstu.iu3.automanagement.data.GameSaveManager
import com.bmstu.iu3.automanagement.ui.screens.*

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    onExit: () -> Unit,
    saveManager: GameSaveManager
) {
    NavHost(
        navController = navController,
        startDestination = Screen.PlayerSelection.route
    ) {
        composable(Screen.PlayerSelection.route) {
            PlayerSelectionScreen(
                saveManager = saveManager,
                onPlayerSelected = {
                    navController.navigate(Screen.MainMenu.route) {
                        popUpTo(Screen.PlayerSelection.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.MainMenu.route) {
            MainMenuScreen(
                onNavigate = { route -> navController.navigate(route) },
                onExit = {
                    saveManager.saveGame(GameState.getCurrentPlayer())
                    onExit()
                }
            )
        }
        composable(Screen.StartRace.route) {
            StartRaceScreen(
                onBack = { navController.popBackStack() },
                onRaceComplete = {
                    saveManager.saveGame(GameState.getCurrentPlayer())
                    navController.navigate(Screen.ViewResults.route) {
                        popUpTo(Screen.MainMenu.route)
                    }
                }
            )
        }
        composable(Screen.BuyComponents.route) {
            BuyComponentsScreen {
                saveManager.saveGame(GameState.getCurrentPlayer())
                navController.popBackStack()
            }
        }
        composable(Screen.Garage.route) {
            AssembleCarScreen {
                saveManager.saveGame(GameState.getCurrentPlayer())
                navController.popBackStack()
            }
        }
        composable(Screen.HireEngineers.route) {
            HireEngineersScreen {
                saveManager.saveGame(GameState.getCurrentPlayer())
                navController.popBackStack()
            }
        }
        composable(Screen.HirePilots.route) {
            HirePilotsScreen {
                saveManager.saveGame(GameState.getCurrentPlayer())
                navController.popBackStack()
            }
        }
        composable(Screen.ViewCars.route) {
            ViewCarsScreen { navController.popBackStack() }
        }
        composable(Screen.ViewPersonnel.route) {
            ViewPersonnelScreen { navController.popBackStack() }
        }
        composable(Screen.ViewStats.route) {
            ViewStatsScreen { navController.popBackStack() }
        }
        composable(Screen.ViewTeams.route) {
            ViewTeamsScreen { navController.popBackStack() }
        }
        composable(Screen.ViewResults.route) {
            ViewResultsScreen { navController.popBackStack() }
        }
    }
}
