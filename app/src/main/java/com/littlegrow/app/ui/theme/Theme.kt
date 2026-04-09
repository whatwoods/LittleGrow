package com.littlegrow.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.littlegrow.app.data.AppTheme
import com.littlegrow.app.data.ThemeMode

data class ThemePreviewColors(
    val background: Color,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
)

private data class AppThemeSpec(
    val lightColors: ColorScheme,
    val preview: ThemePreviewColors,
)

private val EarthyTheme = AppThemeSpec(
    lightColors = lightColorScheme(
        primary = Color(0xFF5D7F56),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFDFEBD4),
        onPrimaryContainer = Color(0xFF1A3116),
        secondary = Color(0xFF8E6F59),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFDDC8),
        onSecondaryContainer = Color(0xFF362113),
        tertiary = Color(0xFFC7933D),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFE2B8),
        onTertiaryContainer = Color(0xFF432C00),
        background = Color(0xFFFFFBF5),
        onBackground = Color(0xFF201B16),
        surface = Color(0xFFFFFBF5),
        onSurface = Color(0xFF201B16),
        surfaceVariant = Color(0xFFF2ECE3),
        onSurfaceVariant = Color(0xFF56483C),
        outline = Color(0xFF857467),
    ),
    preview = ThemePreviewColors(
        background = Color(0xFFF6EFE2),
        primary = Color(0xFF5D7F56),
        secondary = Color(0xFF8E6F59),
        tertiary = Color(0xFFC7933D),
    ),
)

private val PeachTheme = AppThemeSpec(
    lightColors = lightColorScheme(
        primary = Color(0xFFB35C47),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDBD3),
        onPrimaryContainer = Color(0xFF442018),
        secondary = Color(0xFFCC8E63),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFDEC6),
        onSecondaryContainer = Color(0xFF42210B),
        tertiary = Color(0xFFE0A83F),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFE8BA),
        onTertiaryContainer = Color(0xFF443000),
        background = Color(0xFFFFF8F5),
        onBackground = Color(0xFF251916),
        surface = Color(0xFFFFF8F5),
        onSurface = Color(0xFF251916),
        surfaceVariant = Color(0xFFF9E8E1),
        onSurfaceVariant = Color(0xFF624840),
        outline = Color(0xFF946E64),
    ),
    preview = ThemePreviewColors(
        background = Color(0xFFFFEDE6),
        primary = Color(0xFFB35C47),
        secondary = Color(0xFFCC8E63),
        tertiary = Color(0xFFE0A83F),
    ),
)

private val MintTheme = AppThemeSpec(
    lightColors = lightColorScheme(
        primary = Color(0xFF2F7E73),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFCBF0E8),
        onPrimaryContainer = Color(0xFF043731),
        secondary = Color(0xFF4F8E8B),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD1EFED),
        onSecondaryContainer = Color(0xFF0B3534),
        tertiary = Color(0xFFB38E31),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFBE4A6),
        onTertiaryContainer = Color(0xFF3A2B00),
        background = Color(0xFFF7FCFA),
        onBackground = Color(0xFF151D1B),
        surface = Color(0xFFF7FCFA),
        onSurface = Color(0xFF151D1B),
        surfaceVariant = Color(0xFFE3F0ED),
        onSurfaceVariant = Color(0xFF43514E),
        outline = Color(0xFF72807D),
    ),
    preview = ThemePreviewColors(
        background = Color(0xFFE7F6F2),
        primary = Color(0xFF2F7E73),
        secondary = Color(0xFF4F8E8B),
        tertiary = Color(0xFFB38E31),
    ),
)

private val LavenderTheme = AppThemeSpec(
    lightColors = lightColorScheme(
        primary = Color(0xFF7863A9),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFE9E0FF),
        onPrimaryContainer = Color(0xFF29184F),
        secondary = Color(0xFF9877A8),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFF5DBFF),
        onSecondaryContainer = Color(0xFF381D45),
        tertiary = Color(0xFFBC7E95),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFD9E5),
        onTertiaryContainer = Color(0xFF4A2032),
        background = Color(0xFFF9F8FF),
        onBackground = Color(0xFF1D1A23),
        surface = Color(0xFFF9F8FF),
        onSurface = Color(0xFF1D1A23),
        surfaceVariant = Color(0xFFEDE7F4),
        onSurfaceVariant = Color(0xFF4E4659),
        outline = Color(0xFF807789),
    ),
    preview = ThemePreviewColors(
        background = Color(0xFFEDE8F8),
        primary = Color(0xFF7863A9),
        secondary = Color(0xFF9877A8),
        tertiary = Color(0xFFBC7E95),
    ),
)

private val WarmDarkColors = darkColorScheme(
    primary = Color(0xFFE4B27A),
    onPrimary = Color(0xFF46290A),
    primaryContainer = Color(0xFF63411D),
    onPrimaryContainer = Color(0xFFFFDDB7),
    secondary = Color(0xFFD5B9A1),
    onSecondary = Color(0xFF3B2A1C),
    secondaryContainer = Color(0xFF54402F),
    onSecondaryContainer = Color(0xFFF2DDC9),
    tertiary = Color(0xFFF0C77C),
    onTertiary = Color(0xFF3E2E00),
    tertiaryContainer = Color(0xFF5B4300),
    onTertiaryContainer = Color(0xFFFFE08A),
    background = Color(0xFF171310),
    onBackground = Color(0xFFF1DFD1),
    surface = Color(0xFF211C18),
    onSurface = Color(0xFFF1DFD1),
    surfaceVariant = Color(0xFF3A312B),
    onSurfaceVariant = Color(0xFFD6C3B7),
    outline = Color(0xFFA48F82),
)

private fun appThemeSpec(theme: AppTheme): AppThemeSpec = when (theme) {
    AppTheme.EARTHY -> EarthyTheme
    AppTheme.PEACH -> PeachTheme
    AppTheme.MINT -> MintTheme
    AppTheme.LAVENDER -> LavenderTheme
}

fun AppTheme.previewColors(): ThemePreviewColors = appThemeSpec(this).preview

@Composable
fun LittleGrowTheme(
    themeMode: ThemeMode,
    appTheme: AppTheme,
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colorScheme = if (isDark) {
        WarmDarkColors
    } else {
        appThemeSpec(appTheme).lightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}
