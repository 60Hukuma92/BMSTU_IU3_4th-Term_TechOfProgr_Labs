package com.bmstu.iu3.automanagement.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bmstu.iu3.automanagement.R.font.press_start2p
import com.bmstu.iu3.automanagement.data.GameState
import com.bmstu.iu3.automanagement.models.Track
import com.bmstu.iu3.automanagement.ui.theme.PixelButton
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTracksScreen(onBack: () -> Unit) {
    val pixelFont = FontFamily(Font(press_start2p))
    val tracks = GameState.getTracks()

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var name by remember { mutableStateOf("") }
    var length by remember { mutableStateOf("") }
    var straightsRatio by remember { mutableStateOf("") }
    var elevationChange by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }

    fun resetForm() {
        selectedIndex = null
        name = ""
        length = ""
        straightsRatio = ""
        elevationChange = ""
        feedback = ""
    }

    fun loadTrackToForm(index: Int) {
        val track = tracks[index]
        selectedIndex = index
        name = track.getName()
        length = track.getLength().toString()
        straightsRatio = track.getStraightsRatio().toString()
        elevationChange = track.getElevationChange().toString()
        feedback = ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tracks", fontFamily = pixelFont, fontSize = 12.sp) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Your tracks",
                fontFamily = pixelFont,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.primary
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(tracks) { index, track ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { loadTrackToForm(index) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedIndex == index) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(track.getName(), fontFamily = pixelFont, fontSize = 8.sp)
                            Text(
                                "${track.getLength()} km | S ${track.getStraightsRatio()} / C ${track.getCornersRatio()}",
                                fontFamily = pixelFont,
                                fontSize = 7.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Track name") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = length,
                onValueChange = { length = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Length (km)") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = straightsRatio,
                onValueChange = { straightsRatio = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Straights ratio (0..1)") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = elevationChange,
                onValueChange = { elevationChange = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Elevation change") },
                singleLine = true
            )

            val corners = (straightsRatio.toDoubleOrNull()?.let { 1.0 - it })
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Corners ratio: ${corners?.let { String.format(Locale.US, "%.2f", it) } ?: "-"}",
                fontFamily = pixelFont,
                fontSize = 8.sp
            )

            if (feedback.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = feedback,
                    color = MaterialTheme.colorScheme.error,
                    fontFamily = pixelFont,
                    fontSize = 8.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            fun buildTrackFromForm(): Track? {
                val parsedLength = length.toDoubleOrNull()
                val parsedStraights = straightsRatio.toDoubleOrNull()
                val parsedElevation = elevationChange.toDoubleOrNull()

                if (name.isBlank() || parsedLength == null || parsedStraights == null || parsedElevation == null) {
                    feedback = "Fill all fields correctly"
                    return null
                }

                val cornersRatio = 1.0 - parsedStraights
                return Track().apply {
                    setName(name.trim())
                    setLength(parsedLength)
                    setStraightsRatio(parsedStraights)
                    setCornersRatio(cornersRatio)
                    setElevationChange(parsedElevation)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                PixelButton(
                    text = if (selectedIndex == null) "Create" else "Update",
                    onClick = {
                        val newTrack = buildTrackFromForm() ?: return@PixelButton
                        val success = if (selectedIndex == null) {
                            GameState.addTrack(newTrack)
                        } else {
                            GameState.updateTrack(selectedIndex!!, newTrack)
                        }

                        if (success) {
                            feedback = "Saved"
                            resetForm()
                        } else {
                            feedback = "Invalid values"
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                PixelButton(
                    text = "New",
                    onClick = { resetForm() },
                    modifier = Modifier.weight(1f),
                    baseColor = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                PixelButton(
                    text = "Delete",
                    onClick = {
                        val index = selectedIndex
                        if (index == null) {
                            feedback = "Select a track"
                        } else {
                            val success = GameState.removeTrack(index)
                            feedback = if (success) {
                                resetForm()
                                "Deleted"
                            } else {
                                "Cannot delete last track"
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    baseColor = MaterialTheme.colorScheme.error
                )
                PixelButton(
                    text = "Back",
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    baseColor = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

