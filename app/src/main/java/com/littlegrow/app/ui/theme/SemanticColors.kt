package com.littlegrow.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.littlegrow.app.data.MilestoneCategory

class SemanticColors(
    val feedingAccent: Color,
    val feedingAccentBg: Color,
    val sleepAccent: Color,
    val sleepAccentBg: Color,
    val diaperAccent: Color,
    val diaperAccentBg: Color,
    val referenceNormal: Color,
    val referenceBelow: Color,
    val referenceAbove: Color,
    val milestoneCategoryColors: Map<MilestoneCategory, Color>,
    val carouselPlaceholders: List<Pair<Color, Color>>
)

val lightSemanticColors = SemanticColors(
    feedingAccent = Color(0xFFE65100),
    feedingAccentBg = Color(0xFFFFF3E0),
    sleepAccent = Color(0xFF1565C0),
    sleepAccentBg = Color(0xFFE3F2FD),
    diaperAccent = Color(0xFF00695C),
    diaperAccentBg = Color(0xFFE0F2F1),
    referenceNormal = Color(0xFF2E7D32),
    referenceBelow = Color(0xFFC62828),
    referenceAbove = Color(0xFFC62828),
    milestoneCategoryColors = mapOf(
        MilestoneCategory.GROSS_MOTOR to Color(0xFFD32F2F),
        MilestoneCategory.FINE_MOTOR to Color(0xFF1976D2),
        MilestoneCategory.LANGUAGE to Color(0xFFFBC02D),
        MilestoneCategory.SOCIAL to Color(0xFF388E3C),
        MilestoneCategory.COGNITIVE to Color(0xFF7B1FA2)
    ),
    carouselPlaceholders = listOf(
        Color(0xFFFFE0B2) to Color(0xFFFFCC80),
        Color(0xFFBBDEFB) to Color(0xFF90CAF9),
        Color(0xFFC8E6C9) to Color(0xFFA5D6A7),
        Color(0xFFF8BBD0) to Color(0xFFF48FB1),
        Color(0xFFD1C4E9) to Color(0xFFCE93D8)
    )
)

val darkSemanticColors = SemanticColors(
    feedingAccent = Color(0xFFFFB74D),
    feedingAccentBg = Color(0xFF5D4037),
    sleepAccent = Color(0xFF64B5F6),
    sleepAccentBg = Color(0xFF0D47A1),
    diaperAccent = Color(0xFF4DB6AC),
    diaperAccentBg = Color(0xFF004D40),
    referenceNormal = Color(0xFF81C784),
    referenceBelow = Color(0xFFE57373),
    referenceAbove = Color(0xFFE57373),
    milestoneCategoryColors = mapOf(
        MilestoneCategory.GROSS_MOTOR to Color(0xFFEF5350),
        MilestoneCategory.FINE_MOTOR to Color(0xFF42A5F5),
        MilestoneCategory.LANGUAGE to Color(0xFFFFCA28),
        MilestoneCategory.SOCIAL to Color(0xFF66BB6A),
        MilestoneCategory.COGNITIVE to Color(0xFFAB47BC)
    ),
    carouselPlaceholders = listOf(
        Color(0xFF5D4037) to Color(0xFF4E342E),
        Color(0xFF37474F) to Color(0xFF263238),
        Color(0xFF1B5E20) to Color(0xFF2E7D32),
        Color(0xFF880E4F) to Color(0xFF4A148C),
        Color(0xFF311B92) to Color(0xFF4527A0)
    )
)

val LocalSemanticColors = staticCompositionLocalOf { lightSemanticColors }

val androidx.compose.material3.MaterialTheme.semanticColors: SemanticColors
    @Composable
    @ReadOnlyComposable
    get() = LocalSemanticColors.current
