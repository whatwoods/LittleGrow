package com.littlegrow.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.SignLanguage
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WavingHand
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.littlegrow.app.data.BabyProfile
import com.littlegrow.app.data.DiaperEntity
import com.littlegrow.app.data.FeedingEntity
import com.littlegrow.app.data.GrowthEntity
import com.littlegrow.app.data.MilestoneCategory
import com.littlegrow.app.data.MilestoneDraft
import com.littlegrow.app.data.MilestoneEntity
import com.littlegrow.app.data.MonthlyGuide
import com.littlegrow.app.data.StageReportEntry
import com.littlegrow.app.data.StageReportGenerator
import com.littlegrow.app.data.SleepEntity
import com.littlegrow.app.ui.NativeDatePickerField
import com.littlegrow.app.ui.PhotoActionRow
import com.littlegrow.app.ui.PhotoPreviewCard
import com.littlegrow.app.ui.components.GlassSurface
import com.littlegrow.app.ui.components.ExpressiveTextButton as TextButton
import com.littlegrow.app.ui.theme.Spacing
import com.littlegrow.app.ui.theme.softShadow
import com.littlegrow.app.ui.dateFormatter
import com.littlegrow.app.ui.formatDate
import com.littlegrow.app.ui.rememberManagedPhotoAttachment
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// Timeline layout constants
private val TimelineNodeLineX = 20.dp
private val TimelineNodeSize = 32.dp
private val TimelineGap = 24.dp
private val CardShape = RoundedCornerShape(16.dp)

/** Decorative gradient pairs for carousel placeholder backgrounds. */
private val carouselPlaceholderColors = listOf(
    Color(0xFFFFE0B2) to Color(0xFFF57C00),
    Color(0xFFB3E5FC) to Color(0xFF0288D1),
    Color(0xFFC8E6C9) to Color(0xFF388E3C),
    Color(0xFFF8BBD0) to Color(0xFFC2185B),
    Color(0xFFD1C4E9) to Color(0xFF7B1FA2),
)

private val carouselDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

@Composable
fun TimelineScreen(
    profile: BabyProfile?,
    feedings: List<FeedingEntity>,
    sleeps: List<SleepEntity>,
    diapers: List<DiaperEntity>,
    growthRecords: List<GrowthEntity>,
    milestones: List<MilestoneEntity>,
    contentPadding: PaddingValues,
    onAddMilestone: (MilestoneDraft) -> Unit,
    onUpdateMilestone: (Long, MilestoneDraft) -> Unit,
    onDeleteMilestone: (Long) -> Unit,
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var editingMilestone by remember { mutableStateOf<MilestoneEntity?>(null) }
    val ageDays = profile?.birthday?.let { ChronoUnit.DAYS.between(it, LocalDate.now()).toInt() } ?: 0
    val stageReports = profile?.birthday?.let { birthday ->
        StageReportGenerator.reachedReports(
            birthday = birthday,
            ageDays = ageDays,
            feedings = feedings,
            sleeps = sleeps,
            diapers = diapers,
            growthRecords = growthRecords,
            milestones = milestones,
        )
    }.orEmpty()
    val monthlyGuides = (0..(profile?.birthday?.let { ChronoUnit.MONTHS.between(it, LocalDate.now()).toInt() } ?: 0).coerceAtMost(24))
        .mapNotNull(MonthlyGuide::guideFor)
        .reversed()

    val totalMilestones = milestones.size

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding() + 16.dp,
                bottom = contentPadding.calculateBottomPadding() + 96.dp,
            ),
            modifier = Modifier.semantics { contentDescription = "时间线" }
        ) {
            // -- Moments Carousel --
            if (milestones.isNotEmpty()) {
                item {
                    MomentsCarousel(milestones = milestones)
                    Spacer(modifier = Modifier.height(Spacing.xl))
                }
            }

            // -- Growth Timeline --
            if (milestones.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Icon(
                                Icons.Rounded.Star,
                                contentDescription = "暂无数据",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.surfaceVariant,
                            )
                            Text(
                                "还没有里程碑记录。点击下方按钮添加第一条吧！",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(milestones, key = { _, m -> m.id }) { index, milestone ->
                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(milestone.id) { isVisible = true }

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn() + slideInHorizontally(
                            initialOffsetX = { -it / 3 },
                            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                        )
                    ) {
                        TimelineRow(
                            isFirst = index == 0,
                            isLast = index == totalMilestones - 1,
                            isImportant = index == 0,
                        ) {
                            TimelineCard(
                                milestone = milestone,
                                profile = profile,
                                isImportant = index == 0,
                                onEdit = {
                                    editingMilestone = milestone
                                    showDialog = true
                                },
                                onDelete = { onDeleteMilestone(milestone.id) },
                            )
                        }
                    }
                }
            }

            // -- Development guides & reports --
            if (stageReports.isNotEmpty() || monthlyGuides.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    GlassSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        alpha = 0.58f,
                        shape = MaterialTheme.shapes.large,
                        accentColor = MaterialTheme.colorScheme.primary,
                        shadowElevation = 10.dp,
                    ) {
                        Text(
                            "发展指南与总结",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            if (stageReports.isNotEmpty()) {
                itemsIndexed(stageReports, key = { _, it -> "sr_" + it.day }) { _, report ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        StageReportCard(report)
                    }
                }
            }

            if (monthlyGuides.isNotEmpty()) {
                itemsIndexed(monthlyGuides, key = { _, it -> "mg_" + it.month }) { _, guide ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                    Text(guide.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                                }
                                Text(
                                    guide.developmentHighlights.joinToString(" • "),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }

            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
                    YearlySummaryCard(
                        feedings = feedings,
                        sleeps = sleeps,
                        diapers = diapers,
                    )
                }
            }
        }
    }

    if (showDialog) {
        AddMilestoneDialog(
            initial = editingMilestone,
            onDismiss = {
                editingMilestone = null
                showDialog = false
            },
            onSubmit = { draft ->
                val editing = editingMilestone
                if (editing == null) {
                    onAddMilestone(draft)
                } else {
                    onUpdateMilestone(editing.id, draft)
                }
                editingMilestone = null
                showDialog = false
            },
        )
    }
}

