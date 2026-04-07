package com.bmstu.iu3.automanagement.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bmstu.iu3.automanagement.R.font.press_start2p
import com.bmstu.iu3.automanagement.data.GameState
import com.bmstu.iu3.automanagement.models.*
import com.bmstu.iu3.automanagement.ui.theme.PixelButton
import com.bmstu.iu3.automanagement.utils.RaceCalculator
import com.bmstu.iu3.automanagement.utils.RaceCalculator.applyPostRaceConsequences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartRaceScreen(onBack: () -> Unit, onRaceComplete: () -> Unit) {
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
                0 -> ClassicRaceTab(onBack = onBack, onRaceComplete = onRaceComplete)
                else -> SurvivalTabPlaceholder(onBack = onBack)
            }
        }
    }
}

@Composable
private fun ClassicRaceTab(onBack: () -> Unit, onRaceComplete: () -> Unit) {
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
                ) {
                    Text("${track.getName()} (${track.getLength()} km)", modifier = Modifier.padding(8.dp), fontFamily = pixelFont, fontSize = 8.sp)
                }
            }

            item { Spacer(Modifier.height(16.dp)); Text("SELECT CAR:", fontFamily = pixelFont, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary) }
            if (myCars.isEmpty()) {
                item { Text("No cars assembled!", color = MaterialTheme.colorScheme.error, fontFamily = pixelFont, fontSize = 8.sp) }
            } else {
                items(myCars) { car ->
                    val hasBrokenParts = listOf(car.getEngine(), car.getGearbox(), car.getChassis(), car.getSuspension(), car.getAerodynamics(), car.getTyres()).any { it?.isDestroyed() == true }

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selectedCar = car },
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                selectedCar == car -> MaterialTheme.colorScheme.primaryContainer
                                hasBrokenParts -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(car.getName(), fontFamily = pixelFont, fontSize = 8.sp)
                            if (hasBrokenParts) {
                                Text("HAS BROKEN PARTS!", color = Color.Red, fontFamily = pixelFont, fontSize = 6.sp)
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

        val isBroken = selectedCar?.let { car ->
            listOf(car.getEngine(), car.getGearbox(), car.getChassis(), car.getSuspension(), car.getAerodynamics(), car.getTyres()).any { it?.isDestroyed() == true }
        } ?: false

        PixelButton(
            text = if (isBroken) "REPAIR CAR FIRST" else "START RACE",
            onClick = {
                if (selectedCar != null && selectedTrack != null && selectedPilot != null && !isBroken) {
                    val hasHighWear = listOf(selectedCar!!.getEngine(), selectedCar!!.getGearbox(), selectedCar!!.getChassis())
                        .any { (it?.getWear() ?: 0.0) > 0.5 }
                    if (hasHighWear) {
                        showWearWarning = true
                    } else {
                        runRaceSimulation(selectedCar!!, selectedPilot!!, selectedTrack!!, weather, onRaceComplete)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            baseColor = if (selectedCar != null && selectedPilot != null && !isBroken) MaterialTheme.colorScheme.primary else Color.Gray
        )

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
                        runRaceSimulation(selectedCar!!, selectedPilot!!, selectedTrack!!, weather, onRaceComplete)
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
private fun SurvivalTabPlaceholder(onBack: () -> Unit) {
    val pixelFont = FontFamily(Font(press_start2p))

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = "Survival mode setup is coming next. Configure weaponized cars in Garage/Market for now.",
                modifier = Modifier.padding(12.dp),
                fontFamily = pixelFont,
                fontSize = 8.sp
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        PixelButton(text = "Back", onClick = onBack, modifier = Modifier.fillMaxWidth(), baseColor = Color.Gray)
    }
}

private fun runRaceSimulation(car: Car, pilot: Pilot, track: Track, weather: Weather, onComplete: () -> Unit) {
    GameState.generateOpponents()
    val opponents = GameState.getOpponentTeams()
    val results = mutableListOf<RaceResult>()
    
    // Player
    val playerIncident = RaceCalculator.checkIncident(car, pilot, track, weather)
    val playerTime = if (playerIncident?.getSeverity() == "Terminal") 999999.0 else RaceCalculator.calculateRaceTime(car, pilot, track, weather)
    results.add(RaceResult().apply { setTeamName("YOU"); setTime(playerTime); setIncident(playerIncident) })
    
    // ПРИМЕНЯЕМ ЭФФЕКТ ШТРАФА
    if (playerIncident?.getReason() == "Speeding Fine") {
        pilot.setFineAmount(playerIncident.getFineAmount())
        pilot.setFineDeadline(3)
    }

    // AI
    opponents.forEach { team ->
        val opIncident = RaceCalculator.checkIncident(team.getCar()!!, team.getPilot()!!, track, weather)
        val opTime = if (opIncident?.getSeverity() == "Terminal") 999999.0 else RaceCalculator.calculateRaceTime(team.getCar()!!, team.getPilot()!!, track, weather)
        results.add(RaceResult().apply { setTeamName(team.getName()); setTime(opTime); setIncident(opIncident) })
    }
    
    results.sortBy { it.getTime() }
    results.forEachIndexed { index, res -> 
        res.setPosition(index + 1)
        if (res.getTeamName() == "YOU") {
            val prize = when(index) { 0 -> 5000.0; 1 -> 3000.0; 2 -> 1500.0; else -> 0.0 }
            res.setPrizeMoney(prize)
            GameState.addMoney(prize)
        }
    }
    
    applyPostRaceConsequences(car, playerIncident)
    GameState.addRaceResult(results)
    
    // ОБНОВЛЯЕМ СОСТОЯНИЕ (Штрафы и Тюрьму)
    GameState.processRaceEndUpdates()
    
    onComplete()
}