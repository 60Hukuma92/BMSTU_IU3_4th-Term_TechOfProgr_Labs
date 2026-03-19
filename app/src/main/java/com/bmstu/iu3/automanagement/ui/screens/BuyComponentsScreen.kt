package com.bmstu.iu3.automanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bmstu.iu3.automanagement.R.font.press_start2p
import com.bmstu.iu3.automanagement.models.*
import com.bmstu.iu3.automanagement.ui.theme.PixelButton
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyComponentsScreen(onBack: () -> Unit) {
    val marketViewModel: MarketViewModel = viewModel()
    val allComponents = marketViewModel.availableComponents
    val pixelFont = FontFamily(Font(press_start2p))

    val tabs = listOf("ENGINES", "GEARBOX", "CHASSIS", "SUSP", "AERO", "TYRES")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val filteredComponents = remember(selectedTabIndex, allComponents) {
        when (selectedTabIndex) {
            0 -> allComponents.filterIsInstance<Engine>()
            1 -> allComponents.filterIsInstance<Gearbox>()
            2 -> allComponents.filterIsInstance<Chassis>()
            3 -> allComponents.filterIsInstance<Suspension>()
            4 -> allComponents.filterIsInstance<Aerodynamics>()
            5 -> allComponents.filterIsInstance<Tyres>()
            else -> allComponents
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Market", fontFamily = pixelFont, fontSize = 12.sp) })
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 0.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = { HorizontalDivider(thickness = 2.dp) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontFamily = pixelFont, fontSize = 8.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                if (filteredComponents.isNotEmpty()) {
                    items(filteredComponents) { component ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = component.getName(), fontFamily = pixelFont, fontSize = 10.sp)
                                    // ИСПРАВЛЕНО: Два знака после запятой
                                    Text(
                                        text = String.format(Locale.US, "%.2f $", component.getPrice()),
                                        fontFamily = pixelFont,
                                        fontSize = 8.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    val statsText = when(component) {
                                        is Engine -> "Power: ${component.getPower()} | ${component.getType()}"
                                        is Gearbox -> "Type: ${component.getType()}"
                                        is Chassis -> "Susp: ${component.getSuspensionType()}"
                                        else -> "Perf: ${String.format(Locale.US, "%.1f", component.getPerformance())}"
                                    }
                                    Text(text = statsText, fontSize = 6.sp, fontFamily = pixelFont)
                                }
                                PixelButton(text = "BUY", onClick = { marketViewModel.buyComponent(component) }, baseColor = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            PixelButton(text = "Back", onClick = onBack, modifier = Modifier.fillMaxWidth())
        }
    }
}