// ---------------------------------------------------------------------------
// Moments Carousel
// ---------------------------------------------------------------------------

@Composable
private fun MomentsCarousel(milestones: List<MilestoneEntity>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        // Section header: title + "view all" link
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "精彩瞬间",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "查看全部",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        // Horizontal scrolling cards
        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            itemsIndexed(milestones, key = { _, m -> "carousel_${m.id}" }) { index, milestone ->
                MomentCard(milestone = milestone, index = index)
            }
        }
    }
}

@Composable
private fun MomentCard(milestone: MilestoneEntity, index: Int) {
    val (bgLight, bgDark) = carouselPlaceholderColors[index % carouselPlaceholderColors.size]
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .width(256.dp)
            .aspectRatio(4f / 5f)
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Decorative gradient background as placeholder
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(bgLight, bgDark),
                        start = Offset.Zero,
                        end = Offset(600f, 600f),
                    )
                )
        )

        // If there is a real photo, show it on top of the placeholder
        milestone.photoPath?.let {
            PhotoPreviewCard(
                filePath = it,
                contentDescription = milestone.title,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Bottom gradient overlay: from primary/60 to transparent (bottom-up)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            primaryColor.copy(alpha = 0.60f),
                        ),
                    )
                )
        )

        // Date + title at bottom-left over the gradient
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(Spacing.lg),
        ) {
            Text(
                milestone.achievedDate.format(carouselDateFormatter),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.80f),
            )
            Text(
                milestone.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Growth Timeline components
// ---------------------------------------------------------------------------

val milestoneCategoryColors: Map<MilestoneCategory, Color> = mapOf(
    MilestoneCategory.GROSS_MOTOR to Color(0xFFF57C00),
    MilestoneCategory.FINE_MOTOR to Color(0xFF1976D2),
    MilestoneCategory.LANGUAGE to Color(0xFF388E3C),
    MilestoneCategory.SOCIAL to Color(0xFFC2185B),
    MilestoneCategory.COGNITIVE to Color(0xFF7B1FA2),
)

val milestoneCategoryIcons: Map<MilestoneCategory, ImageVector> = mapOf(
    MilestoneCategory.GROSS_MOTOR to Icons.Rounded.DirectionsRun,
    MilestoneCategory.FINE_MOTOR to Icons.Rounded.SignLanguage,
    MilestoneCategory.LANGUAGE to Icons.Rounded.RecordVoiceOver,
    MilestoneCategory.SOCIAL to Icons.Rounded.WavingHand,
    MilestoneCategory.COGNITIVE to Icons.Rounded.Psychology,
)

/**
 * A single timeline row: left gradient axis + node + card content to the right.
 */
@Composable
private fun TimelineRow(
    isFirst: Boolean,
    isLast: Boolean,
    isImportant: Boolean,
    content: @Composable () -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val surfaceContainerHighColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val surfaceColor = MaterialTheme.colorScheme.surface

    // Left gradient axis: primary/20 via primary/5 to transparent
    val axisBrush = Brush.verticalGradient(
        colors = listOf(
            primaryColor.copy(alpha = 0.20f),
            primaryColor.copy(alpha = 0.05f),
            Color.Transparent,
        ),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Spacing.lg, end = Spacing.lg)
            .drawBehind {
                // Draw the 4dp-wide left gradient axis
                val lineX = TimelineNodeLineX.toPx()
                val strokeW = 4.dp.toPx()
                val topY = if (isFirst) (TimelineNodeSize / 2).toPx() else 0f
                val bottomY = if (isLast) (TimelineNodeSize / 2).toPx() else size.height

                drawLine(
                    brush = axisBrush,
                    start = Offset(lineX, topY),
                    end = Offset(lineX, bottomY),
                    strokeWidth = strokeW,
                )
            }
            .padding(bottom = if (isLast) 0.dp else TimelineGap),
        verticalAlignment = Alignment.Top,
    ) {
        // Timeline node
        Box(
            modifier = Modifier
                .padding(top = Spacing.sm)
                .size(TimelineNodeSize),
            contentAlignment = Alignment.Center,
        ) {
            if (isImportant) {
                // Important/latest: primaryContainer circle, primary/20 shadow, surface 4dp border
                Box(
                    modifier = Modifier
                        .size(TimelineNodeSize)
                        .shadow(
                            elevation = 8.dp,
                            shape = CircleShape,
                            ambientColor = primaryColor.copy(alpha = 0.20f),
                            spotColor = primaryColor.copy(alpha = 0.20f),
                        )
                        .border(4.dp, surfaceColor, CircleShape)
                        .background(primaryContainerColor, CircleShape),
                )
            } else {
                // Normal: surfaceContainerHigh circle, surface 4dp border
                Box(
                    modifier = Modifier
                        .size(TimelineNodeSize)
                        .border(4.dp, surfaceColor, CircleShape)
                        .background(surfaceContainerHighColor, CircleShape),
                )
            }
        }

        Spacer(modifier = Modifier.width(Spacing.lg))

        // Card content fills the remaining space
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

/**
 * A single timeline card matching the stitch/_4 design:
 * surfaceContainerLowest/80 bg, backdrop blur via GlassSurface, rounded-xl (16dp),
 * subtle shadow, date label, optional image, title, description.
 */
@Composable
private fun TimelineCard(
    milestone: MilestoneEntity,
    profile: BabyProfile?,
    isImportant: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    // Build date label: "3月20日 · 5个月"
    val dateLabel = remember(milestone.achievedDate, profile?.birthday) {
        val month = milestone.achievedDate.monthValue
        val day = milestone.achievedDate.dayOfMonth
        val ageStr = profile?.birthday?.let { birthday ->
            val totalDays = ChronoUnit.DAYS.between(birthday, milestone.achievedDate)
            val months = totalDays / 30
            val remainingDays = totalDays % 30
            when {
                months > 0 && remainingDays > 0 -> "${months}个月${remainingDays}天"
                months > 0 -> "${months}个月"
                else -> "${totalDays}天"
            }
        }
        if (ageStr != null) "${month}月${day}日 · $ageStr" else "${month}月${day}日"
    }

    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(
                elevation = 6.dp,
                shape = CardShape,
                color = Color(0xFF825600).copy(alpha = 0.05f),
            ),
        shape = CardShape,
        alpha = 0.80f,
        accentColor = primaryColor,
        shadowElevation = 0.dp,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Decorative circle at top-right (matches mockup's bg-primary/5 circle)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(128.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.05f),
                                Color.Transparent,
                            ),
                        ),
                        shape = CircleShape,
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                // Date label (primary bold for important, onSurfaceVariant for normal)
                Text(
                    dateLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isImportant) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Optional image area
                milestone.photoPath?.let {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        PhotoPreviewCard(
                            filePath = it,
                            contentDescription = "${milestone.title}的照片",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.sm))

                // Title: headlineSmall bold
                Text(
                    milestone.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // Description (onSurfaceVariant)
                milestone.note?.let {
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Edit / Delete actions
                Spacer(modifier = Modifier.height(Spacing.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onEdit) { Text("编辑") }
                    TextButton(onClick = onDelete) {
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Stage reports & yearly summary (preserved from original)
// ---------------------------------------------------------------------------

@Composable
private fun StageReportCard(report: StageReportEntry) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(report.report.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            report.report.summary.forEach { line ->
                Text(line, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun YearlySummaryCard(
    feedings: List<FeedingEntity>,
    sleeps: List<SleepEntity>,
    diapers: List<DiaperEntity>,
) {
    val totalNightWakes = sleeps
        .sortedBy { it.startTime }
        .zipWithNext()
        .count { (previous, next) -> Duration.between(previous.endTime, next.startTime).toMinutes() > 10 }
    val busiestDay = feedings
        .groupingBy { it.happenedAt.toLocalDate() }
        .eachCount()
        .maxByOrNull { it.value }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surface,
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "年度陪伴回顾",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "在这里，每一个日夜都被温柔记录。",
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "\uD83C\uDF7C 累计喂养记录：${feedings.size} 次",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    "累计母乳时长 ${feedings.sumOf { it.durationMinutes ?: 0 }} 分钟，为宝宝换了 ${diapers.size} 次带来干爽的纸尿裤。",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    "\uD83C\uDF19 默默陪伴的黑夜：累计夜醒约 $totalNightWakes 次。辛苦了，每一个不眠之夜都闪耀着母爱的光芒。",
                    style = MaterialTheme.typography.bodyMedium,
                )
                busiestDay?.let {
                    Text(
                        "\uD83D\uDD25 最充实的一天：${it.key.formatDate()}，当天记录了 ${it.value} 次喂养，一定非常忙碌吧。",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Add / Edit Milestone Dialog (preserved from original)
// ---------------------------------------------------------------------------

@Composable
fun AddMilestoneDialog(
    initial: MilestoneEntity?,
    onDismiss: () -> Unit,
    onSubmit: (MilestoneDraft) -> Unit,
) {
    var title by rememberSaveable(initial?.id) { mutableStateOf(initial?.title.orEmpty()) }
    var category by rememberSaveable(initial?.id) { mutableStateOf(initial?.category ?: MilestoneCategory.GROSS_MOTOR) }
    var date by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.achievedDate?.format(dateFormatter) ?: LocalDate.now().format(dateFormatter))
    }
    var note by rememberSaveable(initial?.id) { mutableStateOf(initial?.note.orEmpty()) }
    var errorText by rememberSaveable(initial?.id) { mutableStateOf<String?>(null) }
    val photoAttachment = rememberManagedPhotoAttachment(
        initialPhotoPath = initial?.photoPath,
        photoTag = "milestone",
        onError = { errorText = it },
    )
    val photoPath = photoAttachment.photoPath

    AlertDialog(
        onDismissRequest = {
            photoAttachment.discardChanges()
            onDismiss()
        },
        title = { Text(if (initial == null) "添加里程碑" else "编辑里程碑") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("标题") },
                    singleLine = true,
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("类别", modifier = Modifier.align(Alignment.CenterVertically))
                }
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(MilestoneCategory.entries) { _, cat ->
                        androidx.compose.material3.FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.label) }
                        )
                    }
                }

                NativeDatePickerField(
                    value = date,
                    onValueChange = { date = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = "达成日期 (YYYY-MM-DD)",
                    supportingText = "点击选择日期",
                    maxDate = LocalDate.now(),
                )
                photoPath?.let {
                    PhotoPreviewCard(filePath = it, contentDescription = "里程碑照片预览")
                }
                PhotoActionRow(
                    hasPhoto = photoPath != null,
                    onTakePhoto = photoAttachment.onTakePhoto,
                    onPickPhoto = photoAttachment.onPickPhoto,
                    onRemovePhoto = photoAttachment.onRemovePhoto,
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("备注/感悟") },
                    maxLines = 3,
                )
                errorText?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsedDate = runCatching { LocalDate.parse(date.trim(), dateFormatter) }.getOrNull()
                    if (title.isBlank()) {
                        errorText = "里程碑标题不能为空。"
                    } else if (date.isBlank() || parsedDate == null) {
                        errorText = "请选择达成日期。"
                    } else {
                        photoAttachment.commitChanges()
                        onSubmit(
                            MilestoneDraft(
                                title = title,
                                category = category,
                                achievedDate = parsedDate,
                                photoPath = photoPath,
                                note = note,
                            ),
                        )
                    }
                },
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                photoAttachment.discardChanges()
                onDismiss()
            }) {
                Text("取消")
            }
        },
    )
}
