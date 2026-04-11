package com.bmstu.iu3.automanagement.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.bmstu.iu3.automanagement.data.GameState
import com.bmstu.iu3.automanagement.R.font.press_start2p
import com.bmstu.iu3.automanagement.models.Car
import com.bmstu.iu3.automanagement.models.Component
import com.bmstu.iu3.automanagement.ui.theme.CarCard
import com.bmstu.iu3.automanagement.ui.theme.PixelButton
import com.bmstu.iu3.automanagement.ui.theme.buildComponentStatsText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewCarsScreen(onBack: () -> Unit) {
    val cars = GameState.getAssembledCars()
    val components = GameState.getOwnedComponents()
    var selectedCar by remember(cars) { mutableStateOf(cars.firstOrNull()) }

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
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedCar = car },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedCar == car) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                        ) {
                            CarCard(car)
                        }
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

                selectedCar?.let { car ->
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Installed on ${car.getName()}:",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily(Font(press_start2p))
                        )
                    }
                    items(car.getAllInstalledComponents()) { component ->
                        ComponentOnCarRow(car = car, component = component)
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Install from inventory:",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily(Font(press_start2p))
                        )
                    }
                    items(components) { component ->
                        InventoryInstallRow(car = car, component = component)
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

@Composable
private fun ComponentOnCarRow(car: Car, component: Component) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(component.getName(), style = MaterialTheme.typography.bodySmall)
                Text(buildComponentStatsText(component), style = MaterialTheme.typography.labelSmall)
            }
            PixelButton(
                text = "REMOVE",
                onClick = { GameState.uninstallComponentFromCar(car, component) },
                baseColor = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun InventoryInstallRow(car: Car, component: Component) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(component.getName(), style = MaterialTheme.typography.bodySmall)
                Text(buildComponentStatsText(component), style = MaterialTheme.typography.labelSmall)
            }
            PixelButton(
                text = "INSTALL",
                onClick = { GameState.installComponentToCar(car, component) },
                baseColor = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}
