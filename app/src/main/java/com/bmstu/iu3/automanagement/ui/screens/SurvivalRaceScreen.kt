package com.bmstu.iu3.automanagement.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.bmstu.iu3.automanagement.models.*
import com.bmstu.iu3.automanagement.survival.SurvivalRaceEngine
import com.bmstu.iu3.automanagement.survival.DefaultSurvivalRandom
import com.bmstu.iu3.automanagement.ui.theme.PixelButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurvivalRaceScreen(onBack: () -> Unit, onRaceComplete: () -> Unit) {
    val pixelFont = FontFamily(Font(press_start2p))

    val tracks = GameState.getTracks()
    val myCars = GameState.getAssembledCars()
    val myPilots = GameState.getHiredPilots().filter { !it.isInJail() }

    GameState.generateOpponents()
    val opponents = GameState.getOpponentTeams()

    var selectedTrack by remember { mutableStateOf(tracks.firstOrNull()) }
    var selectedCar by remember { mutableStateOf<Car?>(null) }
    var selectedPilot by remember { mutableStateOf<Pilot?>(null) }

    var isRacing by remember { mutableStateOf(false) }
    var raceCompleted by remember { mutableStateOf(false) }
    var engine by remember { mutableStateOf<SurvivalRaceEngine?>(null) }
    val turnLogs = remember { mutableStateListOf<String>() }
    val standings = remember { mutableStateListOf<String>() }
    val listState = rememberLazyListState()
    val gameMessages = remember { mutableStateListOf<String>() }

    if (isRacing && selectedTrack != null && selectedCar != null && selectedPilot != null && engine == null) {
        engine = SurvivalRaceEngine(
            track = selectedTrack!!,
            weather = Weather.SUNNY,
            playerCar = selectedCar!!,
            playerPilot = selectedPilot!!,
            opponents = opponents.take(2 + (GameState.getBudgetObject().getAmount() / 50000).toInt().coerceAtMost(3)),
            random = DefaultSurvivalRandom()
        )
        gameMessages.add("Starting survival race on ${selectedTrack?.getName()}...")
    }

    if (isRacing && engine != null) {
        Column(modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(Color.Red))
                Spacer(Modifier.width(8.dp))
                Text("SURVIVAL MODE: ${selectedTrack?.getName()}", color = Color.White, fontFamily = pixelFont, fontSize = 12.sp)
            }
            Spacer(Modifier.height(16.dp))

            // Standings
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.DarkGray)
                    .padding(8.dp)
            ) {
                Text("STANDINGS (TURN ${engine!!.turnNumber}):", color = Color.Yellow, fontSize = 9.sp, fontFamily = pixelFont)
                engine!!.getStandings().forEachIndexed { index, competitor ->
                    Text(
                        "${index + 1}. ${competitor.name} - ${String.format("%.1f", competitor.progress)}m",
                        color = if (competitor.isPlayer) Color.Green else Color.White,
                        fontSize = 8.sp,
                        fontFamily = pixelFont
                    )
                }
                if (!engine!!.getPlayerState().alive) {
                    Text("YOU ARE ELIMINATED!", color = Color.Red, fontSize = 9.sp, fontFamily = pixelFont)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Race log
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.DarkGray)
                    .clickable(enabled = !raceCompleted) { }
            ) {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(gameMessages) { msg ->
                        Text(msg, color = Color.Yellow, fontSize = 8.sp, fontFamily = pixelFont, lineHeight = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Action buttons
            if (!raceCompleted) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (engine!!.getPlayerState().alive && !engine!!.finished) {
                        val canAttack = engine!!.getAliveOpponentsCount() > 0 &&
                            (0 until engine!!.getStandings().size).any { engine!!.canPlayerAttack(it) }

                        PixelButton(
                            text = "ATTACK",
                            onClick = {
                                val standings = engine!!.getStandings()
                                var targetIndex = -1
                                for (i in standings.indices) {
                                    if (engine!!.canPlayerAttack(i)) {
                                        targetIndex = i
                                        break
                                    }
                                }
                                if (targetIndex >= 0) {
                                    val result = engine!!.performPlayerAttack(targetIndex)
                                    gameMessages.addAll(result.logs)
                                    raceCompleted = result.finished
                                }
                            },
                            modifier = Modifier.weight(1f),
                            baseColor = if (canAttack && engine!!.getPlayerState().alive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            enabled = canAttack && engine!!.getPlayerState().alive
                        )

                        PixelButton(
                            text = "OVERTAKE",
                            onClick = {
                                val result = engine!!.performPlayerOvertake()
                                gameMessages.addAll(result.logs)
                                raceCompleted = result.finished
                            },
                            modifier = Modifier.weight(1f),
                            enabled = engine!!.getPlayerState().alive
                        )

                        PixelButton(
                            text = "EVIDENCE",
                            onClick = {
                                val standings = engine!!.getStandings()
                                if (standings.size > 1) {
                                    val result = engine!!.performPlayerCompromisingEvidence(0, 10)
                                    gameMessages.addAll(result.logs)
                                    raceCompleted = result.finished
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = engine!!.getPlayerState().alive && engine!!.getStandings().size > 1
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Result button
            if (raceCompleted) {
                PixelButton(
                    text = "VIEW RESULTS",
                    onClick = {
                        val results = engine!!.buildResults()
                        val playerName = GameState.getCurrentPlayer()
                        GameState.addRaceResult(results.map { result ->
                            val displayName = if (result.getTeamName() == "YOU") playerName else result.getTeamName()
                            result.apply {
                                setTeamName(displayName)
                            }
                        })
                        GameState.processRaceEndUpdates()
                        onRaceComplete()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (!raceCompleted && !engine!!.getPlayerState().alive) {
                PixelButton(
                    text = "BACK TO MENU",
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    baseColor = Color.Gray
                )
            }
        }

        LaunchedEffect(gameMessages.size) {
            if (gameMessages.isNotEmpty()) {
                listState.animateScrollToItem(gameMessages.size - 1)
            }
        }
    } else {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Survival Mode Preparation", fontFamily = pixelFont, fontSize = 12.sp) }) }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item { Text("SELECT TRACK:", fontFamily = pixelFont, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary) }
                    items(tracks) { track ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selectedTrack = track },
                            colors = CardDefaults.cardColors(containerColor = if (selectedTrack == track) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text("${track.getName()} (${track.getLength()} km)", modifier = Modifier.padding(8.dp), fontFamily = pixelFont, fontSize = 8.sp)
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)); Text("SELECT CAR:", fontFamily = pixelFont, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary) }
                    if (myCars.isEmpty()) {
                        item { Text("No cars assembled!", color = Color.Red, fontSize = 8.sp, fontFamily = pixelFont) }
                    } else {
                        items(myCars) { car ->
                            val isBroken = listOf(car.getEngine(), car.getGearbox(), car.getChassis()).any { it?.isDestroyed() == true }
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { if (!isBroken) selectedCar = car },
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        selectedCar == car -> MaterialTheme.colorScheme.primaryContainer
                                        isBroken -> MaterialTheme.colorScheme.errorContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(car.getName(), fontFamily = pixelFont, fontSize = 8.sp)
                                    if (isBroken) Text("BROKEN!", color = Color.Red, fontSize = 6.sp, fontFamily = pixelFont)
                                    val hasWeapons = (car.getMeleeWeapon1() != null || car.getMeleeWeapon2() != null || car.getRangedWeapon() != null)
                                    if (!hasWeapons) Text("NO WEAPONS!", color = Color.Yellow, fontSize = 6.sp, fontFamily = pixelFont)
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)); Text("SELECT PILOT:", fontFamily = pixelFont, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary) }
                    if (myPilots.isEmpty()) {
                        item { Text("NO PILOTS AVAILABLE!", color = Color.Red, fontSize = 8.sp, fontFamily = pixelFont) }
                    } else {
                        items(myPilots) { pilot ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selectedPilot = pilot },
                                colors = CardDefaults.cardColors(containerColor = if (selectedPilot == pilot) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text("${pilot.getName()} (Skill: ${pilot.getSkill()})", modifier = Modifier.padding(8.dp), fontFamily = pixelFont, fontSize = 8.sp)
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                        Text("TIPS:", fontFamily = pixelFont, fontSize = 9.sp, color = Color.Yellow)
                        Text(
                            "• Attack opponents to eliminate them\n" +
                            "• Overtake to gain progress\n" +
                            "• Use compromising evidence to slow rivals\n" +
                            "• Have weapons to participate!",
                            fontFamily = pixelFont,
                            fontSize = 7.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                PixelButton(
                    text = "START SURVIVAL",
                    onClick = {
                        if (selectedCar != null && selectedPilot != null && selectedTrack != null) {
                            val hasWeapons = (selectedCar!!.getMeleeWeapon1() != null ||
                                            selectedCar!!.getMeleeWeapon2() != null ||
                                            selectedCar!!.getRangedWeapon() != null)
                            if (!hasWeapons) {
                                gameMessages.clear()
                                gameMessages.add("Error: Car has no weapons! Cannot participate in survival mode.")
                            } else {
                                isRacing = true
                                raceCompleted = false
                                gameMessages.clear()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                PixelButton(text = "Back", onClick = onBack, modifier = Modifier.fillMaxWidth(), baseColor = Color.Gray)
            }
        }
    }
}
