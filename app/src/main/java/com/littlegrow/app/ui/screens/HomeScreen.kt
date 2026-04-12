package com.littlegrow.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.littlegrow.app.ui.components.CardTone
import com.littlegrow.app.ui.components.ExpressiveFilledTonalButton as FilledTonalButton
import com.littlegrow.app.ui.components.ExpressiveFilterChip as FilterChip
import com.littlegrow.app.ui.components.ExpressiveTextButton as TextButton
import com.littlegrow.app.ui.components.EmptyRecordCard
import com.littlegrow.app.ui.components.GlassSurface
import com.littlegrow.app.ui.components.InfoCard
import com.littlegrow.app.ui.components.staggeredFadeSlideIn
import com.littlegrow.app.ui.theme.semanticColors
import com.littlegrow.app.ui.theme.Spacing
import com.littlegrow.app.ui.theme.ContentAlpha
import com.littlegrow.app.ui.theme.softShadow
import java.time.Duration
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
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
    refreshing: Boolean,
    contentPadding: PaddingValues,
    onRefresh: () -> Unit,
    onCaregiverFilterChange: (String?) -> Unit,
    onDismissGuide: (Int) -> Unit,
    onOpenRecords: (RecordTab) -> Unit,
    onOpenGrowth: () -> Unit,
    onOpenTimeline: () -> Unit,
    onOpenMedicalSummary: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = onRefresh,
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                start = Spacing.lg,
                end = Spacing.lg,
                top = contentPadding.calculateTopPadding() + Spacing.lg,
                bottom = contentPadding.calculateBottomPadding() + Spacing.xl,
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl),
        ) {
            // 1. Hero Profile Card
            item {
                Box(modifier = Modifier.staggeredFadeSlideIn(0)) {
                    PostcardHeroCard(summary = summary)
                }
            }

            // 2. Stats Bento Grid
            item {
                Box(modifier = Modifier.staggeredFadeSlideIn(1)) {
                    StatsBentoGrid(summary = summary)
                }
            }

            // 3. Milestone Section
            item {
                Box(modifier = Modifier.staggeredFadeSlideIn(2)) {
                    MilestoneSection(
                        summary = summary,
                        onOpenTimeline = onOpenTimeline,
                    )
                }
            }

            item {
                Box(modifier = Modifier.staggeredFadeSlideIn(3)) {
                    QuickActionGrid(
                        onOpenRecords = onOpenRecords,
                        onOpenGrowth = onOpenGrowth,
                        onOpenTimeline = onOpenTimeline,
                        onOpenMedicalSummary = onOpenMedicalSummary,
                        onOpenSettings = onOpenSettings,
                    )
                }
            }

            // 4. Daily Tip
            if (encouragementText.isNotBlank()) {
                item {
                    Box(modifier = Modifier.staggeredFadeSlideIn(4)) {
                        DailyTipCard(text = encouragementText)
                    }
                }
            }

            if (caregivers.isNotEmpty()) {
                item {
                    Box(modifier = Modifier.staggeredFadeSlideIn(5)) {
                        InfoCard(
                            title = "看护人筛选",
                            tone = CardTone.Default,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
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

            activeModules.forEachIndexed { index, module ->
                val animationIndex = index + 6
                when (module) {
                    HomeModule.TODAY_SUMMARY -> {
                        item {
                            Box(modifier = Modifier.staggeredFadeSlideIn(animationIndex)) {
                                TodaySummarySection(summary = summary)
                            }
                        }
                    }

                    HomeModule.RECENT_FEEDINGS -> {
                        item {
                            Box(modifier = Modifier.staggeredFadeSlideIn(animationIndex)) {
                                SectionLabel("最近喂养")
                            }
                        }
                        if (summary.recentFeedings.isEmpty()) {
                            item {
                                Box(modifier = Modifier.staggeredFadeSlideIn(animationIndex)) {
                                    EmptyRecordCard("当前筛选下还没有喂养记录。")
                                }
                            }
                        } else {
                            itemsIndexed(summary.recentFeedings) { index, feeding ->
                                Box(modifier = Modifier.animateItem().staggeredFadeSlideIn(animationIndex + index).padding(bottom = Spacing.sm)) {
                                    HomeFeedingCard(feeding)
                                }
                            }
                        }
                    }

                    HomeModule.RECENT_SLEEP -> {
                        item {
                            Box(modifier = Modifier.staggeredFadeSlideIn(animationIndex)) {
                                SectionLabel("最近睡眠")
                            }
                        }
                        if (summary.recentSleeps.isEmpty()) {
                            item {
                                Box(modifier = Modifier.staggeredFadeSlideIn(animationIndex)) {
                                    EmptyRecordCard("当前筛选下还没有睡眠记录。")
                                }
                            }
                        } else {
                            itemsIndexed(summary.recentSleeps) { index, sleep ->
                                Box(modifier = Modifier.animateItem().staggeredFadeSlideIn(animationIndex + index).padding(bottom = Spacing.sm)) {
                                    HomeSleepCard(sleep)
                                }
                            }
                        }
                    }

                    HomeModule.LATEST_GROWTH -> {
                        item {
                            Box(modifier = Modifier.staggeredFadeSlideIn(animationIndex)) {
                                LatestGrowthCard(summary)
                            }
                        }
                    }

                    HomeModule.MILESTONE -> {
                        item {
                            Box(modifier = Modifier.staggeredFadeSlideIn(animationIndex)) {
                                LatestMilestoneCard(summary)
                            }
                        }
                    }

                    HomeModule.VACCINE -> {
                        item {
                            Box(modifier = Modifier.staggeredFadeSlideIn(animationIndex)) {
                                VaccineReminderCard(vaccines = vaccines)
                            }
                        }
                    }

                    HomeModule.TREND -> {
                        item {
                            Box(modifier = Modifier.staggeredFadeSlideIn(animationIndex)) {
                                TrendCard(trends = weeklyTrends)
                            }
                        }
                    }

                    HomeModule.ROUTINE -> {
                        item {
                            Box(modifier = Modifier.staggeredFadeSlideIn(animationIndex)) {
                                RoutineCard(routineInsights = routineInsights)
                            }
                        }
                    }

                    HomeModule.MEMORY -> {
                        memoryOfTheDay?.let { memory ->
                            item {
                                Box(modifier = Modifier.staggeredFadeSlideIn(animationIndex)) {
                                    MemoryCard(memory)
                                }
                            }
                        }
                    }

                    HomeModule.GUIDE -> {
                        monthlyGuide?.let { guide ->
                            item {
                                Box(modifier = Modifier.staggeredFadeSlideIn(animationIndex)) {
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
    }
}

/**
 * Hero profile card with golden-glow gradient, baby name, age, and weight/height stats.
 */
@Composable
private fun PostcardHeroCard(summary: HomeSummary) {
    val babyName = summary.profile?.name ?: "宝贝"
    val avatarPath = summary.profile?.avatarPath
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer

    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(),
        alpha = 0.58f,
        shape = MaterialTheme.shapes.large,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    // Golden-glow gradient: 135deg from primary to primaryContainer at 15% opacity
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.08f),
                                primaryContainerColor.copy(alpha = 0.12f),
                            ),
                            start = Offset.Zero,
                            end = Offset(size.width, size.height),
                        )
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xl, vertical = Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                // Top row: name + age on left, avatar on right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = babyName,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = summary.ageText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }

                    Spacer(modifier = Modifier.width(Spacing.lg))

                    // Avatar (96dp circle with white border)
                    if (avatarPath.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        listOf(primaryColor, primaryContainerColor)
                                    )
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = babyName.take(1),
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    } else {
                        BabyAvatar(
                            avatarPath = avatarPath,
                            contentDescription = "宝宝头像",
                            modifier = Modifier.size(96.dp),
                            containerColor = Color.White,
                            borderColor = Color.White,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 2-column stats grid for weight and height
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    // Weight stat
                    val growth = summary.latestGrowth
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(Spacing.md),
                        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.6f),
                    ) {
                        Column(
                            modifier = Modifier.padding(Spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "当前体重",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            )
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(
                                    text = growth?.weightKg?.let { String.format("%.1f", it) } ?: "--",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "kg",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp),
                                )
                            }
                        }
                    }

                    // Height stat
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(Spacing.md),
                        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.6f),
                    ) {
                        Column(
                            modifier = Modifier.padding(Spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "当前身高",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(
                                    text = growth?.heightCm?.let { String.format("%.0f", it) } ?: "--",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "cm",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Stats Bento Grid: feeding card (wider, ~58%) and sleep card (~42%) side by side.
 */
@Composable
private fun StatsBentoGrid(summary: HomeSummary) {
    val latestFeeding = summary.recentFeedings.firstOrNull()
    val latestSleep = summary.recentSleeps.firstOrNull()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        // Left: Feeding card (wider, ~58%)
        Surface(
            modifier = Modifier.weight(0.58f),
            shape = RoundedCornerShape(Spacing.md),
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Column(
                modifier = Modifier.padding(Spacing.lg2),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                Text(
                    text = "最近喂奶",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = latestFeeding?.amountMl?.let { "${it}ml" }
                            ?: latestFeeding?.durationMinutes?.let { "${it}分钟" }
                            ?: "${summary.todayFeedings}次",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                        text = latestFeeding?.let {
                            val minutes = ChronoUnit.MINUTES.between(it.happenedAt, java.time.LocalDateTime.now())
                            when {
                                minutes < 60 -> "${minutes}分前"
                                minutes < 1440 -> "${minutes / 60}小时 ${minutes % 60}分前"
                                else -> it.happenedAt.formatDateTime()
                            }
                        } ?: "暂无记录",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                    )
                }
            }
        }

        // Right: Sleep card (~42%)
        Surface(
            modifier = Modifier.weight(0.42f),
            shape = RoundedCornerShape(Spacing.md),
            color = MaterialTheme.colorScheme.tertiaryContainer,
        ) {
            Column(
                modifier = Modifier.padding(Spacing.lg2),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                Text(
                    text = "睡眠",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val sleepText = latestSleep?.let {
                        val durationMin = Duration.between(it.startTime, it.endTime).toMinutes()
                        val hours = durationMin / 60.0
                        if (hours >= 1.0) String.format("%.1fh", hours) else "${durationMin}分"
                    } ?: "${summary.todaySleepMinutes}分"
                    Text(
                        text = sleepText,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    Text(
                        text = latestSleep?.sleepType?.label ?: "暂无记录",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                    )
                }
            }
        }
    }
}

/**
 * Milestone celebrations section with header and milestone item.
 */
@Composable
private fun MilestoneSection(
    summary: HomeSummary,
    onOpenTimeline: () -> Unit,
) {
    val milestone = summary.latestMilestone
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "成长里程碑",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            TextButton(onClick = onOpenTimeline) {
                Text(
                    text = "全部",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        if (milestone != null) {
            // Milestone item
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Spacing.md),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
                ) {
                    // 64dp icon area with primaryContainer/30 background
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(Spacing.md))
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = milestone.category.label.take(1),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        )
                    }

                    // Text content
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Text(
                                text = milestone.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                            val daysAgo = ChronoUnit.DAYS.between(milestone.achievedDate, LocalDate.now()).toInt()
                            Text(
                                text = when {
                                    daysAgo == 0 -> "今天"
                                    daysAgo < 30 -> "${daysAgo}天前"
                                    daysAgo < 365 -> "${daysAgo / 30}个月前"
                                    else -> milestone.achievedDate.formatDate()
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                        milestone.note?.let { note ->
                            Text(
                                text = note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Spacing.md),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Text(
                    text = "还没有里程碑记录，快去记录宝宝的第一个里程碑吧！",
                    modifier = Modifier.padding(Spacing.lg),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Daily tip card with 4dp primary-colored left border drawn via Modifier.drawBehind.
 */
@Composable
private fun DailyTipCard(text: String) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                // 4dp left border in primary color
                drawRect(
                    color = primaryColor,
                    topLeft = Offset.Zero,
                    size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height),
                )
            },
        shape = RoundedCornerShape(Spacing.md),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier.padding(start = Spacing.lg2, end = Spacing.lg2, top = Spacing.lg2, bottom = Spacing.lg2),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Rounded.Today,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Spacing.xl),
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "今日育儿贴士",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("快捷操作", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
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
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        SectionLabel("今日摘要")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
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
    InfoCard(title = "最近体重") {
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
    InfoCard(title = "最近里程碑") {
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
    InfoCard(title = "疫苗提醒") {
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
    InfoCard(title = "本周趋势") {
        if (trends.isEmpty()) {
            Text("最近两周数据还不够，暂时看不出明显变化。", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                trends.take(3).forEach { trend ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
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
    InfoCard(title = "作息规律") {
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
    InfoCard(title = memory.title) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
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
                .padding(Spacing.lg),
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
                .padding(Spacing.lg),
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
                .padding(Spacing.lg),
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

