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
    val listState = rememberLazyListState()

    // Авто-скролл к последнему сообщению комментатора
    LaunchedEffect(state.logLines.size) {
        if (state.logLines.isNotEmpty()) {
            listState.animateScrollToItem(state.logLines.size - 1)
        }
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(Color.Red))
                        Spacer(Modifier.width(8.dp))
                        Text("LIVE BROADCAST", fontFamily = pixelFont, fontSize = 14.sp)
                    }
                },
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
            // ТАБЛО ЛИДЕРА
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("CURRENT LEADER:", color = Color.Gray, fontSize = 8.sp, fontFamily = pixelFont)
                    Text(
                        text = if (state.isRunning) "TRACKING..." else "RACE OVER", 
                        color = Color.Yellow, 
                        fontSize = 12.sp, 
                        fontFamily = pixelFont
                    )
                }
            }

            // ГЛАВНЫЙ ЭКРАН КОММЕНТАРИЕВ
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.logLines) { line ->
                    CommentaryRow(line, pixelFont)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PixelButton(
                    text = "ABORT", 
                    onClick = onBack, 
                    modifier = Modifier.weight(1f), 
                    baseColor = Color.DarkGray,
                    fontSize = 10.sp
                )
                if (state.finished) {
                    PixelButton(
                        text = "RESULTS", 
                        onClick = onFinished, 
                        modifier = Modifier.weight(1f),
                        baseColor = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CommentaryRow(line: String, font: FontFamily) {
    val isIncident = line.contains("DRAMA") || line.contains("OFF") || line.contains("RETIRED")
    val textColor = if (isIncident) Color.Red else Color.White
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = line,
            color = textColor,
            fontFamily = font,
            fontSize = 11.sp, // Крупный читаемый шрифт
            lineHeight = 16.sp
        )
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = Color.DarkGray, thickness = 0.5.dp)
    }
}
