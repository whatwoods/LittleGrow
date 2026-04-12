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
import com.littlegrow.app.ui.theme.darkSemanticColors
import com.littlegrow.app.ui.theme.lightSemanticColors
import com.littlegrow.app.ui.theme.LocalSemanticColors
import java.time.LocalTime

data class ThemePreviewColors(
    val background: Color,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
)

private data class AppThemeSpec(
    val lightColors: ColorScheme,
    val darkColors: ColorScheme,
    val preview: ThemePreviewColors,
)

private val EarthyTheme = AppThemeSpec(
    lightColors = lightColorScheme(
        primary = Color(0xFF825600),
        onPrimary = Color(0xFFFFF8F2),
        primaryContainer = Color(0xFFFFB22D),
        onPrimaryContainer = Color(0xFF543600),
        inversePrimary = Color(0xFFF3A71C),
        secondary = Color(0xFF006786),
        onSecondary = Color(0xFFF3FAFF),
        secondaryContainer = Color(0xFFBEE9FF),
        onSecondaryContainer = Color(0xFF005974),
        tertiary = Color(0xFF974362),
        onTertiary = Color(0xFFFFF7F7),
        tertiaryContainer = Color(0xFFFF9BBC),
        onTertiaryContainer = Color(0xFF641A39),
        background = Color(0xFFFAF9F6),
        onBackground = Color(0xFF303330),
        surface = Color(0xFFFAF9F6),
        onSurface = Color(0xFF303330),
        surfaceVariant = Color(0xFFE1E3DF),
        onSurfaceVariant = Color(0xFF5D605C),
        surfaceTint = Color(0xFF825600),
        inverseSurface = Color(0xFF0D0F0D),
        inverseOnSurface = Color(0xFF9D9D9B),
        error = Color(0xFFAA371C),
        onError = Color(0xFFFFF7F6),
        errorContainer = Color(0xFFFA7150),
        onErrorContainer = Color(0xFF671200),
        outline = Color(0xFF797B78),
        outlineVariant = Color(0xFFB0B2AF),
        scrim = Color(0xFF000000),
        surfaceBright = Color(0xFFFAF9F6),
        surfaceContainer = Color(0xFFEEEEEA),
        surfaceContainerHigh = Color(0xFFE8E8E5),
        surfaceContainerHighest = Color(0xFFE1E3DF),
        surfaceContainerLow = Color(0xFFF4F4F0),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceDim = Color(0xFFD8DBD6),
    ),
    darkColors = darkColorScheme(
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
    ),
    preview = ThemePreviewColors(
        background = Color(0xFFFAF9F6),
        primary = Color(0xFF825600),
        secondary = Color(0xFF006786),
        tertiary = Color(0xFF974362),
    ),
)

private val PeachTheme = AppThemeSpec(
    lightColors = lightColorScheme(
        primary = Color(0xFFF88379), // Coral Pink
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFD1CC),
        onPrimaryContainer = Color(0xFF5A1E18),
        secondary = Color(0xFFE9967A), // Dark Salmon
        onSecondary = Color(0xFFFFFFFF),
        tertiary = Color(0xFFFFB6C1), // Warm Pink
        onTertiary = Color(0xFFFFFFFF),
        background = Color(0xFFFFFDD0), // Cream
        onBackground = Color(0xFF332E2D),
        surface = Color(0xFFFFFDD0),
        onSurface = Color(0xFF332E2D),
        surfaceVariant = Color(0xFFEFE0DF),
        onSurfaceVariant = Color(0xFF4F4544),
    ),
    darkColors = darkColorScheme(
        primary = Color(0xFFFFB4AB),
        onPrimary = Color(0xFF561E16),
        primaryContainer = Color(0xFF73332A),
        onPrimaryContainer = Color(0xFFFFDAD5),
        secondary = Color(0xFFFFB4A9),
        onSecondary = Color(0xFF561E18),
        tertiary = Color(0xFFFFB3BC),
        onTertiary = Color(0xFF561D26),
        background = Color(0xFF201A19),
        onBackground = Color(0xFFEDE0DE),
        surface = Color(0xFF201A19),
        onSurface = Color(0xFFEDE0DE),
        surfaceVariant = Color(0xFF534341),
        onSurfaceVariant = Color(0xFFD8C2BF),
    ),
    preview = ThemePreviewColors(
        background = Color(0xFFFFFDD0),
        primary = Color(0xFFF88379),
        secondary = Color(0xFFE9967A),
        tertiary = Color(0xFFFFB6C1),
    ),
)

