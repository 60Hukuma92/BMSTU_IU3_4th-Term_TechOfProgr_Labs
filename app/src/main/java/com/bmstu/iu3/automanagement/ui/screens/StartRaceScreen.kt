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
import com.bmstu.iu3.automanagement.ui.theme.PixelButton
import com.bmstu.iu3.automanagement.race.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val MAX_VISIBLE_COMMENTARY = 40

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartRaceScreen(onBack: () -> Unit, onRaceComplete: () -> Unit) {
    val pixelFont = FontFamily(Font(press_start2p))
    val scope = rememberCoroutineScope()
    
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
    var lastDisplayedSecond by remember { mutableStateOf<String?>(null) }
    val raceMessages = remember { mutableStateListOf<CommentatorMessage>() }
    val pendingMessages = remember { mutableStateListOf<CommentatorMessage>() }
    val listState = rememberLazyListState()

    fun showNextCommentary() {
        if (pendingMessages.isNotEmpty()) {
            raceMessages.add(pendingMessages.removeAt(0))
            if (raceMessages.size > MAX_VISIBLE_COMMENTARY) {
                raceMessages.removeAt(0)
            }
        }
    }

    val engine = remember {
        ClassicRaceEngine(
            clock = SystemRaceClock(),
            tacticResolver = SimpleTacticResolver(),
            pitStopManager = SemaphorePitStopManager(listOf(PitStopBox("pit-box-1", 1))),
            eventSink = object : RaceEventSink { override fun publish(event: RaceLogEntry) {} }
        )
    }

    if (isRacing) {
        Column(modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(Color.Red))
                Spacer(Modifier.width(8.dp))
                Text("LIVE BROADCAST: ${selectedTrack?.getName()}", color = Color.White, fontFamily = pixelFont, fontSize = 12.sp)
            }
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable { showNextCommentary() }
            ) {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(raceMessages) { msg ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("${msg.displayTime} ${msg.source.uppercase()}:", color = Color.Gray, fontSize = 8.sp, fontFamily = pixelFont)
                            Text(msg.message, color = if(msg.source == "Incident") Color.Red else Color.Yellow, fontSize = 11.sp, fontFamily = pixelFont, lineHeight = 16.sp)
                            HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }

                if (pendingMessages.isNotEmpty()) {
                    Text(
                        text = "TAP TO SHOW NEXT COMMENT (${pendingMessages.size})",
                        color = Color.Cyan,
                        fontFamily = pixelFont,
                        fontSize = 8.sp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    )
                }
            }
            
            LaunchedEffect(raceMessages.size) {
                if (raceMessages.isNotEmpty()) {
                    listState.animateScrollToItem(raceMessages.size - 1)
                }
            }

            if (raceCompleted) {
                PixelButton(text = "VIEW RESULTS", onClick = onRaceComplete, modifier = Modifier.fillMaxWidth())
            }
        }
    } else {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Race Preparation", fontFamily = pixelFont, fontSize = 12.sp) }) }
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
                }

                Spacer(Modifier.height(16.dp))
                PixelButton(
                    text = "START RACE",
                    onClick = {
                        if (selectedCar != null && selectedPilot != null && selectedTrack != null) {
                            isRacing = true
                            raceCompleted = false
                            raceMessages.clear()
                            pendingMessages.clear()
                            lastDisplayedSecond = null
                                            engine.startRace(
                                track = selectedTrack!!,
                                players = listOf("YOU") + opponents.map { it.getName() },
                                pilotSkillsByName = buildMap {
                                    put("YOU", selectedPilot!!.getSkill())
                                    opponents.forEach { team -> put(team.getName(), team.getPilot()?.getSkill() ?: 50) }
                                },
                                carPerformanceByName = buildMap {
                                    put("YOU", selectedCar!!.getPerformance())
                                    opponents.forEach { team -> put(team.getName(), team.getCar()?.getPerformance() ?: team.getCar()?.getTotalPerformance() ?: 0.0) }
                                },
                                onCommentary = { msg ->
                                    scope.launch(Dispatchers.Main) {
                                        val playerName = GameState.getCurrentPlayer()
                                        val displayMsg = msg.copy(message = msg.message.replace(Regex("\\bYOU\\b"), playerName))
                                        val secondKey = displayMsg.displayTime
                                        if (secondKey == lastDisplayedSecond) return@launch
                                        if (shouldDisplayCommentary(displayMsg, raceMessages.size)) {
                                            lastDisplayedSecond = secondKey
                                            pendingMessages.add(displayMsg)
                                            if (raceMessages.isEmpty()) {
                                                showNextCommentary()
                                            }
                                        }
                                    }
                                },
                                onFinished = { outcome ->
                                    scope.launch(Dispatchers.Main) {
                                        raceCompleted = true
                                        val playerName = GameState.getCurrentPlayer()
                                        GameState.addRaceCommentary(compactCommentary(outcome.commentary).map {
                                            it.copy(message = it.message.replace(Regex("\\bYOU\\b"), playerName))
                                        })
                                        val standings = outcome.standings.sortedBy { it.position }
                                        val leaderProgress = standings.maxOfOrNull { it.finalProgress }?.coerceAtLeast(1.0) ?: 1.0
                                        val baseSeconds = selectedTrack!!.getLength() * 60.0 + (selectedTrack!!.getCornersRatio() * 45.0) + (selectedTrack!!.getElevationChange() * 0.7)

                                        GameState.addRaceResult(standings.map { standing ->
                                            val teamName = if (standing.displayName == "YOU") playerName else standing.displayName
                                            val resultTime = if (standing.finalProgress <= 0.0) {
                                                999999.0
                                            } else {
                                                baseSeconds * (leaderProgress / standing.finalProgress)
                                            }

                                            RaceResult().apply {
                                                setTeamName(teamName)
                                                setPosition(standing.position)
                                                setTime(resultTime)
                                                setIncident(standing.incident)
                                            }
                                        })

                                        // Обработка штрафов
                                        if (outcome.pilotFines.containsKey("YOU")) {
                                            selectedPilot!!.setFineAmount(outcome.pilotFines["YOU"]!!)
                                            selectedPilot!!.setFineDeadline(3)
                                        }
                                        GameState.processRaceEndUpdates()
                                    }
                                }
                            )
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

private fun shouldDisplayCommentary(msg: CommentatorMessage, visibleCount: Int): Boolean {
    val text = msg.message.uppercase()
    return when {
        msg.source == "Incident" -> true
        msg.source == "Weather" -> true
        text.contains("DNF") || text.contains("FLAG") || text.contains("OUT OF THE RACE") -> true
        text.contains("LEAD") || text.contains("OVERTAK") || text.contains("PIT") -> true
        msg.source.startsWith("Car:") -> visibleCount % 4 == 0
        else -> visibleCount % 6 == 0
    }
}

private fun compactCommentary(messages: List<CommentatorMessage>): List<CommentatorMessage> {
    val compacted = mutableListOf<CommentatorMessage>()
    messages.forEach { msg ->
        if (shouldDisplayCommentary(msg, compacted.size)) {
            compacted.add(msg)
        }
    }
    return if (compacted.size > MAX_VISIBLE_COMMENTARY) compacted.takeLast(MAX_VISIBLE_COMMENTARY) else compacted
}

