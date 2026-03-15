package com.bmstu.iu3.automanagement.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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