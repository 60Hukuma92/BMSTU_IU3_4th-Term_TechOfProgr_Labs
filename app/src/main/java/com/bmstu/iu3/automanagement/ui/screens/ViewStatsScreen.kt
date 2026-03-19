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
import androidx.compose.ui.unit.sp
import com.bmstu.iu3.automanagement.R.font.press_start2p
import com.bmstu.iu3.automanagement.data.GameState
import com.bmstu.iu3.automanagement.ui.theme.PixelButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewStatsScreen(onBack: () -> Unit) {
    val pixelFont = FontFamily(Font(press_start2p))
    val raceHistory = GameState.getRaceHistory()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Race Statistics", fontFamily = pixelFont, fontSize = 14.sp) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (raceHistory.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No races completed yet", fontFamily = pixelFont, fontSize = 10.sp)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(raceHistory) { results ->
                        val playerResult = results.find { it.getTeamName() == "YOU" }
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("RACE #${raceHistory.indexOf(results) + 1}", fontFamily = pixelFont, fontSize = 10.sp)
                                    Text("Pos: ${playerResult?.getPosition() ?: "DNF"}", fontFamily = pixelFont, fontSize = 8.sp, color = MaterialTheme.colorScheme.primary)
                                }
                                Text(
                                    text = playerResult?.getTimeFormatted() ?: "--:--",
                                    fontFamily = pixelFont,
                                    fontSize = 10.sp
                                )
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
