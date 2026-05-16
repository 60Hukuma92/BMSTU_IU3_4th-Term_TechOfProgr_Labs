package com.bmstu.iu3.automanagement.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.bmstu.iu3.automanagement.ui.theme.PixelButton
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewResultsScreen(onBack: () -> Unit) {
    val pixelFont = FontFamily(Font(press_start2p))
    val lastRaceResults = GameState.getRaceHistory().firstOrNull() ?: emptyList()
    val commentary = GameState.getLastRaceCommentary()
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("STANDINGS", "COMMENTARY")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CHAMPIONSHIP RESULTS", fontFamily = pixelFont, fontSize = 14.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
                .padding(16.dp)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Black,
                contentColor = Color.Yellow,
                divider = { HorizontalDivider(color = Color.DarkGray) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontFamily = pixelFont, fontSize = 10.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (selectedTabIndex == 0) {
                    StandingsTab(lastRaceResults, pixelFont)
                } else {
                    CommentaryTab(commentary, pixelFont)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            PixelButton(
                text = "RETURN TO HUB", 
                onClick = onBack, 
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun StandingsTab(results: List<RaceResult>, font: FontFamily) {
    val playerName = GameState.getCurrentPlayer()
    if (results.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No standings data", color = Color.White, fontFamily = font, fontSize = 10.sp)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(results) { result ->
                val isPlayer = result.getTeamName() == playerName || (playerName.isEmpty() && result.getTeamName() == "YOU")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPlayer) Color(0xFF2E7D32) else Color(0xFF1A1A1A)
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
                            color = Color.White,
                            modifier = Modifier.width(32.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = result.getTeamName(), color = Color.White, fontFamily = font, fontSize = 10.sp)
                            val incident = result.getIncident()
                            if (incident != null) {
                                Text(
                                    text = incident.getReason(),
                                    color = Color.Red,
                                    fontFamily = font,
                                    fontSize = 8.sp
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            val time = result.getTime()
                            Text(
                                text = if (time > 900000) "DNF" else result.getTimeFormatted(),
                                color = Color.White,
                                fontFamily = font,
                                fontSize = 10.sp
                            )
                            if (result.getPrizeMoney() > 0) {
                                Text(
                                    text = "+${String.format(Locale.US, "%.0f", result.getPrizeMoney())} $",
                                    color = Color.Yellow,
                                    fontFamily = font,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentaryTab(commentary: List<com.bmstu.iu3.automanagement.models.CommentatorMessage>, font: FontFamily) {
    val listState = rememberLazyListState()
    LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(commentary) { msg ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Row {
                    Text(text = msg.displayTime, color = Color.Gray, fontFamily = font, fontSize = 8.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = msg.source.uppercase(), color = Color.Cyan, fontFamily = font, fontSize = 8.sp)
                }
                Text(
                    text = msg.message,
                    color = Color.White,
                    fontFamily = font,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
                HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
