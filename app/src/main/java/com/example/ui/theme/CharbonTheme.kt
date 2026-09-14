package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.R
import com.example.model.CharbonThemeMode

val NotoSansSymbolsFamily = FontFamily(
    Font(R.font.noto_sans_symbols2, FontWeight.Normal)
)

data class CharbonColors(
    val keyboardBackground: Color,
    val toolbarBackground: Color,
    val keyBackground: Color,
    val keyBackgroundPressed: Color,
    val keySpecialBackground: Color,
    val keyBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val accentText: Color
)

val LocalCharbonColors = staticCompositionLocalOf {
    DarkCharbonColors
}

val LightCharbonColors = CharbonColors(
    keyboardBackground = Color(0xFFF1F3F6),
    toolbarBackground = Color(0xFFE4E7EC),
    keyBackground = Color(0xFFFFFFFF),
    keyBackgroundPressed = Color(0xFFE2E6EC),
    keySpecialBackground = Color(0xFFD6DBE2),
    keyBorder = Color(0xFFCBD2DA),
    textPrimary = Color(0xFF111827),
    textSecondary = Color(0xFF6B7280),
    accent = Color(0xFF2563EB),
    accentText = Color(0xFFFFFFFF)
)

val DarkCharbonColors = CharbonColors(
    keyboardBackground = Color(0xFF1A1C20),
    toolbarBackground = Color(0xFF24272D),
    keyBackground = Color(0xFF2E323A),
    keyBackgroundPressed = Color(0xFF3B404A),
    keySpecialBackground = Color(0xFF23262D),
    keyBorder = Color(0xFF383C46),
    textPrimary = Color(0xFFF3F4F6),
    textSecondary = Color(0xFF9CA3AF),
    accent = Color(0xFF3B82F6),
    accentText = Color(0xFFFFFFFF)
)

val AmoledCharbonColors = CharbonColors(
    keyboardBackground = Color(0xFF000000),
    toolbarBackground = Color(0xFF0D0E10),
    keyBackground = Color(0xFF14161A),
    keyBackgroundPressed = Color(0xFF22252C),
    keySpecialBackground = Color(0xFF0A0B0E),
    keyBorder = Color(0xFF252830),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFF8E95A2),
    accent = Color(0xFF60A5FA),
    accentText = Color(0xFF000000)
)

@Composable
fun CharbonTheme(
    mode: CharbonThemeMode = CharbonThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val colors = when (mode) {
        CharbonThemeMode.LIGHT -> LightCharbonColors
        CharbonThemeMode.DARK -> DarkCharbonColors
        CharbonThemeMode.AMOLED -> AmoledCharbonColors
        CharbonThemeMode.SYSTEM -> if (isSystemDark) DarkCharbonColors else LightCharbonColors
    }

    val m3ColorScheme = if (colors == LightCharbonColors) {
        lightColorScheme(
            primary = colors.accent,
            background = colors.keyboardBackground,
            surface = colors.keyBackground,
            onPrimary = colors.accentText,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary
        )
    } else {
        darkColorScheme(
            primary = colors.accent,
            background = colors.keyboardBackground,
            surface = colors.keyBackground,
            onPrimary = colors.accentText,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary
        )
    }

    CompositionLocalProvider(LocalCharbonColors provides colors) {
        MaterialTheme(
            colorScheme = m3ColorScheme,
            typography = Typography,
            content = content
        )
    }
}
