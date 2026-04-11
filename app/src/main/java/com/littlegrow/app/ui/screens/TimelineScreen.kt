package com.littlegrow.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.Surface
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
import com.littlegrow.app.ui.components.ExpressiveTextButton as TextButton
import com.littlegrow.app.ui.theme.ContentAlpha
import com.littlegrow.app.ui.theme.Spacing
import com.littlegrow.app.ui.theme.softShadow
import com.littlegrow.app.ui.dateFormatter
import com.littlegrow.app.ui.formatDate
import com.littlegrow.app.ui.rememberManagedPhotoAttachment
import java.time.Duration
import java.time.LocalDate
import java.time.temporal.ChronoUnit

val TimelineLineOffset = 24.dp
val TimelineContentStart = 48.dp

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
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("时光轨迹", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(
                        "记录宝宝的每一次进步，从第一次翻身到开口叫妈妈。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
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
                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(milestone.id) { isVisible = true }
                    val fromLeft = index % 2 == 0

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn() + slideInHorizontally(
                            initialOffsetX = { if (fromLeft) -it / 3 else it / 3 },
                            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                        )
                    ) {
                        TimelineRow(
                            isFirst = index == 0,
                            isLast = index == totalMilestones - 1,
                            category = milestone.category,
                        ) {
                            GlassSurface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .softShadow(elevation = 10.dp),
                                shape = MaterialTheme.shapes.large,
                                alpha = 0.52f,
                                accentColor = milestoneCategoryColors[milestone.category]
                                    ?: MaterialTheme.colorScheme.primary,
                                shadowElevation = 0.dp,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Spacing.lg),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                                ) {
                                    // Title + day badge
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            milestone.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f),
                                        )
                                        profile?.birthday?.let { birthday ->
                                            val day = ChronoUnit.DAYS.between(birthday, milestone.achievedDate) + 1
                                            Surface(
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                                                shape = MaterialTheme.shapes.small,
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 4.dp),
                                                    verticalAlignment = Alignment.Bottom,
                                                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                                                ) {
                                                    Text(
                                                        "第",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    )
                                                    Text(
                                                        "$day",
                                                        style = MaterialTheme.typography.displaySmall,
                                                        color = MaterialTheme.colorScheme.primary,
                                                    )
                                                    Text(
                                                        "天",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Date + category
                                    val dateStr = "${milestone.achievedDate.formatDate()} · ${milestone.category.label}"
                                    Text(
                                        dateStr,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = ContentAlpha.medium),
                                    )

                                    milestone.note?.let {
                                        Text(it, style = MaterialTheme.typography.bodyMedium)
                                    }

                                    milestone.photoPath?.let {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = Spacing.sm)
                                                .softShadow(elevation = 8.dp, shape = MaterialTheme.shapes.large)
                                                .clip(MaterialTheme.shapes.large)
                                        ) {
                                            PhotoPreviewCard(
                                                filePath = it,
                                                contentDescription = "${dateStr}的里程碑照片 - ${milestone.title}",
                                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                    ) {
                                        TextButton(onClick = {
                                            editingMilestone = milestone
                                            showDialog = true
                                        }) { Text("编辑") }
                                        TextButton(onClick = { onDeleteMilestone(milestone.id) }) {
                                            Text("删除", color = MaterialTheme.colorScheme.error)
                                        }
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

@Composable
private fun TimelineRow(
    isFirst: Boolean,
    isLast: Boolean,
    category: MilestoneCategory,
    content: @Composable () -> Unit
) {
    val nodeColor = milestoneCategoryColors[category] ?: MaterialTheme.colorScheme.primary
    val icon = milestoneCategoryIcons[category] ?: Icons.Rounded.Star
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    // Ribbon: warm primary→tertiary gradient for full visual continuity
    val ribbonBrush = Brush.verticalGradient(listOf(nodeColor, primaryColor, tertiaryColor))
    val fadeInBrush = Brush.verticalGradient(
        listOf(Color.Transparent, primaryColor.copy(alpha = 0.3f), nodeColor)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val nodeY = 28.dp.toPx()
                val lineX = TimelineLineOffset.toPx()
                val strokeW = 4.dp.toPx()
                val glowW = 10.dp.toPx()

                if (!isFirst) {
                    // Glow halo behind ribbon
                    drawLine(
                        nodeColor.copy(alpha = 0.12f),
                        androidx.compose.ui.geometry.Offset(lineX, 0f),
                        androidx.compose.ui.geometry.Offset(lineX, nodeY - 14.dp.toPx()),
                        strokeWidth = glowW,
                    )
                    drawLine(
                        fadeInBrush,
                        androidx.compose.ui.geometry.Offset(lineX, 0f),
                        androidx.compose.ui.geometry.Offset(lineX, nodeY - 14.dp.toPx()),
                        strokeWidth = strokeW,
                    )
                }
                if (!isLast) {
                    // Glow halo below node
                    drawLine(
                        nodeColor.copy(alpha = 0.1f),
                        androidx.compose.ui.geometry.Offset(lineX, nodeY + 14.dp.toPx()),
                        androidx.compose.ui.geometry.Offset(lineX, size.height),
                        strokeWidth = glowW,
                    )
                    drawLine(
                        ribbonBrush,
                        androidx.compose.ui.geometry.Offset(lineX, nodeY + 14.dp.toPx()),
                        androidx.compose.ui.geometry.Offset(lineX, size.height),
                        strokeWidth = strokeW,
                    )
                }
            }
            .padding(bottom = if (isLast) 0.dp else 28.dp)
    ) {
        // Node: outer glow ring + filled circle with icon
        Box(
            modifier = Modifier
                .padding(start = TimelineLineOffset - 14.dp, top = 14.dp)
                .size(28.dp)
                .drawBehind {
                    // Soft glow ring
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(nodeColor.copy(alpha = 0.35f), Color.Transparent),
                            radius = size.minDimension * 0.9f,
                        )
                    )
                }
                .background(nodeColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Box(modifier = Modifier.padding(start = TimelineContentStart - TimelineLineOffset - 14.dp, end = 16.dp)) {
            content()
        }
    }
}

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
