package com.bmstu.iu3.automanagement.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bmstu.iu3.automanagement.R.font.press_start2p
import com.bmstu.iu3.automanagement.models.Car
import com.bmstu.iu3.automanagement.models.Component

@Composable
fun PixelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    baseColor: Color = MaterialTheme.colorScheme.primary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val lightShadow = baseColor.copy(alpha = 0.5f)
    val darkShadow = Color.Black.copy(alpha = 0.3f)

    val offset = if (isPressed) 2.dp else 0.dp

    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .background(Color.Black)
            .padding(2.dp)
            .background(baseColor)
            .drawBehind {
                if (!isPressed) {
                    drawRect(color = Color.White.copy(0.5f), size = size.copy(height = 2.dp.toPx()))
                    drawRect(color = Color.White.copy(0.5f), size = size.copy(width = 2.dp.toPx()))
                    drawRect(
                        color = darkShadow,
                        topLeft = Offset(0f, size.height - 2.dp.toPx()),
                        size = size.copy(height = 2.dp.toPx())
                    )
                }
            }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            modifier = Modifier.offset(y = offset),
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 16.sp
            ),
            fontFamily = FontFamily(Font(press_start2p))
        )
    }
}

@Composable
fun WorkerCard(name: String, role: String, skill: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = name, style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily(Font(press_start2p)))
            Text(text = "Role: $role", style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily(Font(press_start2p)))
            Text(text = "Skill: $skill", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily(Font(press_start2p)))
        }
    }
}

@Composable
fun SlotItem(label: String, value: String?) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("$label: ", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily(Font(press_start2p)))
        Text(
            text = value ?: "---",
            color = if (value == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily(Font(press_start2p))
        )
    }
}

@Composable
fun ComponentCard(component: Component, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(component.getName(), style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily(Font(press_start2p)))
            Text(
                text = "Type: ${component.javaClass.simpleName}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily(Font(press_start2p))
            )
        }
    }
}

@Composable
fun CarCard(car: Car) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = car.getName(),
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FontFamily(Font(press_start2p))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Performance: ${car.getTotalPerformance()}",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily(Font(press_start2p)),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("Specs:", style = MaterialTheme.typography.labelLarge, fontFamily = FontFamily(Font(press_start2p)))
            Column(modifier = Modifier.padding(start = 8.dp, top = 4.dp)) {
                car.getEngine()?.let { Text("- Engine: ${it.getName()}", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily(Font(press_start2p))) }
                car.getGearbox()?.let { Text("- Gearbox: ${it.getName()}", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily(Font(press_start2p))) }
                car.getChassis()?.let { Text("- Chassis: ${it.getName()}", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily(Font(press_start2p))) }
                car.getSuspension()?.let { Text("- Suspension: ${it.getName()}", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily(Font(press_start2p))) }
                car.getAerodynamics()?.let { Text("- Aerodynamics: ${it.getName()}", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily(Font(press_start2p))) }
                car.getTyres()?.let { Text("- Tyres: ${it.getName()}", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily(Font(press_start2p))) }
            }
        }
    }
}
