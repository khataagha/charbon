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
    val cardBackground: Color,
    val keyBackground: Color,
    val keyBackgroundPressed: Color,
    val keySpecialBackground: Color,
    val keyBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val accentText: Color,
    val accentLight: Color,
    val divider: Color,
    val success: Color
)

val LocalCharbonColors = staticCompositionLocalOf {
    DarkCharbonColors
}

val LightCharbonColors = CharbonColors(
    keyboardBackground = Color(0xFFF7F6F2),
    toolbarBackground = Color(0xFFEFECE6),
    cardBackground = Color(0xFFFFFFFF),
    keyBackground = Color(0xFFFFFFFF),
    keyBackgroundPressed = Color(0xFFE5E0D6),
    keySpecialBackground = Color(0xFFEBE6DD),
    keyBorder = Color(0xFFDDD7CC),
    textPrimary = Color(0xFF191B1F),
    textSecondary = Color(0xFF6B707B),
    accent = Color(0xFFD97724),
    accentText = Color(0xFFFFFFFF),
    accentLight = Color(0xFFFDF2E7),
    divider = Color(0xFFE8E4DC),
    success = Color(0xFF2E9E68)
)

val DarkCharbonColors = CharbonColors(
    keyboardBackground = Color(0xFF111215),
    toolbarBackground = Color(0xFF17191E),
    cardBackground = Color(0xFF181A20),
    keyBackground = Color(0xFF22242B),
    keyBackgroundPressed = Color(0xFF30333D),
    keySpecialBackground = Color(0xFF191B21),
    keyBorder = Color(0xFF2B2E37),
    textPrimary = Color(0xFFF4F3F0),
    textSecondary = Color(0xFF8E93A0),
    accent = Color(0xFFE88938),
    accentText = Color(0xFF111215),
    accentLight = Color(0x33E88938),
    divider = Color(0xFF242730),
    success = Color(0xFF34D399)
)

val AmoledCharbonColors = CharbonColors(
    keyboardBackground = Color(0xFF000000),
    toolbarBackground = Color(0xFF0C0D10),
    cardBackground = Color(0xFF0F1014),
    keyBackground = Color(0xFF16181F),
    keyBackgroundPressed = Color(0xFF252833),
    keySpecialBackground = Color(0xFF0B0C0F),
    keyBorder = Color(0xFF20232C),
    textPrimary = Color(0xFFFBFBF9),
    textSecondary = Color(0xFF838794),
    accent = Color(0xFFF29542),
    accentText = Color(0xFF000000),
    accentLight = Color(0x33F29542),
    divider = Color(0xFF1B1D24),
    success = Color(0xFF34D399)
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
