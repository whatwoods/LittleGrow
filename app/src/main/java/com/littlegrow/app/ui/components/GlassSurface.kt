package com.littlegrow.app.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.littlegrow.app.ui.theme.softShadow
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

val LocalGlassHazeState = compositionLocalOf<HazeState?> { null }

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    alpha: Float = 0.70f,
    shape: Shape = RoundedCornerShape(24.dp),
    tintColor: Color = MaterialTheme.colorScheme.surface,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    borderAlpha: Float = 0.18f,
    highlightAlpha: Float = 0.16f,
    shadowElevation: Dp = 18.dp,
    clipHazeToShape: Boolean = false,
    expandHazeBounds: Boolean = true,
    content: @Composable () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val hazeState = LocalGlassHazeState.current
    val hazeTint = if (isDark) {
        tintColor.copy(alpha = (alpha * 0.12f).coerceIn(0.07f, 0.12f))
    } else {
        Color.White.copy(alpha = (alpha * 0.075f).coerceIn(0.03f, 0.07f))
    }
    val fallbackTint = if (isDark) {
        tintColor.copy(alpha = (alpha * 0.2f).coerceIn(0.14f, 0.22f))
    } else {
        tintColor.copy(alpha = (alpha * 0.12f).coerceIn(0.07f, 0.12f))
    }
    val hazeStyle = HazeStyle(
        backgroundColor = Color.Transparent,
        tint = HazeTint(hazeTint),
        blurRadius = 24.dp,
        noiseFactor = if (isDark) 0.06f else 0.035f,
        fallbackTint = HazeTint(fallbackTint),
    )
    val overlayAlpha = if (isDark) {
        (alpha * 0.18f).coerceIn(0.08f, 0.16f)
    } else {
        (alpha * 0.09f).coerceIn(0.02f, 0.08f)
    }
    val resolvedBorderAlpha = if (isDark) borderAlpha.coerceIn(0.08f, 0.14f) else borderAlpha.coerceIn(0.12f, 0.22f)
    val resolvedHighlightAlpha = if (isDark) highlightAlpha.coerceIn(0.04f, 0.07f) else highlightAlpha.coerceIn(0.05f, 0.1f)
    val shadowColor = if (isDark) Color.Black.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.05f)
    val hazeModifier = if (hazeState != null) {
        Modifier.hazeEffect(
            state = hazeState,
            style = hazeStyle,
        ) {
            blurEnabled = true
            if (clipHazeToShape) {
                blurredEdgeTreatment = BlurredEdgeTreatment(shape)
            }
            expandLayerBounds = expandHazeBounds
        }
    } else {
        Modifier
    }

    Surface(
        modifier = modifier
            .then(hazeModifier)
            .softShadow(elevation = shadowElevation, shape = shape, color = shadowColor),
        shape = shape,
        color = Color.Transparent,
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)),
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.drawWithCache {
                val diagonalTint = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = overlayAlpha * if (isDark) 0.35f else 0.75f),
                        tintColor.copy(alpha = overlayAlpha * if (isDark) 0.46f else 0.6f),
                        accentColor.copy(alpha = overlayAlpha * if (isDark) 0.2f else 0.26f),
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height),
                )
                val topGlow = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = resolvedHighlightAlpha),
                        Color.White.copy(alpha = resolvedHighlightAlpha * 0.32f),
                        Color.Transparent,
                    ),
                    startY = 0f,
                    endY = size.height * 0.52f,
                )
                val bottomShade = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = overlayAlpha * if (isDark) 0.16f else 0.08f),
                    ),
                    startY = size.height * 0.34f,
                    endY = size.height,
                )
                onDrawBehind {
                    drawRect(brush = diagonalTint)
                    drawRect(brush = topGlow)
                    drawRect(brush = bottomShade)
                }
            },
        ) {
            content()
        }
    }
}
