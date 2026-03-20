package com.bmstu.iu3.automanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bmstu.iu3.automanagement.data.GameState
import com.bmstu.iu3.automanagement.R.font.press_start2p
import com.bmstu.iu3.automanagement.ui.theme.PixelButton
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewTeamsScreen(onBack: () -> Unit) {
    val opponents = GameState.getOpponentTeams()
    val myCars = GameState.getAssembledCars()
    val pixelFont = FontFamily(Font(press_start2p))

    LaunchedEffect(Unit) {
        GameState.generateOpponents()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Championship Standings", fontFamily = pixelFont, fontSize = 12.sp) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    Text("YOU:", fontFamily = pixelFont, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Player Team", fontFamily = pixelFont, fontSize = 12.sp)
                            val bestCarPerf = myCars.maxByOrNull { it.getPerformance() }?.getPerformance() ?: 0.0
                            Text("Best Car: ${String.format(Locale.US, "%.1f", bestCarPerf)}", fontFamily = pixelFont, fontSize = 8.sp)
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("COMPETITORS:", fontFamily = pixelFont, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                }

                items(opponents) { team ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = team.getName(), style = MaterialTheme.typography.titleMedium, fontFamily = pixelFont, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Pilot: ${team.getPilot()?.getName() ?: "N/A"}", fontFamily = pixelFont, fontSize = 8.sp)
                            val perf = team.getCar()?.getPerformance() ?: 0.0
                            Text("Performance: ${String.format(Locale.US, "%.1f", perf)}", fontFamily = pixelFont, fontSize = 8.sp, color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            PixelButton(text = "Back", onClick = onBack, modifier = Modifier.fillMaxWidth())
        }
    }
}
