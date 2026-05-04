package com.bmstu.iu3.automanagement.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import java.util.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bmstu.iu3.automanagement.R.font.press_start2p
import com.bmstu.iu3.automanagement.data.GameSaveManager
import com.bmstu.iu3.automanagement.data.GameState
import com.bmstu.iu3.automanagement.models.*
import com.bmstu.iu3.automanagement.ui.theme.PixelButton
import com.bmstu.iu3.automanagement.race.ClassicRaceSessionStore
import com.bmstu.iu3.automanagement.utils.ComponentComparator
import com.bmstu.iu3.automanagement.survival.DefaultSurvivalRandom
import com.bmstu.iu3.automanagement.survival.SurvivalRaceEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartRaceScreen(
    onBack: () -> Unit,
    onClassicRaceStart: () -> Unit,
    saveManager: GameSaveManager,
    onSurvivalComplete: () -> Unit
) {
    val pixelFont = FontFamily(Font(press_start2p))
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("CLASSIC", "SURVIVAL")

    Scaffold(
        topBar = { TopAppBar(title = { Text("Race Prep", fontFamily = pixelFont, fontSize = 12.sp) }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontFamily = pixelFont, fontSize = 9.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTabIndex) {
                0 -> ClassicRaceTab(onBack = onBack, onClassicRaceStart = onClassicRaceStart)
                else -> SurvivalRaceTab(onBack = onBack, saveManager = saveManager, onSurvivalComplete = onSurvivalComplete)
            }
        }
    }
}

