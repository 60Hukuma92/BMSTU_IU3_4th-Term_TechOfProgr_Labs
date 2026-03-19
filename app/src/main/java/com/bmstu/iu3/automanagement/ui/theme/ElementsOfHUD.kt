package com.bmstu.iu3.automanagement.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
    
    // Вычисляем цвет при нажатии (делаем темнее)
    val buttonColor = if (isPressed) {
        // Уменьшаем яркость каждого канала для эффекта затемнения
        Color(
            red = (baseColor.red * 0.7f),
            green = (baseColor.green * 0.7f),
            blue = (baseColor.blue * 0.7f),
            alpha = baseColor.alpha
        )
    } else {
        baseColor
    }

    val darkShadow = Color.Black.copy(alpha = 0.3f)
    val offset = if (isPressed) 2.dp else 0.dp

    Box(
        modifier = modifier
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .background(Color.Black)
            .padding(2.dp)
            .background(buttonColor)
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
            style = TextStyle(fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp),
            fontFamily = FontFamily(Font(press_start2p))
        )
    }
}

@Composable
fun WearIndicator(wear: Double) {
    val color = when {
        wear > 0.8 -> Color.Red
        wear > 0.5 -> Color.Yellow
        else -> Color.Green
    }
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("WEAR", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily(Font(press_start2p)), fontSize = 8.sp)
            Text("${(wear * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily(Font(press_start2p)), fontSize = 8.sp)
        }
        LinearProgressIndicator(
            progress = { wear.toFloat() },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = color,
            trackColor = Color.Black.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Butt
        )
    }
}

@Composable
fun ComponentCard(component: Component, isSelected: Boolean = false, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(component.getName(), style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily(Font(press_start2p)))
            Text(
                text = "Type: ${component.javaClass.simpleName}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily(Font(press_start2p))
            )
            WearIndicator(wear = component.getWear())
        }
    }
}

@Composable
fun CarCard(car: Car) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(car.getName(), style = MaterialTheme.typography.headlineSmall, fontFamily = FontFamily(Font(press_start2p)))
            Spacer(modifier = Modifier.height(8.dp))
            
            val displayPerformance = if (car.getPerformance() > 0) car.getPerformance() else car.getTotalPerformance()
            
            Text(
                text = "Performance: ${String.format("%.1f", displayPerformance)}",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily(Font(press_start2p)),
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            Text("COMPONENTS CONDITION:", style = MaterialTheme.typography.labelLarge, fontFamily = FontFamily(Font(press_start2p)), fontSize = 10.sp)
            
            Column(modifier = Modifier.padding(start = 8.dp, top = 4.dp)) {
                car.getEngine()?.let { WearIndicator(it.getWear()) }
                car.getGearbox()?.let { WearIndicator(it.getWear()) }
                car.getChassis()?.let { WearIndicator(it.getWear()) }
            }
        }
    }
}

@Composable
fun WorkerCard(name: String, role: String, skill: Int) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
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
