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

private val NurturingTheme = AppThemeSpec(
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
    preview = ThemePreviewColors(
        background = Color(0xFFFAF9F6),
        primary = Color(0xFF825600),
        secondary = Color(0xFF006786),
        tertiary = Color(0xFF974362),
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

private fun appThemeSpec(theme: AppTheme): AppThemeSpec = NurturingTheme

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
