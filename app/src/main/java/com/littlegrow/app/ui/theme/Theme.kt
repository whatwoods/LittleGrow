package com.littlegrow.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.littlegrow.app.data.ThemeMode

private val LightColors = lightColorScheme(
    primary = Color(0xFF6FA58B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8F1E5),
    secondary = Color(0xFF8BB7D8),
    secondaryContainer = Color(0xFFD7ECFB),
    tertiary = Color(0xFFF4BB6A),
    tertiaryContainer = Color(0xFFFFE7C3),
    background = Color(0xFFFFFBF6),
    surface = Color.White,
    surfaceVariant = Color(0xFFF6EFE7),
    onSurfaceVariant = Color(0xFF5C564E),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9BD0B5),
    onPrimary = Color(0xFF163528),
    primaryContainer = Color(0xFF2B4B3D),
    secondary = Color(0xFFAED3EE),
    secondaryContainer = Color(0xFF25475F),
    tertiary = Color(0xFFF1C98D),
    tertiaryContainer = Color(0xFF684B1B),
    background = Color(0xFF151312),
    surface = Color(0xFF1E1C1B),
    surfaceVariant = Color(0xFF2D2925),
    onSurfaceVariant = Color(0xFFD4C5B7),
)

@Composable
fun LittleGrowTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && themeMode == ThemeMode.SYSTEM -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        isDark -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}

