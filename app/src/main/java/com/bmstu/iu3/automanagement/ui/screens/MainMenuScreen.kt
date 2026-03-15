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
import com.bmstu.iu3.automanagement.R.font.press_start2p
import com.bmstu.iu3.automanagement.ui.theme.PixelButton

@Composable
fun MainMenuScreen(onNavigate: (String) -> Unit, onExit: () -> Unit) {
    val menuItems = listOf(
        Screen.StartRace,
        Screen.BuyComponents,
        Screen.AssembleCar,
        Screen.HireEngineers,
        Screen.HirePilots,
        Screen.ViewCars,
        Screen.ViewPersonnel,
        Screen.ViewStats,
        Screen.ViewOtherTeams,
        Screen.ViewOtherResults
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Auto Management", style = MaterialTheme.typography.headlineMedium, fontFamily = FontFamily(Font(press_start2p)))
        Spacer(modifier = Modifier.height(24.dp))
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
            items(menuItems) { screen ->
                PixelButton(
                    text = screen.title,
                    onClick = { onNavigate(screen.route) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            }
            item {
                PixelButton(
                    text = "Exit",
                    onClick = onExit,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    baseColor = MaterialTheme.colorScheme.error
                )
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
        Text(text = title, style = MaterialTheme.typography.headlineMedium, fontFamily = FontFamily(Font(press_start2p)))
        Spacer(modifier = Modifier.height(24.dp))
        PixelButton(
            text = "Back to Menu",
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
