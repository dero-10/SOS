package com.sos.studentonstudy.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal val SosPurple = Color(0xFF5548E7)
internal val SosPurpleDark = Color(0xFF473ACB)
internal val SosText = Color(0xFF24243B)
internal val SosMuted = Color(0xFF85879A)
internal val SosSurface = Color(0xFFF7F8FC)
internal val SosStroke = Color(0xFFE8E9F1)
internal val SosAqua = Color(0xFFE5F8F5)

private val SosColors = lightColorScheme(
    primary = SosPurple,
    onPrimary = Color.White,
    background = Color.White,
    onBackground = SosText,
    surface = Color.White,
    onSurface = SosText,
    outline = SosStroke
)

@Composable
fun SosTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = SosColors, content = content)
}
