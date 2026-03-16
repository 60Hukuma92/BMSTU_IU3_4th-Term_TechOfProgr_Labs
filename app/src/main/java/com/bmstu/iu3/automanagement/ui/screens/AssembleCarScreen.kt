package com.bmstu.iu3.automanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bmstu.iu3.automanagement.models.AssembleCarViewModel
import com.bmstu.iu3.automanagement.R.font.press_start2p
import com.bmstu.iu3.automanagement.ui.theme.ComponentCard
import com.bmstu.iu3.automanagement.ui.theme.PixelButton
import com.bmstu.iu3.automanagement.ui.theme.SlotItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssembleCarScreen(onBack: () -> Unit) {
    val viewModel: AssembleCarViewModel = viewModel()
    val inventory = viewModel.inventory

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Assemble Car", fontFamily = FontFamily(Font(press_start2p))) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Selected Configuration:", style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily(Font(press_start2p)))
                    Spacer(modifier = Modifier.height(8.dp))

                    SlotItem("Engine", viewModel.selectedEngine.value?.getName())
                    SlotItem("Gearbox", viewModel.selectedGearbox.value?.getName())
                    SlotItem("Chassis", viewModel.selectedChassis.value?.getName())
                    SlotItem("Suspension", viewModel.selectedSuspension.value?.getName())
                    SlotItem("Aerodynamics", viewModel.selectedAero.value?.getName())
                    SlotItem("Tyres", viewModel.selectedTyres.value?.getName())
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Your Inventory:", style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily(Font(press_start2p)))

            LazyColumn(modifier = Modifier.weight(1f)) {
                if (inventory.isNotEmpty()) {
                    items(inventory) { component ->
                        ComponentCard(component) {
                            viewModel.selectComponent(component)
                        }
                    }
                } else {
                    item {
                        Text(
                            "No components in inventory",
                            fontFamily = FontFamily(Font(press_start2p)),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    PixelButton(text = "Back", onClick = onBack, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    PixelButton(
                        text = "ASSEMBLE",
                        onClick = { viewModel.assemble() },
                        modifier = Modifier.weight(1f),
                        baseColor = MaterialTheme.colorScheme.tertiary
                    )
                }
        }
    }
}
