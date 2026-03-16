package com.bmstu.iu3.automanagement.utils

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bmstu.iu3.automanagement.data.GameState

@Composable
fun DevMenuDialog(onDismiss: () -> Unit) {
    var cheatAmount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Developer Tools", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Economy Control", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = cheatAmount,
                        onValueChange = { cheatAmount = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Set Amount") },
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        cheatAmount.toDoubleOrNull()?.let { GameState.setBudget(it) }
                        cheatAmount = ""
                    }) {
                        Text("Set")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val current = GameState.getBudgetObject().getAmount()
                            GameState.setBudget(current + 10000.0)
                        }
                    ) { Text("+10k") }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val current = GameState.getBudgetObject().getAmount()
                            GameState.setBudget(current + 100000.0)
                        }
                    ) { Text("+100k") }
                }

                HorizontalDivider()

                Text("Game State", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { /* TODO: Clear Inventory */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text("Clear Inventory (WIP)")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
