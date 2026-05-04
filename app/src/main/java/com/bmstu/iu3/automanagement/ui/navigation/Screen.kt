package com.bmstu.iu3.automanagement.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object PlayerSelection : Screen("player_selection", "Select Player")
    object MainMenu : Screen("main_menu", "Main Menu")
    object StartRace : Screen("start_race", "Start Race")
    object RaceProgress : Screen("race_progress", "Race Progress")
    object BuyComponents : Screen("buy_components", "Buy Components")
    object Garage : Screen("garage", "Garage")
    object HireEngineers : Screen("hire_engineers", "Hire Engineers")
    object HirePilots : Screen("hire_pilots", "Hire Pilots")
    object ManageTracks : Screen("manage_tracks", "Manage Tracks")
    object ViewCars : Screen("view_cars", "View Cars")
    object ViewPersonnel : Screen("view_personnel", "View Personnel")
    object ViewStats : Screen("view_stats", "Race Statistics")
    object ViewTeams : Screen("view_teams", "Championship")
    object ViewResults : Screen("view_results", "Championship Results")
}