@Composable
private fun ClassicRaceTab(onBack: () -> Unit, onClassicRaceStart: () -> Unit) {
    val pixelFont = FontFamily(Font(press_start2p))

    val tracks = GameState.getTracks()
    val myCars = GameState.getAssembledCars()
    val myPilots = GameState.getHiredPilots()

    var selectedTrack by remember { mutableStateOf(tracks.firstOrNull()) }
    var selectedCar by remember { mutableStateOf<Car?>(null) }
    var selectedPilot by remember { mutableStateOf<Pilot?>(null) }
    val weather = remember { Weather.entries.random() }
    var showWearWarning by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            item { Text("SELECT TRACK:", fontFamily = pixelFont, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary) }
            items(tracks) { track ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selectedTrack = track },
                    colors = CardDefaults.cardColors(containerColor = if (selectedTrack == track) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                ) { Text("${track.getName()} (${track.getLength()} km)", modifier = Modifier.padding(8.dp), fontFamily = pixelFont, fontSize = 8.sp) }
            }

            item { Spacer(Modifier.height(16.dp)); Text("SELECT CAR:", fontFamily = pixelFont, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary) }
            if (myCars.isEmpty()) {
                item { Text("No cars assembled!", color = MaterialTheme.colorScheme.error, fontFamily = pixelFont, fontSize = 8.sp) }
            } else {
                items(myCars) { car ->
                    val readiness = ComponentComparator.validateCarForRace(car)
                    val hasIssue = !readiness.isValid

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selectedCar = car },
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                selectedCar == car -> MaterialTheme.colorScheme.primaryContainer
                                hasIssue -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(car.getName(), fontFamily = pixelFont, fontSize = 8.sp)
                            if (hasIssue) {
                                Text(readiness.message ?: "CAR IS NOT READY", color = Color.Red, fontFamily = pixelFont, fontSize = 6.sp)
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)); Text("SELECT PILOT:", fontFamily = pixelFont, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary) }
            if (myPilots.isEmpty()) {
                item { Text("No pilots hired!", color = MaterialTheme.colorScheme.error, fontFamily = pixelFont, fontSize = 8.sp) }
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
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                    Text("WEATHER: ${weather.name}", modifier = Modifier.padding(12.dp), fontFamily = pixelFont, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val selectedCarValidation = selectedCar?.let { ComponentComparator.validateCarForRace(it) }
        val isCarReady = selectedCarValidation?.isValid == true
        val hasHighWear = selectedCar?.let { car ->
            listOf(car.getEngine(), car.getGearbox(), car.getChassis()).any { (it?.getWear() ?: 0.0) > 0.5 }
        } ?: false

        PixelButton(
            text = when {
                selectedCar != null && !isCarReady -> "CAR NOT READY"
                else -> "START RACE"
            },
            onClick = {
                if (selectedCar != null && selectedTrack != null && selectedPilot != null && isCarReady) {
                    if (hasHighWear) {
                        showWearWarning = true
                    } else {
                        if (ClassicRaceSessionStore.startClassicRace(selectedCar!!, selectedPilot!!, selectedTrack!!, weather)) {
                            onClassicRaceStart()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            baseColor = if (selectedCar != null && selectedPilot != null && isCarReady) MaterialTheme.colorScheme.primary else Color.Gray
        )

        if (selectedCar != null && !isCarReady) {
            Text(
                text = selectedCarValidation?.message ?: "Car is not ready",
                color = MaterialTheme.colorScheme.error,
                fontFamily = pixelFont,
                fontSize = 8.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        PixelButton(text = "Back", onClick = onBack, modifier = Modifier.fillMaxWidth(), baseColor = Color.Gray)

        if (showWearWarning) {
            AlertDialog(
                onDismissRequest = { showWearWarning = false },
                title = { Text("WARNING: HIGH WEAR", fontFamily = pixelFont, fontSize = 12.sp) },
                text = { Text("Critical wear! Continue?", fontFamily = pixelFont, fontSize = 8.sp) },
                confirmButton = {
                    Button(onClick = {
                        showWearWarning = false
                        val isStillValid = ComponentComparator.validateCarForRace(selectedCar!!).isValid
                        if (isStillValid) {
                            if (ClassicRaceSessionStore.startClassicRace(selectedCar!!, selectedPilot!!, selectedTrack!!, weather)) {
                                onClassicRaceStart()
                            }
                        }
                    }) { Text("RACE ANYWAY") }
                },
                dismissButton = {
                    Button(onClick = { showWearWarning = false }) { Text("CANCEL") }
                }
            )
        }
    }
}

@Composable
private fun SurvivalRaceTab(
    onBack: () -> Unit,
    saveManager: GameSaveManager,
    onSurvivalComplete: () -> Unit
) {
    val pixelFont = FontFamily(Font(press_start2p))
    val tracks = GameState.getTracks()
    val cars = GameState.getAssembledCars()
    val pilots = GameState.getHiredPilots()
    val currentPlayer = GameState.getCurrentPlayer()

    var selectedTrack by remember { mutableStateOf(tracks.firstOrNull()) }
    var selectedCar by remember { mutableStateOf<Car?>(null) }
    var selectedPilot by remember { mutableStateOf<Pilot?>(null) }
    var selectedTargetIndex by remember { mutableIntStateOf(1) }
    var selectedTargetName by remember { mutableStateOf<String?>(null) }
    var availableCompromisingEvidence by remember(currentPlayer) { mutableStateOf(saveManager.getCompromisingEvidenceForPlayer(currentPlayer)) }
    var engine by remember { mutableStateOf<SurvivalRaceEngine?>(null) }
    var statusMessage by remember { mutableStateOf("Choose car, pilot and track.") }
    var finishHandled by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        if (engine == null) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                item { Text("SELECT TRACK:", fontFamily = pixelFont, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary) }
                items(tracks) { track ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selectedTrack = track },
                        colors = CardDefaults.cardColors(containerColor = if (selectedTrack == track) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                    ) { Text("${track.getName()} (${track.getLength()} km)", modifier = Modifier.padding(8.dp), fontFamily = pixelFont, fontSize = 8.sp) }
                }

                item { Spacer(Modifier.height(12.dp)); Text("SELECT CAR:", fontFamily = pixelFont, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary) }
                if (cars.isEmpty()) {
                    item { Text("No cars assembled!", color = MaterialTheme.colorScheme.error, fontFamily = pixelFont, fontSize = 8.sp) }
                } else {
                    items(cars) { car ->
                        val readiness = ComponentComparator.validateCarForRace(car)
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selectedCar = car },
                            colors = CardDefaults.cardColors(containerColor = when {
                                selectedCar == car -> MaterialTheme.colorScheme.primaryContainer
                                !readiness.isValid -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            })
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(car.getName(), fontFamily = pixelFont, fontSize = 8.sp)
                                if (!readiness.isValid) Text(readiness.message ?: "Not ready", color = Color.Red, fontFamily = pixelFont, fontSize = 6.sp)
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(12.dp)); Text("SELECT PILOT:", fontFamily = pixelFont, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary) }
                if (pilots.isEmpty()) {
                    item { Text("No pilots hired!", color = MaterialTheme.colorScheme.error, fontFamily = pixelFont, fontSize = 8.sp) }
                } else {
                    items(pilots) { pilot ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selectedPilot = pilot },
                            colors = CardDefaults.cardColors(containerColor = if (selectedPilot == pilot) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                        ) { Text("${pilot.getName()} (Skill: ${pilot.getSkill()})", modifier = Modifier.padding(8.dp), fontFamily = pixelFont, fontSize = 8.sp) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PixelButton(text = "SAVE", onClick = { saveManager.saveGame(GameState.getCurrentPlayer()); statusMessage = "Game saved." }, modifier = Modifier.weight(1f), baseColor = MaterialTheme.colorScheme.secondary)
                PixelButton(
                    text = "START",
                    onClick = {
                        val car = selectedCar
                        val pilot = selectedPilot
                        val track = selectedTrack
                        if (car == null || pilot == null || track == null) {
                            statusMessage = "Select car, pilot and track."
                            return@PixelButton
                        }
                        val readiness = ComponentComparator.validateCarForRace(car)
                        if (!readiness.isValid) {
                            statusMessage = readiness.message ?: "Car is not ready."
                            return@PixelButton
                        }
                        GameState.generateOpponents()
                        engine = SurvivalRaceEngine(
                            track = track,
                            weather = Weather.entries.random(),
                            playerCar = car,
                            playerPilot = pilot,
                            opponents = GameState.getOpponentTeams().take(5),
                            random = DefaultSurvivalRandom()
                        )
                        // initialize selected target to first opponent if exists
                        val initStandings = engine?.getStandings() ?: listOf()
                        val firstOpponent = initStandings.indexOfFirst { !it.isPlayer }
                        if (firstOpponent >= 0) {
                            selectedTargetIndex = firstOpponent
                            selectedTargetName = initStandings[firstOpponent].name
                        } else {
                            selectedTargetIndex = 0
                            selectedTargetName = initStandings.getOrNull(0)?.name
                        }
                        finishHandled = false
                        statusMessage = "Survival race started."
                    },
                    modifier = Modifier.weight(1f),
                    baseColor = MaterialTheme.colorScheme.primary
                )
            }

            if (availableCompromisingEvidence != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "COMPROMISING EVIDENCE READY: -${availableCompromisingEvidence!!.getPushBackValue()}",
                    fontFamily = pixelFont,
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            PixelButton(text = "Back", onClick = onBack, modifier = Modifier.fillMaxWidth(), baseColor = Color.Gray)
        } else {
            val activeEngine = engine!!
            val standings = activeEngine.getStandings()
            // Keep selectedTargetName in sync: if name is null or not present, pick first opponent
            val firstOpponentIndex = standings.indexOfFirst { !it.isPlayer }
            if (selectedTargetName == null || standings.none { it.name == selectedTargetName && it.alive && !it.isPlayer }) {
                if (firstOpponentIndex >= 0) {
                    selectedTargetIndex = firstOpponentIndex
                    selectedTargetName = standings[firstOpponentIndex].name
                } else {
                    selectedTargetIndex = 0
                    selectedTargetName = standings.getOrNull(0)?.name
                }
            } else {
                // Keep index synchronized with name
                val idx = standings.indexOfFirst { it.name == selectedTargetName }
                if (idx >= 0) selectedTargetIndex = idx
            }

            Text("ORDER ON TRACK:", fontFamily = pixelFont, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(standings) { index, competitor ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable {
                            selectedTargetIndex = index
                            selectedTargetName = competitor.name
                        },
                        colors = CardDefaults.cardColors(containerColor = when {
                            competitor.isPlayer -> MaterialTheme.colorScheme.primaryContainer
                            index == selectedTargetIndex -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        })
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("${index + 1}. ${competitor.name}${if (competitor.isPlayer) " (YOU)" else ""}", fontFamily = pixelFont, fontSize = 8.sp)
                            Text(String.format(Locale.US, "Progress: %.1f", competitor.progress), fontFamily = pixelFont, fontSize = 7.sp)
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)); Text("LOG:", fontFamily = pixelFont, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary) }
                items(activeEngine.getTurnLogs().takeLast(6)) { log ->
                    Text(log, fontFamily = pixelFont, fontSize = 7.sp)
                }
            }

            if (activeEngine.finished) {
                Text(
                    text = if (activeEngine.winnerName == "YOU") "YOU WIN!" else "${activeEngine.winnerName ?: "Nobody"} won.",
                    fontFamily = pixelFont,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                LaunchedEffect(activeEngine.finished) {
                    if (!finishHandled) {
                        GameState.addRaceResult(activeEngine.buildResults())
                        if (activeEngine.winnerName == "YOU") {
                            val awardedEvidence = saveManager.awardCompromisingEvidenceToCurrentPlayer()
                            availableCompromisingEvidence = awardedEvidence
                            statusMessage = awardedEvidence?.let {
                                "Compromising evidence awarded: -${it.getPushBackValue()}"
                            } ?: statusMessage
                        }
                        finishHandled = true
                        onSurvivalComplete()
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PixelButton(text = "ATTACK", onClick = {
                        // Resolve target index by name to avoid index-shift issues
                        val curStandings = activeEngine.getStandings()
                        val resolvedIndex = selectedTargetName?.let { name ->
                            curStandings.indexOfFirst { it.name == name && it.alive && !it.isPlayer }
                        } ?: -1
                        val targetIndexToUse = if (resolvedIndex >= 0) resolvedIndex else curStandings.indexOfFirst { !it.isPlayer }
                        if (targetIndexToUse < 0) {
                            statusMessage = "No valid targets."
                        } else {
                            val result = activeEngine.performPlayerAttack(targetIndexToUse)
                            statusMessage = result.logs.lastOrNull() ?: statusMessage
                        }
                    }, modifier = Modifier.weight(1f), baseColor = MaterialTheme.colorScheme.error)
                    PixelButton(text = "OVERTAKE", onClick = {
                        val curStandings = activeEngine.getStandings()
                        val playerIdx = curStandings.indexOfFirst { it.isPlayer }
                        if (playerIdx < 0) {
                            statusMessage = "Player state not found."
                        } else if (playerIdx == 0) {
                            // Player already in front -- engine will handle bonus
                            val result = activeEngine.performPlayerOvertake()
                            statusMessage = result.logs.lastOrNull() ?: statusMessage
                        } else {
                            val target = curStandings[playerIdx - 1]
                            statusMessage = "Attempting to overtake ${target.name}..."
                            val result = activeEngine.performPlayerOvertake()
                            // show engine result (overtook or failed)
                            statusMessage = result.logs.lastOrNull() ?: statusMessage
                        }
                    }, modifier = Modifier.weight(1f), baseColor = MaterialTheme.colorScheme.tertiary)
                    PixelButton(text = if (availableCompromisingEvidence != null) "USE EVIDENCE" else "NO EVIDENCE", onClick = {
                        val evidence = availableCompromisingEvidence
                        if (evidence == null) {
                            statusMessage = "No compromising evidence available."
                            return@PixelButton
                        }

                        val curStandings = activeEngine.getStandings()
                        val resolvedIndex = selectedTargetName?.let { name ->
                            curStandings.indexOfFirst { it.name == name && it.alive && !it.isPlayer }
                        } ?: -1
                        val targetIndexToUse = if (resolvedIndex >= 0) resolvedIndex else curStandings.indexOfFirst { !it.isPlayer }
                        if (targetIndexToUse < 0) {
                            statusMessage = "No valid targets."
                        } else {
                            val result = activeEngine.performPlayerCompromisingEvidence(targetIndexToUse, evidence.getPushBackValue())
                            statusMessage = result.logs.lastOrNull() ?: statusMessage
                            saveManager.consumeCompromisingEvidenceForPlayer(currentPlayer)
                            availableCompromisingEvidence = null
                        }
                    }, modifier = Modifier.weight(1f), baseColor = MaterialTheme.colorScheme.secondary)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PixelButton(text = "SAVE", onClick = { saveManager.saveGame(GameState.getCurrentPlayer()); statusMessage = "Game saved." }, modifier = Modifier.weight(1f), baseColor = MaterialTheme.colorScheme.secondary)
                    PixelButton(text = "Back", onClick = onBack, modifier = Modifier.weight(1f), baseColor = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(text = statusMessage, fontFamily = pixelFont, fontSize = 8.sp, color = MaterialTheme.colorScheme.primary)
    }
}

