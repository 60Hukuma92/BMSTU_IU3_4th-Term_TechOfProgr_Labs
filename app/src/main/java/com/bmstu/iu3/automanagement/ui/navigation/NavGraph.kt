package com.bmstu.iu3.automanagement.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bmstu.iu3.automanagement.ui.screens.*

@Composable
fun SetupNavGraph(navController: NavHostController, onExit: () -> Unit) {
    NavHost(
        navController = navController,
        startDestination = Screen.MainMenu.route
    ) {
        composable(Screen.MainMenu.route) {
            MainMenuScreen(
                onNavigate = { route -> navController.navigate(route) },
                onExit = onExit
            )
        }
        composable(Screen.StartRace.route) {
            PlaceholderScreen(Screen.StartRace.title) { navController.popBackStack() }
        }
        composable(Screen.BuyComponents.route) {
            BuyComponentsScreen { navController.popBackStack() }
        }
        composable(Screen.Garage.route) {
            AssembleCarScreen { navController.popBackStack() }
        }
        composable(Screen.HireEngineers.route) {
            HireEngineersScreen { navController.popBackStack() }
        }
        composable(Screen.HirePilots.route) {
            HirePilotsScreen { navController.popBackStack() }
        }
        composable(Screen.ViewCars.route) {
            ViewCarsScreen { navController.popBackStack() }
        }
        composable(Screen.ViewPersonnel.route) {
            ViewPersonnelScreen { navController.popBackStack() }
        }
        composable(Screen.ViewStats.route) {
            PlaceholderScreen(Screen.ViewStats.title) { navController.popBackStack() }
        }
        composable(Screen.ViewOtherTeams.route) {
            PlaceholderScreen(Screen.ViewOtherTeams.title) { navController.popBackStack() }
        }
        composable(Screen.ViewOtherResults.route) {
            PlaceholderScreen(Screen.ViewOtherResults.title) { navController.popBackStack() }
        }
    }
}
