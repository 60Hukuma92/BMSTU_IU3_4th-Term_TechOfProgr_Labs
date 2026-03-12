package com.bmstu.iu3.automanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.bmstu.iu3.automanagement.ui.navigation.Screen
import com.bmstu.iu3.automanagement.R.font.game_font

@Composable
fun MainMenuScreen(onNavigate: (String) -> Unit, onExit: () -> Unit) {
    val menuItems = listOf(
        Screen.StartRace,
        Screen.BuyComponents,
        Screen.AssembleCar,
        Screen.HirePersonnel,
        Screen.HirePilot,
        Screen.ViewCars,
        Screen.ViewPilots,
        Screen.ViewStats,
        Screen.ViewOtherTeams,
        Screen.ViewOtherResults
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Auto Management", style = MaterialTheme.typography.headlineMedium, fontFamily = FontFamily(Font(game_font)))
        Spacer(modifier = Modifier.height(24.dp))
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
            items(menuItems) { screen ->
                Button(
                    onClick = { onNavigate(screen.route) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(text = screen.title)
                }
            }
            item { //366x68 approximately
                Button(
                    onClick = onExit,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(text = "Exit")
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium, fontFamily = FontFamily(Font(game_font)))
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack) {
            Text(text = "Back to Menu")
        }
    }
}
