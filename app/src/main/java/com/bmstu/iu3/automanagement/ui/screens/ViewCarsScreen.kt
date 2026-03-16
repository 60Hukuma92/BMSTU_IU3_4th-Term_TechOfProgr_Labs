package com.bmstu.iu3.automanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.bmstu.iu3.automanagement.data.GameState
import com.bmstu.iu3.automanagement.R.font.press_start2p
import com.bmstu.iu3.automanagement.ui.theme.CarCard
import com.bmstu.iu3.automanagement.ui.theme.PixelButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewCarsScreen(onBack: () -> Unit) {
    val components = GameState.getOwnedComponents()
    val cars = GameState.getAssembledCars()

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    "Your Cars",
                    fontFamily = FontFamily(Font(press_start2p))
                )
            })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                item {
                Text(
                    text = "Cars:",
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily(Font(press_start2p)),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
                if (cars.isNotEmpty()) {
                    items(cars) { car ->
                        CarCard(car)
                    }
                } else {
                    item {
                        Text(
                            text = "No cars assembled yet.",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily(Font(press_start2p)),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            PixelButton(
                text = "Back to Menu",
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}