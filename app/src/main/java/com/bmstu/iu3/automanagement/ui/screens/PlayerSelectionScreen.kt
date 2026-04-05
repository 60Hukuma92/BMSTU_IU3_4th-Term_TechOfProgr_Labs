package com.bmstu.iu3.automanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.bmstu.iu3.automanagement.R.font.press_start2p
import com.bmstu.iu3.automanagement.data.GameSaveManager
import com.bmstu.iu3.automanagement.data.GameState
import com.bmstu.iu3.automanagement.ui.theme.PixelButton

@Composable
fun PlayerSelectionScreen(
    saveManager: GameSaveManager,
    onPlayerSelected: () -> Unit
) {
    var existingPlayers by remember { mutableStateOf(saveManager.getAllPlayers().toList().sorted()) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedPlayer by remember { mutableStateOf<String?>(null) }
    var feedbackMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Select Player",
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = FontFamily(Font(press_start2p)),
            modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
        )

        if (existingPlayers.isEmpty()) {
            Text(
                text = "No players found",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(existingPlayers) { player ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PixelButton(
                            text = player,
                            onClick = {
                                selectedPlayer = player
                                if (saveManager.loadGame(player)) {
                                    feedbackMessage = "Loaded game for $player"
                                    onPlayerSelected()
                                } else {
                                    feedbackMessage = "Failed to load game"
                                }
                            },
                            modifier = Modifier.weight(1f),
                            baseColor = if (selectedPlayer == player)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.secondary
                        )
                        IconButton(
                            onClick = {
                                if (saveManager.deleteGame(player)) {
                                    existingPlayers = saveManager.getAllPlayers().toList().sorted()
                                    if (selectedPlayer == player) selectedPlayer = null
                                    feedbackMessage = "Player deleted"
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete player",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (feedbackMessage.isNotEmpty()) {
            Text(
                text = feedbackMessage,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        PixelButton(
            text = "New Player",
            onClick = { showCreateDialog = true },
            modifier = Modifier.fillMaxWidth(),
            baseColor = MaterialTheme.colorScheme.primary
        )
    }

    if (showCreateDialog) {
        NewPlayerDialog(
            onDismiss = { showCreateDialog = false },
            onCreatePlayer = { name ->
                if (name.isNotEmpty()) {
                    if (saveManager.createNewGame(name)) {
                        existingPlayers = saveManager.getAllPlayers().toList().sorted()
                        showCreateDialog = false
                        feedbackMessage = "Player created successfully"
                        onPlayerSelected()
                    }
                }
            }
        )
    }
}

@Composable
fun NewPlayerDialog(
    onDismiss: () -> Unit,
    onCreatePlayer: (String) -> Unit
) {
    var playerName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Player Name") },
        text = {
            Column {
                OutlinedTextField(
                    value = playerName,
                    onValueChange = {
                        playerName = it
                        errorMessage = ""
                    },
                    label = { Text("Player Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        playerName.isEmpty() -> errorMessage = "Name cannot be empty"
                        playerName.length > 20 -> errorMessage = "Name is too long (max 20 characters)"
                        playerName.length < 2 -> errorMessage = "Name is too short (min 2 characters)"
                        else -> onCreatePlayer(playerName)
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
