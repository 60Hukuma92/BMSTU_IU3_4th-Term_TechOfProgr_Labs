package com.bmstu.iu3.automanagement.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bmstu.iu3.automanagement.models.GarageViewModel
import com.bmstu.iu3.automanagement.R.font.press_start2p
import com.bmstu.iu3.automanagement.ui.theme.ComponentCard
import com.bmstu.iu3.automanagement.ui.theme.PixelButton
import com.bmstu.iu3.automanagement.ui.theme.SlotItem
import com.bmstu.iu3.automanagement.data.GameState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssembleCarScreen(onBack: () -> Unit) {
    val viewModel: GarageViewModel = viewModel()
    val inventory = viewModel.inventory
    val engineers = viewModel.hiredEngineers
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("ASSEMBLE", "WORKSHOP")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Garage", fontFamily = FontFamily(Font(press_start2p))) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = { HorizontalDivider(thickness = 2.dp) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontFamily = FontFamily(Font(press_start2p)), fontSize = 10.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTabIndex == 0) {
                AssembleContent(viewModel, inventory, engineers, onBack)
            } else {
                WorkshopContent(viewModel, inventory, engineers, onBack)
            }
        }
    }
}

@Composable
fun AssembleContent(viewModel: GarageViewModel, inventory: List<com.bmstu.iu3.automanagement.models.Component>, engineers: List<com.bmstu.iu3.automanagement.models.Engineer>, onBack: () -> Unit) {
    Column {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Configuration:", style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily(Font(press_start2p)))
                Spacer(modifier = Modifier.height(8.dp))
                SlotItem("Engine", viewModel.selectedEngine.value?.getName())
                SlotItem("Gearbox", viewModel.selectedGearbox.value?.getName())
                SlotItem("Chassis", viewModel.selectedChassis.value?.getName())
                SlotItem("Suspension", viewModel.selectedSuspension.value?.getName())
                SlotItem("Aero", viewModel.selectedAero.value?.getName())
                SlotItem("Tyres", viewModel.selectedTyres.value?.getName())
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                SlotItem("Engineer", viewModel.selectedEngineer.value?.getName())
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            item { Text("Engineer:", fontFamily = FontFamily(Font(press_start2p)), style = MaterialTheme.typography.bodySmall) }
            items(engineers) { eng ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { viewModel.selectEngineer(eng) },
                    colors = CardDefaults.cardColors(containerColor = if (viewModel.selectedEngineer.value == eng) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text("${eng.getName()} (Skill: ${eng.getSkill()})", modifier = Modifier.padding(8.dp), fontFamily = FontFamily(Font(press_start2p)), fontSize = 10.sp)
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)); Text("Components:", fontFamily = FontFamily(Font(press_start2p)), style = MaterialTheme.typography.bodySmall) }
            items(inventory) { component ->
                val isSelected = component == viewModel.selectedEngine.value || component == viewModel.selectedGearbox.value ||
                                 component == viewModel.selectedChassis.value || component == viewModel.selectedSuspension.value ||
                                 component == viewModel.selectedAero.value || component == viewModel.selectedTyres.value
                ComponentCard(component = component, isSelected = isSelected) { viewModel.selectComponent(component) }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            PixelButton(text = "Back", onClick = onBack, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            PixelButton(text = "ASSEMBLE", onClick = { viewModel.assemble() }, modifier = Modifier.weight(1f), baseColor = MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
fun WorkshopContent(viewModel: GarageViewModel, inventory: List<com.bmstu.iu3.automanagement.models.Component>, engineers: List<com.bmstu.iu3.automanagement.models.Engineer>, onBack: () -> Unit) {
    Column {
        Text("Workshop:", style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily(Font(press_start2p)))
        
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Assigned Engineer:", style = MaterialTheme.typography.labelLarge, fontFamily = FontFamily(Font(press_start2p)))
                Text(
                    text = viewModel.selectedEngineer.value?.let { "${it.getName()} (Discount ready)" } ?: "No one selected",
                    color = if (viewModel.selectedEngineer.value != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontFamily = FontFamily(Font(press_start2p)),
                    fontSize = 10.sp
                )
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            item { 
                Text("Select Engineer for discount:", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily(Font(press_start2p)))
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            items(engineers) { eng ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { viewModel.selectEngineer(eng) },
                    colors = CardDefaults.cardColors(containerColor = if (viewModel.selectedEngineer.value == eng) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text("${eng.getName()} (Skill: ${eng.getSkill()})", modifier = Modifier.padding(8.dp), fontFamily = FontFamily(Font(press_start2p)), fontSize = 10.sp)
                }
            }

            item { 
                Spacer(modifier = Modifier.height(16.dp))
                Text("Components to fix:", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily(Font(press_start2p)))
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(inventory) { component ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(component.getName(), fontFamily = FontFamily(Font(press_start2p)), fontSize = 10.sp)
                            com.bmstu.iu3.automanagement.ui.theme.WearIndicator(component.getWear())
                        }
                        
                        if (component.getWear() > 0) {
                            // Расчет цены для отображения в UI
                            val engineer = viewModel.selectedEngineer.value
                            var repairCost = component.getPrice() * 0.3 * component.getWear()
                            engineer?.let {
                                val discount = it.getSkill() / 200.0
                                repairCost *= (1.0 - discount)
                            }

                            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                                Text(
                                    text = String.format(Locale.US, "%.2f $", repairCost),
                                    fontFamily = FontFamily(Font(press_start2p)),
                                    fontSize = 8.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                PixelButton(
                                    text = "FIX",
                                    onClick = { GameState.repairComponent(component, viewModel.selectedEngineer.value) },
                                    baseColor = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        PixelButton(text = "Back", onClick = onBack, modifier = Modifier.fillMaxWidth())
    }
}
