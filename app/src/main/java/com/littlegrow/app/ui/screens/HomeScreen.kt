package com.littlegrow.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.littlegrow.app.data.AgeBasedReference
import com.littlegrow.app.data.DiaperEntity
import com.littlegrow.app.data.FeedingEntity
import com.littlegrow.app.data.GrowthMetric
import com.littlegrow.app.data.HomeModule
import com.littlegrow.app.data.HomeSummary
import com.littlegrow.app.data.MemorySnapshot
import com.littlegrow.app.data.MonthlyGuideEntry
import com.littlegrow.app.data.RecordTab
import com.littlegrow.app.data.RoutineInsight
import com.littlegrow.app.data.SleepEntity
import com.littlegrow.app.data.TrendDirection
import com.littlegrow.app.data.TrendInsight
import com.littlegrow.app.data.VaccineEntity
import com.littlegrow.app.ui.BabyAvatar
import com.littlegrow.app.ui.PhotoPreviewCard
import com.littlegrow.app.ui.formatDate
import com.littlegrow.app.ui.formatDateTime
import com.littlegrow.app.ui.formatMetric
import com.littlegrow.app.ui.formatMinutes
import com.littlegrow.app.ui.components.ExpressiveFilledTonalButton as FilledTonalButton
import com.littlegrow.app.ui.components.ExpressiveFilterChip as FilterChip
import com.littlegrow.app.ui.components.ExpressiveTextButton as TextButton
import com.littlegrow.app.ui.components.EmptyState
import com.littlegrow.app.ui.components.GlassSurface
import com.littlegrow.app.ui.theme.Spacing
import com.littlegrow.app.ui.theme.ContentAlpha
import com.littlegrow.app.ui.theme.softShadow
import java.time.Duration
import java.time.temporal.ChronoUnit

