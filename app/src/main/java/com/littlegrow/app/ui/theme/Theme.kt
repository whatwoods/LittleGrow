package com.littlegrow.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.littlegrow.app.data.AppTheme
import com.littlegrow.app.data.ThemeMode
import java.time.LocalTime

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

// Primary: Sage Green, Background: Warm Beige
private val EarthyTheme = AppThemeSpec(
    lightColors = lightColorScheme(
        primary = Color(0xFF5D7F56),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFDFEBD4),
        onPrimaryContainer = Color(0xFF1A3116),
        inversePrimary = Color(0xFFA1D396),
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
        surfaceTint = Color(0xFF5D7F56),
        inverseSurface = Color(0xFF35302A),
        inverseOnSurface = Color(0xFFF9EFE7),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        outline = Color(0xFF857467),
        outlineVariant = Color(0xFFD6C3B7),
        scrim = Color(0xFF000000),
        surfaceBright = Color(0xFFFFF8F3),
        surfaceContainer = Color(0xFFF5EBE1),
        surfaceContainerHigh = Color(0xFFEFE5DB),
        surfaceContainerHighest = Color(0xFFE9DFD5),
        surfaceContainerLow = Color(0xFFFBF1E7),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceDim = Color(0xFFE3D9CF)
    ),
    preview = ThemePreviewColors(
        background = Color(0xFFFFFBF5),
        primary = Color(0xFF5D7F56),
        secondary = Color(0xFF8E6F59),
        tertiary = Color(0xFFC7933D),
    ),
)

// Soft Amber / Warm Beige
private val PeachTheme = AppThemeSpec(
    lightColors = lightColorScheme(
        primary = Color(0xFF8B5000),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDCBE),
        onPrimaryContainer = Color(0xFF2C1600),
        inversePrimary = Color(0xFFFFB870),
        secondary = Color(0xFF705D4A),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFCDFC6),
        onSecondaryContainer = Color(0xFF271A0B),
        tertiary = Color(0xFF53643E),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFD6EABA),
        onTertiaryContainer = Color(0xFF121F03),
        background = Color(0xFFFFFBFF),
        onBackground = Color(0xFF201A15),
        surface = Color(0xFFFFFBFF),
        onSurface = Color(0xFF201A15),
        surfaceVariant = Color(0xFFF0E0D0),
        onSurfaceVariant = Color(0xFF4F4539),
        surfaceTint = Color(0xFF8B5000),
        inverseSurface = Color(0xFF362F29),
        inverseOnSurface = Color(0xFFFCEEE4),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        outline = Color(0xFF817567),
        outlineVariant = Color(0xFFD3C4B4),
        scrim = Color(0xFF000000),
        surfaceBright = Color(0xFFFFF8F6),
        surfaceContainer = Color(0xFFF7EAE1),
        surfaceContainerHigh = Color(0xFFF1E4DB),
        surfaceContainerHighest = Color(0xFFEBDFD6),
        surfaceContainerLow = Color(0xFFFDF0E7),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceDim = Color(0xFFE5D8D0)
    ),
    preview = ThemePreviewColors(
        background = Color(0xFFFFFBFF),
        primary = Color(0xFF8B5000),
        secondary = Color(0xFF705D4A),
        tertiary = Color(0xFF53643E),
    ),
)

// Soft Blue / Cool Beige
private val MintTheme = AppThemeSpec(
    lightColors = lightColorScheme(
        primary = Color(0xFF386666),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFBCEBEA),
        onPrimaryContainer = Color(0xFF002020),
        inversePrimary = Color(0xFFA0CFCF),
        secondary = Color(0xFF4A6362),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFCCE8E7),
        onSecondaryContainer = Color(0xFF051F1F),
        tertiary = Color(0xFF4F5F7D),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFD6E3FF),
        onTertiaryContainer = Color(0xFF0A1C36),
        background = Color(0xFFFAFDFB),
        onBackground = Color(0xFF191C1C),
        surface = Color(0xFFFAFDFB),
        onSurface = Color(0xFF191C1C),
        surfaceVariant = Color(0xFFDBE5E4),
        onSurfaceVariant = Color(0xFF3F4948),
        surfaceTint = Color(0xFF386666),
        inverseSurface = Color(0xFF2E3131),
        inverseOnSurface = Color(0xFFEFF1F0),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        outline = Color(0xFF6F7978),
        outlineVariant = Color(0xFFBFC9C8),
        scrim = Color(0xFF000000),
        surfaceBright = Color(0xFFF8FAF9),
        surfaceContainer = Color(0xFFEDF1EF),
        surfaceContainerHigh = Color(0xFFE7EBEA),
        surfaceContainerHighest = Color(0xFFE1E6E4),
        surfaceContainerLow = Color(0xFFF3F7F5),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceDim = Color(0xFFD9DDDC)
    ),
    preview = ThemePreviewColors(
        background = Color(0xFFFAFDFB),
        primary = Color(0xFF386666),
        secondary = Color(0xFF4A6362),
        tertiary = Color(0xFF4F5F7D),
    ),
)

