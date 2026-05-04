package com.bmstu.iu3.automanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bmstu.iu3.automanagement.R.font.press_start2p
import com.bmstu.iu3.automanagement.data.GameSaveManager
import com.bmstu.iu3.automanagement.data.GameState
import com.bmstu.iu3.automanagement.race.ClassicRaceSessionStore
import com.bmstu.iu3.automanagement.ui.theme.PixelButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaceProgressScreen(
    saveManager: GameSaveManager,
    onFinished: () -> Unit,
    onBack: () -> Unit
) {
    val pixelFont = FontFamily(Font(press_start2p))
    val state = ClassicRaceSessionStore.state.collectAsState().value

    LaunchedEffect(state.finished) {
        if (state.finished && state.hasResult) {
            saveManager.saveGame(GameState.getCurrentPlayer())
            onFinished()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Race In Progress", fontFamily = pixelFont, fontSize = 14.sp) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = if (state.isRunning) "The race is live..." else "Awaiting race result...",
                fontFamily = pixelFont,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (state.logLines.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("No live log yet", fontFamily = pixelFont, fontSize = 10.sp)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        items(state.logLines) { line ->
                            Text(text = line, fontFamily = pixelFont, fontSize = 7.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PixelButton(text = "BACK", onClick = onBack, modifier = Modifier.weight(1f), baseColor = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

