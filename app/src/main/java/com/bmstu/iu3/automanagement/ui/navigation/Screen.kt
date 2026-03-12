package com.bmstu.iu3.automanagement.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object MainMenu : Screen("main_menu", "Main Menu")
    object StartRace : Screen("start_race", "Start Race")
    object BuyComponents : Screen("buy_components", "Buy Components")
    object AssembleCar : Screen("assemble_car", "Assemble Car")
    object HirePersonnel : Screen("hire_personnel", "Hire Engineers")
    object HirePilot : Screen("hire_pilot", "Hire Pilot")
    object ViewCars : Screen("view_cars", "View Cars")
    object ViewPilots : Screen("view_pilots", "View Pilots")
    object ViewStats : Screen("view_stats", "Race Statistics")
    object ViewOtherTeams : Screen("view_teams", "Other Teams")
    object ViewOtherResults : Screen("view_results", "Other Results")
}