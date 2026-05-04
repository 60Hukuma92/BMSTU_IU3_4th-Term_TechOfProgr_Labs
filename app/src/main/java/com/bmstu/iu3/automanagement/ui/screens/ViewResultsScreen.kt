package com.bmstu.iu3.automanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bmstu.iu3.automanagement.R.font.press_start2p
import com.bmstu.iu3.automanagement.data.GameState
import com.bmstu.iu3.automanagement.models.RaceResult
import java.util.Locale
import com.bmstu.iu3.automanagement.ui.theme.PixelButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewResultsScreen(onBack: () -> Unit) {
    val pixelFont = FontFamily(Font(press_start2p))
    val lastRaceResults = GameState.getRaceHistory().firstOrNull() ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Race Results", fontFamily = pixelFont, fontSize = 14.sp) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (lastRaceResults.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("No race data available", fontFamily = pixelFont, fontSize = 10.sp)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(lastRaceResults) { result ->
                        ResultCard(result, pixelFont)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            PixelButton(text = "Back to Menu", onClick = onBack, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun ResultCard(result: RaceResult, font: FontFamily) {
    val isPlayer = result.getTeamName() == "YOU"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlayer) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${result.getPosition()}.",
                fontFamily = font,
                fontSize = 12.sp,
                modifier = Modifier.width(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = result.getTeamName(), fontFamily = font, fontSize = 10.sp)
                if (result.getIncident() != null) {
                    Text(
                        text = "INCIDENT: ${result.getIncident()?.getSeverity()}",
                        fontFamily = font,
                        fontSize = 8.sp,
                        color = Color.Red
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (result.getTime() > 900000) "DNF" else result.getTimeFormatted(),
                    fontFamily = font,
                    fontSize = 10.sp
                )
                if (result.getPrizeMoney() > 0) {
                    Text(
                        text = "+${String.format(Locale.US, "%.2f", result.getPrizeMoney())} $",
                        fontFamily = font,
                        fontSize = 8.sp,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}


