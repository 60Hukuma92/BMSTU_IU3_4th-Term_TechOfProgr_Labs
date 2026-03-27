package com.bmstu.iu3.automanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bmstu.iu3.automanagement.data.GameState
import com.bmstu.iu3.automanagement.R.font.press_start2p
import com.bmstu.iu3.automanagement.models.Pilot
import com.bmstu.iu3.automanagement.ui.theme.PixelButton
import com.bmstu.iu3.automanagement.ui.theme.WorkerCard
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewPersonnelScreen(onBack: () -> Unit) {
    val engineers = GameState.getHiredEngineers()
    val pilots = GameState.getHiredPilots()
    val jailedPilots = GameState.getJailedPilots()
    val pixelFont = FontFamily(Font(press_start2p))

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("STAFF", "JAIL")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Personnel", fontFamily = pixelFont, fontSize = 12.sp) })
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
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
                        text = { Text(title, fontFamily = pixelFont, fontSize = 10.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (selectedTabIndex == 0) {
                    StaffContent(engineers, pilots)
                } else {
                    JailContent(jailedPilots)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            PixelButton(text = "Back", onClick = onBack, modifier = Modifier.fillMaxWidth(), baseColor = Color.Gray)
        }
    }
}

@Composable
fun StaffContent(engineers: List<com.bmstu.iu3.automanagement.models.Engineer>, pilots: List<Pilot>) {
    val pixelFont = FontFamily(Font(press_start2p))
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { Text("ENGINEERS:", fontFamily = pixelFont, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary) }
        if (engineers.isEmpty()) {
            item { Text("No engineers hired.", fontFamily = pixelFont, fontSize = 8.sp, modifier = Modifier.padding(vertical = 8.dp)) }
        } else {
            items(engineers) { engineer ->
                WorkerCard(name = engineer.getName(), role = "Engineer", skill = engineer.getSkill())
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)); Text("PILOTS:", fontFamily = pixelFont, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary) }
        if (pilots.isEmpty()) {
            item { Text("No pilots hired.", fontFamily = pixelFont, fontSize = 8.sp, modifier = Modifier.padding(vertical = 8.dp)) }
        } else {
            items(pilots) { pilot ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(pilot.getName(), fontFamily = pixelFont, fontSize = 10.sp)
                            Text("Skill: ${pilot.getSkill()}", fontFamily = pixelFont, fontSize = 8.sp)
                            if (pilot.hasFine()) {
                                Text("FINE: ${String.format(Locale.US, "%.0f $", pilot.getFineAmount())}", color = Color.Red, fontFamily = pixelFont, fontSize = 8.sp)
                                Text("DUE IN: ${pilot.getFineDeadline()} RACES", color = Color.Red, fontFamily = pixelFont, fontSize = 6.sp)
                            }
                        }
                        if (pilot.hasFine()) {
                            PixelButton(
                                text = "PAY",
                                onClick = { GameState.payFine(pilot) },
                                baseColor = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.height(40.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JailContent(jailedPilots: List<Pilot>) {
    val pixelFont = FontFamily(Font(press_start2p))
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { Text("PRISONERS:", fontFamily = pixelFont, fontSize = 10.sp, color = Color.Red) }
        if (jailedPilots.isEmpty()) {
            item { Text("Jail is empty.", fontFamily = pixelFont, fontSize = 8.sp, modifier = Modifier.padding(vertical = 8.dp)) }
        } else {
            items(jailedPilots) { pilot ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.DarkGray)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(pilot.getName(), fontFamily = pixelFont, fontSize = 10.sp, color = Color.White)
                            Text("Skill: ${pilot.getSkill()}", fontFamily = pixelFont, fontSize = 8.sp, color = Color.LightGray)
                            Text("SENTENCE: ${pilot.getJailSentence()} RACES", color = Color.Yellow, fontFamily = pixelFont, fontSize = 8.sp)
                        }
                        
                        val bailPrice = pilot.getSalary() * 0.5
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${String.format(Locale.US, "%.0f", bailPrice)} $", color = Color.White, fontFamily = pixelFont, fontSize = 6.sp)
                            PixelButton(
                                text = "BAIL",
                                onClick = { GameState.releaseFromJail(pilot) },
                                baseColor = Color(0xFFFFA500)
                            )
                        }
                    }
                }
            }
        }
    }
}
