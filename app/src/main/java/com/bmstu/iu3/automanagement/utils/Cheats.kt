package com.bmstu.iu3.automanagement.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bmstu.iu3.automanagement.R.font.press_start2p
import com.bmstu.iu3.automanagement.data.GameState
import com.bmstu.iu3.automanagement.models.*
import com.bmstu.iu3.automanagement.ui.theme.PixelButton

@Composable
fun DevMenuDialog(onDismiss: () -> Unit) {
    var cheatAmount by remember { mutableStateOf("") }
    val pixelFont = FontFamily(Font(press_start2p))

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(4.dp, Color.Black),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "DEV TOOLS",
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = pixelFont,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                HorizontalDivider(thickness = 2.dp, color = Color.Black)

                Text(
                    "ECONOMY",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = pixelFont,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = cheatAmount,
                    onValueChange = { cheatAmount = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("AMOUNT", fontFamily = pixelFont, fontSize = 10.sp) },
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = pixelFont, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = Color.Gray
                    )
                )

                PixelButton(
                    text = "SET",
                    onClick = {
                        cheatAmount.toDoubleOrNull()?.let { GameState.setBudget(it) }
                        cheatAmount = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PixelButton(
                        text = "+10K",
                        onClick = {
                            val current = GameState.getBudgetObject().getAmount()
                            GameState.setBudget(current + 10000.0)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    PixelButton(
                        text = "+100K",
                        onClick = {
                            val current = GameState.getBudgetObject().getAmount()
                            GameState.setBudget(current + 100000.0)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider(thickness = 2.dp, color = Color.Black)

                Text(
                    "CHEATS",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = pixelFont,
                    fontSize = 12.sp
                )

                PixelButton(
                    text = "GIVE SUPERCAR PARTS",
                    onClick = {
                        giveSuperCarCheat()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    baseColor = Color(0xFFFFD700)
                )

                HorizontalDivider(thickness = 2.dp, color = Color.Black)

                PixelButton(
                    text = "CLOSE",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    baseColor = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

private fun giveSuperCarCheat() {
    GameState.addComponent(Engine().apply { 
        setName("V12 SUPER"); setPrice(0.0); setPower(1000); setWeight(100); setType("V12"); setPerformance(100.0) 
    })
    GameState.addComponent(Gearbox().apply { 
        setName("X-SHIFT SUPER"); setPrice(0.0); setType("V12"); setPerformance(100.0) 
    })
    GameState.addComponent(Chassis().apply { 
        setName("MONO-CARBON SUPER"); setPrice(0.0); setMaxEngineWeight(200); setSuspensionType("ACTIVE"); setPerformance(100.0) 
    })
    GameState.addComponent(Suspension().apply { 
        setName("MAG-RIDE SUPER"); setPrice(0.0); setType("ACTIVE"); setPerformance(100.0) 
    })
    GameState.addComponent(Aerodynamics().apply { 
        setName("WIND-TUNNEL SUPER"); setPrice(0.0); setPerformance(100.0) 
    })
    GameState.addComponent(Tyres().apply { 
        setName("SLICK SUPER"); setPrice(0.0); setGrip(1.5); setPerformance(100.0) 
    })
}
