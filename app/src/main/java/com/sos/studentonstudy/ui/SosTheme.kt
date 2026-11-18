package com.sos.studentonstudy.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sos.studentonstudy.R
import com.sos.studentonstudy.data.AppLanguage

// Colors sampled from the SOS Figma file. Brand colors are fixed; surface colors follow light/dark mode.
internal val SosPurple = Color(0xFF4F46E5)
internal val SosError = Color(0xFFE5191B)
internal val SosSuccess = Color(0xFF3DBE5A)
internal val SosStar = Color(0xFFFFC700)
internal val SosGradientStart = Color(0xFF3B82F6)
internal val SosGradientEnd = Color(0xFFA855F7)

/** The colors that change between light and dark mode. */
internal class SosPalette(
    val background: Color,
    val card: Color,
    val surface: Color,
    val text: Color,
    val muted: Color,
    val stroke: Color,
    val field: Color,
    val teal: Color,
    val row: Color,
    val avatarGray: Color,
    val isDark: Boolean
)

private val LightPalette = SosPalette(
    background = Color(0xFFF4F4F4), card = Color(0xFFF7F7F7), surface = Color.White,
    text = Color(0xFF111111), muted = Color(0xFF7A7A7A), stroke = Color(0xFFD9D9D9),
    field = Color(0xFFEEEDEB), teal = Color(0xFFD4E7E8), row = Color(0xFFEDEEF0),
    avatarGray = Color(0xFFD9D9D9), isDark = false
)

private val DarkPalette = SosPalette(
    background = Color(0xFF121212), card = Color(0xFF1E1E1E), surface = Color(0xFF242424),
    text = Color(0xFFEDEDED), muted = Color(0xFF9E9E9E), stroke = Color(0xFF3A3A3A),
    field = Color(0xFF2B2B2B), teal = Color(0xFF2B4A4C), row = Color(0xFF262626),
    avatarGray = Color(0xFF444444), isDark = true
)

private val LocalSosPalette = staticCompositionLocalOf { LightPalette }

internal val SosBackground: Color @Composable @ReadOnlyComposable get() = LocalSosPalette.current.background
internal val SosCard: Color @Composable @ReadOnlyComposable get() = LocalSosPalette.current.card
/** Dialogs and raised white areas. */
internal val SosSurface: Color @Composable @ReadOnlyComposable get() = LocalSosPalette.current.surface
internal val SosText: Color @Composable @ReadOnlyComposable get() = LocalSosPalette.current.text
internal val SosMuted: Color @Composable @ReadOnlyComposable get() = LocalSosPalette.current.muted
internal val SosStroke: Color @Composable @ReadOnlyComposable get() = LocalSosPalette.current.stroke
internal val SosField: Color @Composable @ReadOnlyComposable get() = LocalSosPalette.current.field
internal val SosTeal: Color @Composable @ReadOnlyComposable get() = LocalSosPalette.current.teal
internal val SosRow: Color @Composable @ReadOnlyComposable get() = LocalSosPalette.current.row
internal val SosAvatarGray: Color @Composable @ReadOnlyComposable get() = LocalSosPalette.current.avatarGray

private fun colorScheme(p: SosPalette) =
    if (p.isDark) darkColorScheme(
        primary = SosPurple, onPrimary = Color.White, background = p.background, onBackground = p.text,
        surface = p.background, onSurface = p.text, surfaceContainerHigh = p.surface, outline = p.stroke
    ) else lightColorScheme(
        primary = SosPurple, onPrimary = Color.White, background = p.background, onBackground = p.text,
        surface = p.background, onSurface = p.text, surfaceContainerHigh = p.surface, outline = p.stroke
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
fun SosTheme(darkTheme: Boolean = false, language: AppLanguage = AppLanguage.English, content: @Composable () -> Unit) {
    val palette = if (darkTheme) DarkPalette else LightPalette
    CompositionLocalProvider(LocalSosPalette provides palette, LocalAppLanguage provides language) {
        MaterialTheme(colorScheme = colorScheme(palette), typography = SosTypography, content = content)
    }
}
