package com.littlegrow.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.littlegrow.app.ui.theme.ContentAlpha
import com.littlegrow.app.ui.theme.Spacing

/** Visual "tone" for card variants. Default keeps original M3 ElevatedCard style. */
enum class CardTone {
    Default,
    /** Left accent bar + title divider — keepsake journal feel */
    Journal,
    /** GlassSurface wrapper with icon on tertiaryContainer circle */
    Glass,
}

// ─── InfoCard ─────────────────────────────────────────────────────────────────

@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    title: String,
    tone: CardTone = CardTone.Default,
    content: @Composable ColumnScope.() -> Unit
) {
    when (tone) {
        CardTone.Journal -> InfoCardJournal(modifier, title, content)
        CardTone.Glass -> InfoCardGlass(modifier, title, content)
        CardTone.Default -> InfoCardDefault(modifier, title, content)
    }
}

@Composable
private fun InfoCardDefault(
    modifier: Modifier,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.medium),
                modifier = Modifier.padding(bottom = Spacing.sm)
            )
            content()
        }
    }
}

@Composable
private fun InfoCardJournal(
    modifier: Modifier,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val accentColor = MaterialTheme.colorScheme.primary
    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Row {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(accentColor, MaterialTheme.colorScheme.tertiary)
                        )
                    )
            )
            Column(modifier = Modifier.padding(Spacing.lg)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.medium),
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = Spacing.sm),
                    color = dividerColor,
                    thickness = 0.6.dp
                )
                content()
            }
        }
    }
}

@Composable
private fun InfoCardGlass(
    modifier: Modifier,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassSurface(modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.medium),
                modifier = Modifier.padding(bottom = Spacing.sm)
            )
            content()
        }
    }
}

// ─── ActionCard ───────────────────────────────────────────────────────────────

@Composable
fun ActionCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    tone: CardTone = CardTone.Default,
    onClick: () -> Unit,
    icon: @Composable (() -> Unit)? = null
) {
    when (tone) {
        CardTone.Glass -> ActionCardGlass(modifier, title, description, onClick, icon)
        else -> ActionCardDefault(modifier, title, description, onClick, icon)
    }
}

@Composable
private fun ActionCardDefault(
    modifier: Modifier,
    title: String,
    description: String?,
    onClick: () -> Unit,
    icon: @Composable (() -> Unit)?
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(modifier = Modifier.padding(end = Spacing.lg)) { icon() }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = ContentAlpha.medium)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionCardGlass(
    modifier: Modifier,
    title: String,
    description: String?,
    onClick: () -> Unit,
    icon: @Composable (() -> Unit)?
) {
    val tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer
    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        accentColor = MaterialTheme.colorScheme.tertiary,
    ) {
        Surface(
            onClick = onClick,
            color = Color.Transparent,
            shape = MaterialTheme.shapes.medium,
        ) {
            Row(
                modifier = Modifier.padding(Spacing.lg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .padding(end = Spacing.lg)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(tertiaryContainer),
                        contentAlignment = Alignment.Center,
                    ) { icon() }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                    if (description != null) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = ContentAlpha.medium)
                        )
                    }
                }
            }
        }
    }
}

// ─── MetricCard ───────────────────────────────────────────────────────────────

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: @Composable (() -> Unit)? = null
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (icon != null) {
                Box(modifier = Modifier.padding(bottom = Spacing.sm)) { icon() }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = ContentAlpha.medium)
            )
        }
    }
}

/**
 * Glass-wrapped metric card with a radial warm glow behind the value
 * and an `AnimatedContent` slide-up tick when [value] changes.
 */
@Composable
fun GlassMetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: @Composable (() -> Unit)? = null
) {
    val glowColor = MaterialTheme.colorScheme.primary
    GlassSurface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        accentColor = glowColor,
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (icon != null) {
                Box(modifier = Modifier.padding(bottom = Spacing.sm)) { icon() }
            }
            // Radial glow behind the value text
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = 0.18f),
                                Color.Transparent,
                            ),
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = size.minDimension * 1.1f,
                        )
                    )
                }
            ) {
                AnimatedContent(
                    targetState = value,
                    transitionSpec = {
                        slideInVertically { it } togetherWith slideOutVertically { -it }
                    },
                    label = "MetricValueTick"
                ) { targetValue ->
                    Text(
                        text = targetValue,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = ContentAlpha.medium)
            )
        }
    }
}

// ─── EmptyState ───────────────────────────────────────────────────────────────

@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    illustration: @Composable (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (illustration != null) {
            Box(modifier = Modifier.padding(bottom = Spacing.xl)) {
                illustration()
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        if (description != null) {
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.medium),
                textAlign = TextAlign.Center
            )
        }
        if (action != null) {
            Spacer(modifier = Modifier.height(Spacing.xl))
            action()
        }
    }
}
