package com.sos.studentonstudy.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colors sampled from the SOS Figma file.
internal val SosPurple = Color(0xFF4F46E5)
internal val SosBackground = Color(0xFFF4F4F4)
internal val SosCard = Color(0xFFF7F7F7)
internal val SosText = Color(0xFF111111)
internal val SosMuted = Color(0xFF7A7A7A)
internal val SosStroke = Color(0xFFD9D9D9)
internal val SosField = Color(0xFFEEEDEB)
internal val SosTeal = Color(0xFFD4E7E8)
internal val SosRow = Color(0xFFEDEEF0)
internal val SosAvatarGray = Color(0xFFD9D9D9)
internal val SosError = Color(0xFFE5191B)
internal val SosSuccess = Color(0xFF3DBE5A)
internal val SosStar = Color(0xFFFFC700)
internal val SosGradientStart = Color(0xFF3B82F6)
internal val SosGradientEnd = Color(0xFFA855F7)

private val SosColors = lightColorScheme(
    primary = SosPurple,
    onPrimary = Color.White,
    background = SosBackground,
    onBackground = SosText,
    surface = SosBackground,
    onSurface = SosText,
    surfaceContainerHigh = Color.White,
    outline = SosStroke
)

@Composable
fun SosTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = SosColors, content = content)
}