// Soft Lilac / Neutral Beige
private val LavenderTheme = AppThemeSpec(
    lightColors = lightColorScheme(
        primary = Color(0xFF6E5676),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFF7D8FF),
        onPrimaryContainer = Color(0xFF271330),
        inversePrimary = Color(0xFFDABCE2),
        secondary = Color(0xFF665A6F),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFEDDDF6),
        onSecondaryContainer = Color(0xFF21182A),
        tertiary = Color(0xFF805158),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFD9DD),
        onTertiaryContainer = Color(0xFF321017),
        background = Color(0xFFFFFBFB),
        onBackground = Color(0xFF1E1A1D),
        surface = Color(0xFFFFFBFB),
        onSurface = Color(0xFF1E1A1D),
        surfaceVariant = Color(0xFFEBE0E8),
        onSurfaceVariant = Color(0xFF4C444C),
        surfaceTint = Color(0xFF6E5676),
        inverseSurface = Color(0xFF332F32),
        inverseOnSurface = Color(0xFFF7EEF3),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        outline = Color(0xFF7E747D),
        outlineVariant = Color(0xFFCFC4CC),
        scrim = Color(0xFF000000),
        surfaceBright = Color(0xFFFEF7FC),
        surfaceContainer = Color(0xFFF3EBF1),
        surfaceContainerHigh = Color(0xFFEDE6EC),
        surfaceContainerHighest = Color(0xFFE8E0E6),
        surfaceContainerLow = Color(0xFFF9F1F7),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceDim = Color(0xFFDFD8DE)
    ),
    preview = ThemePreviewColors(
        background = Color(0xFFFFFBFB),
        primary = Color(0xFF6E5676),
        secondary = Color(0xFF665A6F),
        tertiary = Color(0xFF805158),
    ),
)

private val WarmDarkColors = darkColorScheme(
    primary = Color(0xFFA1D396),
    onPrimary = Color(0xFF133811),
    primaryContainer = Color(0xFF2A4F26),
    onPrimaryContainer = Color(0xFFBCEFB1),
    inversePrimary = Color(0xFF40683A),
    secondary = Color(0xFFC8BBAE),
    onSecondary = Color(0xFF302922),
    secondaryContainer = Color(0xFF473F37),
    onSecondaryContainer = Color(0xFFE5D7CA),
    tertiary = Color(0xFFB2CCA3),
    onTertiary = Color(0xFF1F3514),
    tertiaryContainer = Color(0xFF354B29),
    onTertiaryContainer = Color(0xFFCDE8BE),
    background = Color(0xFF161310),
    onBackground = Color(0xFFE8E2D9),
    surface = Color(0xFF161310),
    onSurface = Color(0xFFE8E2D9),
    surfaceVariant = Color(0xFF4C463F),
    onSurfaceVariant = Color(0xFFCFC5BC),
    surfaceTint = Color(0xFFA1D396),
    inverseSurface = Color(0xFFE8E2D9),
    inverseOnSurface = Color(0xFF34302C),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF989088),
    outlineVariant = Color(0xFF4C463F),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF3D3833),
    surfaceContainer = Color(0xFF231F1C),
    surfaceContainerHigh = Color(0xFF2E2926),
    surfaceContainerHighest = Color(0xFF393430),
    surfaceContainerLow = Color(0xFF1D1B18),
    surfaceContainerLowest = Color(0xFF110E0B),
    surfaceDim = Color(0xFF161310)
)

private fun appThemeSpec(theme: AppTheme): AppThemeSpec = when (theme) {
    AppTheme.EARTHY -> EarthyTheme
    AppTheme.PEACH -> PeachTheme
    AppTheme.MINT -> MintTheme
    AppTheme.LAVENDER -> LavenderTheme
}

fun AppTheme.previewColors(): ThemePreviewColors = appThemeSpec(this).preview

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LittleGrowTheme(
    themeMode: ThemeMode,
    appTheme: AppTheme,
    largeTextModeEnabled: Boolean = false,
    darkModeScheduleEnabled: Boolean = false,
    darkModeStartHour: Int = 20,
    darkModeEndHour: Int = 7,
    content: @Composable () -> Unit,
) {
    val resolvedThemeMode = if (
        darkModeScheduleEnabled &&
        isWithinDarkSchedule(
            currentHour = LocalTime.now().hour,
            startHour = darkModeStartHour,
            endHour = darkModeEndHour,
        )
    ) {
        ThemeMode.DARK
    } else {
        themeMode
    }
    val isDark = when (resolvedThemeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colorScheme = if (isDark) {
        WarmDarkColors
    } else {
        appThemeSpec(appTheme).lightColors
    }
    val typography = typographyForScale(if (largeTextModeEnabled) 1.2f else 1f)

    CompositionLocalProvider(
        LocalMinimumInteractiveComponentSize provides if (largeTextModeEnabled) 64.dp else 48.dp,
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = Shapes,
            motionScheme = MotionScheme.expressive(),
            content = content,
        )
    }
}

private fun isWithinDarkSchedule(
    currentHour: Int,
    startHour: Int,
    endHour: Int,
): Boolean {
    return if (startHour == endHour) {
        true
    } else if (startHour < endHour) {
        currentHour in startHour until endHour
    } else {
        currentHour >= startHour || currentHour < endHour
    }
}