private val MintTheme = AppThemeSpec(
    lightColors = lightColorScheme(
        primary = Color(0xFF98FF98), // Mint Green
        onPrimary = Color(0xFF00390A),
        primaryContainer = Color(0xFFB4FFB1),
        onPrimaryContainer = Color(0xFF002204),
        secondary = Color(0xFF71EFA3), // Sea Foam
        onSecondary = Color(0xFF00391E),
        tertiary = Color(0xFF8CE6E1), // Cyan
        onTertiary = Color(0xFF003735),
        background = Color(0xFFE1F5E6), // Light Green
        onBackground = Color(0xFF191C19),
        surface = Color(0xFFE1F5E6),
        onSurface = Color(0xFF191C19),
        surfaceVariant = Color(0xFFDCE5DA),
        onSurfaceVariant = Color(0xFF414941),
    ),
    darkColors = darkColorScheme(
        primary = Color(0xFF82DA84),
        onPrimary = Color(0xFF00390A),
        primaryContainer = Color(0xFF005312),
        onPrimaryContainer = Color(0xFF9DF79E),
        secondary = Color(0xFF86D5A2),
        onSecondary = Color(0xFF00381C),
        tertiary = Color(0xFF8CE6E1),
        onTertiary = Color(0xFF003735),
        background = Color(0xFF111411),
        onBackground = Color(0xFFE2E3DE),
        surface = Color(0xFF111411),
        onSurface = Color(0xFFE2E3DE),
        surfaceVariant = Color(0xFF414941),
        onSurfaceVariant = Color(0xFFC0C9BF),
    ),
    preview = ThemePreviewColors(
        background = Color(0xFFE1F5E6),
        primary = Color(0xFF98FF98),
        secondary = Color(0xFF71EFA3),
        tertiary = Color(0xFF8CE6E1),
    ),
)

private val LavenderTheme = AppThemeSpec(
    lightColors = lightColorScheme(
        primary = Color(0xFFC8A2C8), // Lilac
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFEEDCFF),
        onPrimaryContainer = Color(0xFF280055),
        secondary = Color(0xFFB565A7), // Purple
        onSecondary = Color(0xFFFFFFFF),
        tertiary = Color(0xFFFFC0CB), // Rose Pink
        onTertiary = Color(0xFFFFFFFF),
        background = Color(0xFFE6E6FA), // Lavender Gray
        onBackground = Color(0xFF1C1B1E),
        surface = Color(0xFFE6E6FA),
        onSurface = Color(0xFF1C1B1E),
        surfaceVariant = Color(0xFFE7E0EB),
        onSurfaceVariant = Color(0xFF49454E),
    ),
    darkColors = darkColorScheme(
        primary = Color(0xFFD0BCFF),
        onPrimary = Color(0xFF381E72),
        primaryContainer = Color(0xFF4F378B),
        onPrimaryContainer = Color(0xFFEADDFF),
        secondary = Color(0xFFCCC2DC),
        onSecondary = Color(0xFF332D41),
        tertiary = Color(0xFFEFB8C8),
        onTertiary = Color(0xFF492532),
        background = Color(0xFF141218),
        onBackground = Color(0xFFE6E1E5),
        surface = Color(0xFF141218),
        onSurface = Color(0xFFE6E1E5),
        surfaceVariant = Color(0xFF49454F),
        onSurfaceVariant = Color(0xFFCAC4D0),
    ),
    preview = ThemePreviewColors(
        background = Color(0xFFE6E6FA),
        primary = Color(0xFFC8A2C8),
        secondary = Color(0xFFB565A7),
        tertiary = Color(0xFFFFC0CB),
    ),
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
    val themeSpec = appThemeSpec(appTheme)
    val colorScheme = if (isDark) {
        themeSpec.darkColors
    } else {
        themeSpec.lightColors
    }
    val typography = typographyForScale(if (largeTextModeEnabled) 1.2f else 1f)

    val semanticColors = if (isDark) darkSemanticColors else lightSemanticColors
    CompositionLocalProvider(
        LocalMinimumInteractiveComponentSize provides if (largeTextModeEnabled) 64.dp else 48.dp,
        LocalSemanticColors provides semanticColors
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
