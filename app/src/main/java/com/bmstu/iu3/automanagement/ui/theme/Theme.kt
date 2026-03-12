package com.bmstu.iu3.automanagement.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DeepBlack = Color(0xFF000000)
val DarkSurface = Color(0xFF121212)
val AccentGold = Color(0xFFFFD700)

private val GameDarkColorScheme = darkColorScheme(
    primary = AccentGold,
    background = DeepBlack,
    surface = DarkSurface,
    onBackground = Color.White,
    onSurface = Color.White,
    onPrimary = Color.White,
)

@Composable
fun AutoManagementTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GameDarkColorScheme,
//        typography = Typography,
        content = content
    )
}