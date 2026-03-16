package com.bmstu.iu3.automanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.bmstu.iu3.automanagement.data.GameState
import com.bmstu.iu3.automanagement.R.font.press_start2p
import com.bmstu.iu3.automanagement.ui.theme.PixelButton
import com.bmstu.iu3.automanagement.ui.theme.WorkerCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewPersonnelScreen(onBack: () -> Unit) {
    val engineers = GameState.getHiredEngineers()
    val pilots = GameState.getHiredPilots()

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    "Your Personnel",
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
                        "Engineers:",
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = FontFamily(Font(press_start2p)),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                if (engineers.isNotEmpty()) {
                    items(engineers) { engineer ->
                        WorkerCard(
                            name = engineer.getName(),
                            role = "Engineer",
                            skill = engineer.getSkill()
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "No engineers hired yet.",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily(Font(press_start2p)),
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Pilots:",
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = FontFamily(Font(press_start2p)),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (pilots.isNotEmpty()) {
                    items(pilots) { pilot ->
                        WorkerCard(
                            name = pilot.getName(),
                            role = "Pilot",
                            skill = pilot.getSkill()
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "No pilots hired yet.",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily(Font(press_start2p)),
                        )
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
