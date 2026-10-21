package com.sos.studentonstudy.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sos.studentonstudy.R

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

/** Inter, the font used throughout the Figma file (SIL Open Font License, see third_party/inter). */
internal val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

private val SosTypography = Typography().let { base ->
    Typography(
        displayLarge = base.displayLarge.copy(fontFamily = Inter),
        displayMedium = base.displayMedium.copy(fontFamily = Inter),
        displaySmall = base.displaySmall.copy(fontFamily = Inter),
        headlineLarge = base.headlineLarge.copy(fontFamily = Inter),
        headlineMedium = base.headlineMedium.copy(fontFamily = Inter),
        headlineSmall = base.headlineSmall.copy(fontFamily = Inter),
        titleLarge = base.titleLarge.copy(fontFamily = Inter),
        titleMedium = base.titleMedium.copy(fontFamily = Inter),
        titleSmall = base.titleSmall.copy(fontFamily = Inter),
        // Text without an explicit style falls back to bodyLarge; Figma sets Inter with no extra tracking.
        bodyLarge = base.bodyLarge.copy(fontFamily = Inter, letterSpacing = 0.sp),
        bodyMedium = base.bodyMedium.copy(fontFamily = Inter),
        bodySmall = base.bodySmall.copy(fontFamily = Inter),
        labelLarge = base.labelLarge.copy(fontFamily = Inter),
        labelMedium = base.labelMedium.copy(fontFamily = Inter),
        labelSmall = base.labelSmall.copy(fontFamily = Inter)
    )
}

@Composable
fun SosTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = SosColors, typography = SosTypography, content = content)
}