@Composable
fun HomeScreen(
    summary: HomeSummary,
    activeModules: List<HomeModule>,
    weeklyTrends: List<TrendInsight>,
    routineInsights: List<RoutineInsight>,
    encouragementText: String,
    monthlyGuide: MonthlyGuideEntry?,
    memoryOfTheDay: MemorySnapshot?,
    vaccines: List<VaccineEntity>,
    caregivers: List<String>,
    caregiverFilter: String?,
    contentPadding: PaddingValues,
    onCaregiverFilterChange: (String?) -> Unit,
    onDismissGuide: (Int) -> Unit,
    onOpenRecords: (RecordTab) -> Unit,
    onOpenGrowth: () -> Unit,
    onOpenTimeline: () -> Unit,
    onOpenMedicalSummary: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = Spacing.lg,
            end = Spacing.lg,
            top = contentPadding.calculateTopPadding() + Spacing.lg,
            bottom = contentPadding.calculateBottomPadding() + Spacing.xl,
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        item {
            GlassSurface(
                modifier = Modifier.softShadow(),
                alpha = 0.55f
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
                ) {
                    val avatarPath = summary.profile?.avatarPath
                    val babyName = summary.profile?.name ?: "宝贝"
                    val greeting = when (java.time.LocalTime.now().hour) {
                        in 5..11 -> "早上好"
                        in 12..17 -> "下午好"
                        else -> "晚上好"
                    }
                    if (avatarPath.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = babyName.take(1),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        BabyAvatar(
                            avatarPath = avatarPath,
                            contentDescription = "宝宝头像",
                            modifier = Modifier.size(72.dp),
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = summary.profile?.name?.let { "$it， $greeting" } ?: "欢迎来到长呀长",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = summary.ageText,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text("记录宝宝今天的成长吧")
                    }
                }
            }
        }

        if (encouragementText.isNotBlank()) {
            item {
                ElevatedCard {
                    Text(
                        text = encouragementText,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }

        if (caregivers.isNotEmpty()) {
            item {
                ElevatedCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("看护人筛选", fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FilterChip(
                                selected = caregiverFilter == null,
                                onClick = { onCaregiverFilterChange(null) },
                                label = { Text("全部") },
                            )
                            caregivers.forEach { caregiver ->
                                FilterChip(
                                    selected = caregiverFilter == caregiver,
                                    onClick = { onCaregiverFilterChange(caregiver) },
                                    label = { Text(caregiver) },
                                )
                            }
                        }
                        Text(
                            caregiverFilter?.let { "当前只看 $it 的记录摘要。" } ?: "当前显示全部看护人的记录摘要。",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }

        item {
            QuickActionGrid(
                onOpenRecords = onOpenRecords,
                onOpenGrowth = onOpenGrowth,
                onOpenTimeline = onOpenTimeline,
                onOpenMedicalSummary = onOpenMedicalSummary,
                onOpenSettings = onOpenSettings,
            )
        }

        activeModules.forEach { module ->
            when (module) {
                HomeModule.TODAY_SUMMARY -> {
                    item { TodaySummarySection(summary = summary) }
                }

                HomeModule.RECENT_FEEDINGS -> {
                    item { SectionLabel("最近喂养") }
                    if (summary.recentFeedings.isEmpty()) {
                        item { EmptyRecordCard("当前筛选下还没有喂养记录。") }
                    } else {
                        items(summary.recentFeedings) { feeding ->
                            Box(modifier = Modifier.animateItem().padding(bottom = 8.dp)) {
                                HomeFeedingCard(feeding)
                            }
                        }
                    }
                }

                HomeModule.RECENT_SLEEP -> {
                    item { SectionLabel("最近睡眠") }
                    if (summary.recentSleeps.isEmpty()) {
                        item { EmptyRecordCard("当前筛选下还没有睡眠记录。") }
                    } else {
                        items(summary.recentSleeps) { sleep ->
                            Box(modifier = Modifier.animateItem().padding(bottom = 8.dp)) {
                                HomeSleepCard(sleep)
                            }
                        }
                    }
                }

                HomeModule.LATEST_GROWTH -> {
                    item { LatestGrowthCard(summary) }
                }

                HomeModule.MILESTONE -> {
                    item { LatestMilestoneCard(summary) }
                }

                HomeModule.VACCINE -> {
                    item { VaccineReminderCard(vaccines = vaccines) }
                }

                HomeModule.TREND -> {
                    item { TrendCard(trends = weeklyTrends) }
                }

                HomeModule.ROUTINE -> {
                    item { RoutineCard(routineInsights = routineInsights) }
                }

                HomeModule.MEMORY -> {
                    memoryOfTheDay?.let { memory ->
                        item { MemoryCard(memory) }
                    }
                }

                HomeModule.GUIDE -> {
                    monthlyGuide?.let { guide ->
                        item {
                            MonthlyGuideCard(
                                guide = guide,
                                onDismiss = { onDismissGuide(guide.month) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionGrid(
    onOpenRecords: (RecordTab) -> Unit,
    onOpenGrowth: () -> Unit,
    onOpenTimeline: () -> Unit,
    onOpenMedicalSummary: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    fun triggerHapticAndRun(action: () -> Unit) {
        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
        action()
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("快捷操作", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(onClick = { triggerHapticAndRun { onOpenRecords(RecordTab.FEEDING) } }) { Text("记喂奶") }
                FilledTonalButton(onClick = { triggerHapticAndRun { onOpenRecords(RecordTab.SLEEP) } }) { Text("记睡眠") }
                FilledTonalButton(onClick = { triggerHapticAndRun { onOpenRecords(RecordTab.DIAPER) } }) { Text("记尿布") }
                FilledTonalButton(onClick = { triggerHapticAndRun { onOpenRecords(RecordTab.MEDICAL) } }) { Text("健康") }
                FilledTonalButton(onClick = { triggerHapticAndRun { onOpenGrowth() } }) { Text("生长") }
                FilledTonalButton(onClick = { triggerHapticAndRun { onOpenMedicalSummary() } }) { Text("就医") }
                FilledTonalButton(onClick = { triggerHapticAndRun { onOpenTimeline() } }) { Text("里程碑") }
                FilledTonalButton(onClick = { triggerHapticAndRun { onOpenSettings() } }) { Text("设置") }
            }
        }
    }
}

@Composable
private fun TodaySummarySection(summary: HomeSummary) {
    val ageMonths = summary.profile?.birthday?.let { ChronoUnit.MONTHS.between(it, java.time.LocalDate.now()).toInt() }
    val feedingRange = ageMonths?.let(AgeBasedReference::feedingTimesPerDay)
    val sleepRange = ageMonths?.let(AgeBasedReference::sleepHoursPerDay)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel("今日摘要")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "喂养",
                value = "${summary.todayFeedings}",
            )
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "排泄",
                value = "${summary.todayDiapers}",
            )
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "睡眠(分)",
                value = "${summary.todaySleepMinutes}",
            )
        }
        val latestGrow = summary.latestGrowth?.let {
            "${it.date.formatDate()} 体重 ${it.weightKg.formatMetric(GrowthMetric.WEIGHT)}"
        } ?: "还没有生长记录。"
        Text(text = "最新生长：$latestGrow", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LatestGrowthCard(summary: HomeSummary) {
    SectionCard(
        title = "最近体重",
        emptyText = "还没有生长记录。",
    ) {
        val growth = summary.latestGrowth
        if (growth == null) {
            Text("还没有生长记录。", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Text("${growth.date.formatDate()} · 体重 ${growth.weightKg.formatMetric(GrowthMetric.WEIGHT)}")
            Text("身高 ${growth.heightCm.formatMetric(GrowthMetric.HEIGHT)} · 头围 ${growth.headCircCm.formatMetric(GrowthMetric.HEAD)}")
        }
    }
}

@Composable
private fun LatestMilestoneCard(summary: HomeSummary) {
    SectionCard(
        title = "最近里程碑",
        emptyText = "还没有里程碑记录。",
    ) {
        val milestone = summary.latestMilestone
        if (milestone == null) {
            Text("还没有里程碑记录。", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Text(milestone.title, fontWeight = FontWeight.SemiBold)
            Text("${milestone.category.label} · ${milestone.achievedDate.formatDate()}")
            milestone.note?.let {
                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun VaccineReminderCard(vaccines: List<VaccineEntity>) {
    val pending = vaccines.filterNot { it.isDone }.sortedBy { it.scheduledDate }
    val overdue = pending.filter { it.scheduledDate.isBefore(java.time.LocalDate.now().minusDays(30)) }
    SectionCard(
        title = "疫苗提醒",
        emptyText = "当前没有待接种疫苗。",
    ) {
        if (pending.isEmpty()) {
            Text("当前没有待接种疫苗。", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            val nearest = pending.first()
            Text("${nearest.vaccineName} 第 ${nearest.doseNumber} 针", fontWeight = FontWeight.SemiBold)
            Text("建议日期 ${nearest.scheduledDate.formatDate()}")
            if (overdue.isNotEmpty()) {
                Text("已有 ${overdue.size} 项逾期超过 30 天，建议尽快补种。", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun TrendCard(trends: List<TrendInsight>) {
    SectionCard(
        title = "本周趋势",
        emptyText = "最近两周数据还不够，暂时看不出明显变化。",
    ) {
        if (trends.isEmpty()) {
            Text("最近两周数据还不够，暂时看不出明显变化。", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                trends.take(3).forEach { trend ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ReferenceBadgeComposable(
                            text = trend.category,
                            color = when (trend.direction) {
                                TrendDirection.UP -> MaterialTheme.colorScheme.tertiaryContainer
                                TrendDirection.DOWN -> MaterialTheme.colorScheme.errorContainer
                                TrendDirection.STABLE -> MaterialTheme.colorScheme.surfaceVariant
                            },
                        )
                        Text(trend.description, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutineCard(routineInsights: List<RoutineInsight>) {
    SectionCard(
        title = "作息规律",
        emptyText = "再多记录几天，系统会帮你识别更明显的规律。",
    ) {
        if (routineInsights.isEmpty()) {
            Text("再多记录几天，系统会帮你识别更明显的规律。", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                routineInsights.forEach { insight ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(insight.title, fontWeight = FontWeight.SemiBold)
                        Text(insight.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryCard(memory: MemorySnapshot) {
    SectionCard(title = memory.title, emptyText = "暂无成长回忆。") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            memory.lines.forEach { line -> Text(line) }
            memory.photoPaths.forEach { path ->
                PhotoPreviewCard(filePath = path, contentDescription = memory.title)
            }
        }
    }
}

@Composable
private fun MonthlyGuideCard(
    guide: MonthlyGuideEntry,
    onDismiss: () -> Unit,
) {
    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(guide.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = onDismiss) {
                    Text("知道了")
                }
            }
            GuideBlock("发育特点", guide.developmentHighlights)
            GuideBlock("喂养建议", guide.feedingTips)
            GuideBlock("睡眠建议", guide.sleepTips)
            GuideBlock("照护提醒", guide.careTips)
        }
    }
}

@Composable
private fun GuideBlock(
    title: String,
    items: List<String>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold)
        items.forEach { item ->
            Text("- $item", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    emptyText: String,
    content: @Composable () -> Unit,
) {
    ElevatedCard(modifier = Modifier.softShadow()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun EmptyRecordCard(text: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        EmptyState(
            title = "暂无记录",
            description = text,
            illustration = {
                Icon(
                    imageVector = Icons.Rounded.Today,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        )
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    badge: ReferenceBadgeData? = null,
    subtitle: String? = null,
) {
    GlassSurface(modifier = modifier, alpha = 0.65f) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.medium))
            Text(value, style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            badge?.let {
                ReferenceBadgeComposable(text = it.text, color = it.color)
            }
            subtitle?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.medium))
            }
        }
    }
}

@Composable
private fun HomeFeedingCard(feeding: FeedingEntity) {
    ElevatedCard(modifier = Modifier.softShadow()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(feeding.type.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(feeding.happenedAt.formatDateTime(), style = MaterialTheme.typography.bodyMedium)
            val details = buildList {
                feeding.durationMinutes?.let { add("${it} 分钟") }
                feeding.amountMl?.let { add("${it} ml") }
                feeding.foodName?.let { add(it) }
                feeding.caregiver?.let { add("记录人 $it") }
                feeding.note?.let { add(it) }
            }
            if (details.isNotEmpty()) {
                Text(details.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HomeSleepCard(sleep: SleepEntity) {
    ElevatedCard(modifier = Modifier.softShadow()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("睡眠", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("${sleep.startTime.formatDateTime()} - ${sleep.endTime.formatDateTime()}", style = MaterialTheme.typography.bodyMedium)
            Text(
                Duration.between(sleep.startTime, sleep.endTime).toMinutes().formatMinutes(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            )
            Text(
                listOfNotNull(sleep.sleepType.label, sleep.fallingAsleepMethod?.label, sleep.caregiver?.let { "记录人 $it" }).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            sleep.note?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private data class ReferenceBadgeData(
    val text: String,
    val color: Color,
)

@Composable
private fun ReferenceBadgeComposable(
    text: String,
    color: Color,
) {
    Surface(
        color = color,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

private fun referenceColor(
    value: Double,
    min: Double,
    max: Double,
): Color {
    return when {
        value < min -> Color(0xFFFFE7C2)
        value > max -> Color(0xFFFFD7C9)
        else -> Color(0xFFD9F1D7)
    }
}
