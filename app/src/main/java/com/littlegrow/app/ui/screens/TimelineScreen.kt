package com.littlegrow.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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
import com.littlegrow.app.ui.components.staggeredFadeSlideIn
import com.littlegrow.app.ui.theme.Spacing
import com.littlegrow.app.ui.theme.semanticColors
import com.littlegrow.app.ui.components.ExpressiveTextButton as TextButton
import com.littlegrow.app.ui.dateFormatter
import com.littlegrow.app.ui.formatDate
import com.littlegrow.app.ui.rememberManagedPhotoAttachment
import java.time.Duration
import java.time.LocalDate
import java.time.temporal.ChronoUnit

val TimelineLineOffset = 24.dp
val TimelineContentStart = 48.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    profile: BabyProfile?,
    feedings: List<FeedingEntity>,
    sleeps: List<SleepEntity>,
    diapers: List<DiaperEntity>,
    growthRecords: List<GrowthEntity>,
    milestones: List<MilestoneEntity>,
    refreshing: Boolean,
    contentPadding: PaddingValues,
    onRefresh: () -> Unit,
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

    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = onRefresh,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = contentPadding.calculateTopPadding() + Spacing.lg,
                    bottom = contentPadding.calculateBottomPadding() + 96.dp,
                ),
                modifier = Modifier.semantics { contentDescription = "时间线" }
            ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    Text("时光轨迹", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(
                        "记录宝宝的每一次进步，从第一次翻身到开口叫妈妈。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(Spacing.lg))
                }
            }

            if (milestones.isNotEmpty()) {
                item {
                    MomentsCarousel(milestones = milestones)
                }
            }

            if (milestones.isEmpty()) {
                item {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Icon(Icons.Rounded.Star, contentDescription = "暂无数据", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
                            Text("还没有里程碑记录。点击下方按钮添加第一条吧！", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                itemsIndexed(milestones, key = { _, m -> m.id }) { index, milestone ->
                    Box(modifier = Modifier.staggeredFadeSlideIn(index)) {
                        TimelineRow(
                            isFirst = index == 0,
                            isLast = index == totalMilestones - 1,
                            category = milestone.category,
                        ) {
                            Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(milestone.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        profile?.birthday?.let { birthday ->
                                            val day = ChronoUnit.DAYS.between(birthday, milestone.achievedDate) + 1
                                            Surface(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(16.dp),
                                                shadowElevation = 2.dp,
                                            ) {
                                                Text(
                                                    "出生第 $day 天",
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    val dateStr = "${milestone.achievedDate.formatDate()} · ${milestone.category.label}"
                                    Text(dateStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                    milestone.note?.let {
                                        Text(it, style = MaterialTheme.typography.bodyMedium)
                                    }

                                    milestone.photoPath?.let {
                                        Box(modifier = Modifier
                                            .padding(top = 8.dp)
                                            .shadow(4.dp, RoundedCornerShape(16.dp))
                                            .clip(RoundedCornerShape(16.dp))
                                        ) {
                                            PhotoPreviewCard(filePath = it, contentDescription = "${dateStr}的里程碑照片 - ${milestone.title}", modifier = Modifier.fillMaxWidth().height(200.dp))
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                    ) {
                                        TextButton(
                                            onClick = {
                                                editingMilestone = milestone
                                                showDialog = true
                                            },
                                        ) {
                                            Text("编辑")
                                        }
                                        TextButton(onClick = { onDeleteMilestone(milestone.id) }) {
                                            Text("删除", color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
            }

            if (stageReports.isNotEmpty() || monthlyGuides.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Box(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)).fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(
                            "发展指南与总结",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
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
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                    Text(guide.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                                }
                                Text(guide.developmentHighlights.joinToString(" • "), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
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

val milestoneCategoryColors: Map<MilestoneCategory, Color>
    @Composable
    get() = mapOf(
        MilestoneCategory.GROSS_MOTOR to (MaterialTheme.semanticColors.milestoneCategoryColors[MilestoneCategory.GROSS_MOTOR] ?: MaterialTheme.colorScheme.primary),
        MilestoneCategory.FINE_MOTOR to (MaterialTheme.semanticColors.milestoneCategoryColors[MilestoneCategory.FINE_MOTOR] ?: MaterialTheme.colorScheme.primary),
        MilestoneCategory.LANGUAGE to (MaterialTheme.semanticColors.milestoneCategoryColors[MilestoneCategory.LANGUAGE] ?: MaterialTheme.colorScheme.primary),
        MilestoneCategory.SOCIAL to (MaterialTheme.semanticColors.milestoneCategoryColors[MilestoneCategory.SOCIAL] ?: MaterialTheme.colorScheme.primary),
        MilestoneCategory.COGNITIVE to (MaterialTheme.semanticColors.milestoneCategoryColors[MilestoneCategory.COGNITIVE] ?: MaterialTheme.colorScheme.primary),
    )

@Composable
private fun MomentsCarousel(milestones: List<MilestoneEntity>) {
    val placeholders = MaterialTheme.semanticColors.carouselPlaceholders
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
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
            )
            Text(
                "最新 ${milestones.size} 条",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            itemsIndexed(milestones.take(5), key = { _, milestone -> "carousel_${milestone.id}" }) { index, milestone ->
                val (startColor, endColor) = placeholders[index % placeholders.size]
                Box(
                    modifier = Modifier
                        .width(240.dp)
                        .aspectRatio(4f / 5f)
                        .clip(RoundedCornerShape(Spacing.lg))
                        .background(
                            Brush.linearGradient(
                                listOf(startColor, endColor),
                            )
                        )
                ) {
                    milestone.photoPath?.let {
                        PhotoPreviewCard(
                            filePath = it,
                            contentDescription = milestone.title,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.38f)),
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        Text(
                            milestone.achievedDate.formatDate(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.84f),
                        )
                        Text(
                            milestone.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 2,
                        )
                    }
                }
            }
        }
    }
}

val milestoneCategoryIcons: Map<MilestoneCategory, ImageVector> = mapOf(
    MilestoneCategory.GROSS_MOTOR to Icons.Rounded.DirectionsRun,
    MilestoneCategory.FINE_MOTOR to Icons.Rounded.SignLanguage,
    MilestoneCategory.LANGUAGE to Icons.Rounded.RecordVoiceOver,
    MilestoneCategory.SOCIAL to Icons.Rounded.WavingHand,
    MilestoneCategory.COGNITIVE to Icons.Rounded.Psychology,
)

@Composable
private fun TimelineRow(
    isFirst: Boolean,
    isLast: Boolean,
    category: MilestoneCategory,
    content: @Composable () -> Unit
) {
    val nodeColor = milestoneCategoryColors[category] ?: MaterialTheme.colorScheme.primary
    val icon = milestoneCategoryIcons[category] ?: Icons.Rounded.Star
    val lineColor = timelineStageColor(category)
    val gradientBrush = Brush.verticalGradient(listOf(nodeColor, lineColor))
    val latestPulse = rememberInfiniteTransition(label = "timeline_pulse")
    val pulseScale by latestPulse.animateFloat(
        initialValue = 0.86f,
        targetValue = 1.24f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_scale",
    )
    val pulseAlpha by latestPulse.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.32f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_alpha",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val nodeY = 24.dp.toPx()
                val lineX = TimelineLineOffset.toPx()
                val strokeW = 3.dp.toPx()
                
                if (!isFirst) {
                    drawLine(lineColor, androidx.compose.ui.geometry.Offset(lineX, 0f), androidx.compose.ui.geometry.Offset(lineX, nodeY - 12.dp.toPx()), strokeWidth = strokeW)
                }
                if (!isLast) {
                    drawLine(gradientBrush, androidx.compose.ui.geometry.Offset(lineX, nodeY + 12.dp.toPx()), androidx.compose.ui.geometry.Offset(lineX, size.height), strokeWidth = strokeW)
                }
            }
            .padding(bottom = if (isLast) 0.dp else 32.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(start = TimelineLineOffset - 12.dp, top = 12.dp)
                .size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isFirst) {
                Box(
                    modifier = Modifier
                        .size((24 * pulseScale).dp)
                        .background(nodeColor.copy(alpha = pulseAlpha), CircleShape),
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(nodeColor, CircleShape),
            )
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Box(modifier = Modifier.padding(start = TimelineContentStart - TimelineLineOffset - 12.dp, end = 16.dp)) {
            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                alpha = 0.66f,
                shape = RoundedCornerShape(22.dp),
                accentColor = nodeColor,
                shadowElevation = 14.dp,
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun timelineStageColor(category: MilestoneCategory): Color =
    MaterialTheme.semanticColors.milestoneCategoryColors[category] ?: MaterialTheme.colorScheme.primary

@Composable
private fun StageReportCard(report: StageReportEntry) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
        Box(modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surface)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("年度陪伴回顾", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                Text("在这里，每一个日夜都被温柔记录。", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("🍼 累计喂养记录：${feedings.size} 次", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "累计母乳时长 ${feedings.sumOf { it.durationMinutes ?: 0 }} 分钟，为宝宝换了 ${diapers.size} 次带来干爽的纸尿裤。",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    "🌙 默默陪伴的黑夜：累计夜醒约 $totalNightWakes 次。辛苦了，每一个不眠之夜都闪耀着母爱的光芒。",
                    style = MaterialTheme.typography.bodyMedium,
                )
                busiestDay?.let {
                    Text(
                        "🔥 最充实的一天：${it.key.formatDate()}，当天记录了 ${it.value} 次喂养，一定非常忙碌吧。",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

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
