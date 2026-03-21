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
    var wearAmount by remember { mutableStateOf("") }
    val pixelFont = FontFamily(Font(press_start2p))
    val myCars = GameState.getAssembledCars()

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
                Text("DEV TOOLS", style = MaterialTheme.typography.headlineSmall, fontFamily = pixelFont, fontSize = 16.sp)

                HorizontalDivider(thickness = 2.dp, color = Color.Black)

                // ECONOMY SECTION
                Text("ECONOMY", color = MaterialTheme.colorScheme.primary, fontFamily = pixelFont, fontSize = 12.sp)
                
                OutlinedTextField(
                    value = cheatAmount,
                    onValueChange = { cheatAmount = it },
                    label = { Text("SET BUDGET", fontFamily = pixelFont, fontSize = 8.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontFamily = pixelFont, fontSize = 10.sp)
                )
                
                PixelButton(
                    text = "APPLY NEW BUDGET", 
                    onClick = {
                        cheatAmount.toDoubleOrNull()?.let { GameState.setBudget(it) }
                        cheatAmount = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 10.sp,
                    paddingVertical = 8.dp
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PixelButton(text = "+10K", onClick = { GameState.addMoney(10000.0) }, modifier = Modifier.weight(1f), fontSize = 10.sp, paddingVertical = 10.dp)
                    PixelButton(text = "+100K", onClick = { GameState.addMoney(100000.0) }, modifier = Modifier.weight(1f), fontSize = 10.sp, paddingVertical = 10.dp)
                }

                HorizontalDivider(thickness = 2.dp, color = Color.Black)

                // WEAR CONTROL SECTION
                Text("WEAR CONTROL", color = MaterialTheme.colorScheme.primary, fontFamily = pixelFont, fontSize = 12.sp)
                
                OutlinedTextField(
                    value = wearAmount,
                    onValueChange = { wearAmount = it },
                    label = { Text("SET WEAR % (0-100)", fontFamily = pixelFont, fontSize = 8.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontFamily = pixelFont, fontSize = 10.sp),
                    placeholder = { Text("e.g. 50", fontSize = 8.sp, fontFamily = pixelFont) }
                )

                if (myCars.isEmpty()) {
                    Text("NO ASSEMBLED CARS FOUND", color = Color.Red, fontFamily = pixelFont, fontSize = 8.sp)
                } else {
                    myCars.forEach { car ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(car.getName().take(12), fontFamily = pixelFont, fontSize = 8.sp, modifier = Modifier.weight(1f))
                                PixelButton(
                                    text = "SET",
                                    onClick = {
                                        val w = (wearAmount.toDoubleOrNull() ?: 0.0) / 100.0
                                        setCarWear(car, w)
                                    },
                                    fontSize = 8.sp,
                                    paddingVertical = 6.dp,
                                    paddingHorizontal = 12.dp
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(thickness = 2.dp, color = Color.Black)

                // GLOBAL CHEATS
                Text("QUICK ACTIONS", color = MaterialTheme.colorScheme.primary, fontFamily = pixelFont, fontSize = 12.sp)
                
                PixelButton(
                    text = "GIVE SUPERCAR PARTS", 
                    onClick = { giveSuperCarCheat() }, 
                    modifier = Modifier.fillMaxWidth(), 
                    baseColor = Color(0xFFFFD700), 
                    fontSize = 10.sp,
                    paddingVertical = 10.dp
                )

                HorizontalDivider(thickness = 2.dp, color = Color.Black)
                
                PixelButton(
                    text = "CLOSE MENU", 
                    onClick = onDismiss, 
                    modifier = Modifier.fillMaxWidth(), 
                    baseColor = MaterialTheme.colorScheme.secondary,
                    paddingVertical = 12.dp
                )
            }
        }
    }
}

private fun setCarWear(car: Car, wear: Double) {
    listOf(car.getEngine(), car.getGearbox(), car.getChassis(), car.getSuspension(), car.getAerodynamics(), car.getTyres()).forEach {
        it?.setWear(wear.coerceIn(0.0, 1.0))
        it?.setDestroyed(wear >= 1.0) // Если 100%, то деталь разрушена
    }
}

private fun giveSuperCarCheat() {
    GameState.addComponent(Engine().apply { setName("V12 SUPER [Bolt-On]"); setPrice(0.0); setPower(1000); setWeight(100); setType("Bolt-On"); setPerformance(100.0) })
    GameState.addComponent(Gearbox().apply { setName("Gearbox [Bolt-On]"); setPrice(0.0); setType("Bolt-On"); setPerformance(100.0) })
    GameState.addComponent(Chassis().apply { setName("Chassis [Active] MONO"); setPrice(0.0); setMaxEngineWeight(200); setSuspensionType("Active"); setPerformance(100.0) })
    GameState.addComponent(Suspension().apply { setName("Suspension [Active]"); setPrice(0.0); setType("Active"); setPerformance(100.0) })
    GameState.addComponent(Aerodynamics().apply { setName("SUPER AERO"); setPrice(0.0); setPerformance(100.0) })
    GameState.addComponent(Tyres().apply { setName("SUPER SLICKS"); setPrice(0.0); setGrip(1.5); setPerformance(100.0) })
}
