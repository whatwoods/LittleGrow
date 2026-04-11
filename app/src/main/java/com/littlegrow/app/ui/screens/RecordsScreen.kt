package com.littlegrow.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.LocalDining
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material.icons.rounded.Wash
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.littlegrow.app.BreastfeedingTimerState
import com.littlegrow.app.RecordQuickAction
import com.littlegrow.app.data.ActivityDraft
import com.littlegrow.app.data.ActivityEntity
import com.littlegrow.app.data.AllergyStatus
import com.littlegrow.app.data.DiaperDraft
import com.littlegrow.app.data.DiaperEntity
import com.littlegrow.app.data.FeedingDraft
import com.littlegrow.app.data.FeedingEntity
import com.littlegrow.app.data.FeedingFormDefaults
import com.littlegrow.app.data.FeedingType
import com.littlegrow.app.data.MedicalDraft
import com.littlegrow.app.data.MedicalEntity
import com.littlegrow.app.data.PoopColor
import com.littlegrow.app.data.RecordTab
import com.littlegrow.app.data.SleepDraft
import com.littlegrow.app.data.SleepEntity
import com.littlegrow.app.ui.PhotoPreviewCard
import com.littlegrow.app.ui.components.AdaptiveActionBar
import com.littlegrow.app.ui.components.AdaptiveActionBarItem
import com.littlegrow.app.ui.components.AdaptiveActionBarItemStyle
import com.littlegrow.app.ui.components.GlassSurface
import com.littlegrow.app.ui.components.ExpressiveOutlinedButton as OutlinedButton
import com.littlegrow.app.ui.components.ExpressiveTextButton as TextButton
import com.littlegrow.app.ui.formatDate
import com.littlegrow.app.ui.formatDateTime
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ──────────────────────────────────────────────────────────────────────
// RecordsScreen - stitch/_3 bento-grid design
// ──────────────────────────────────────────────────────────────────────

private val timeOnlyFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun RecordsScreen(
    selectedTab: RecordTab,
    orderedTabs: List<RecordTab>,
    feedings: List<FeedingEntity>,
    sleeps: List<SleepEntity>,
    diapers: List<DiaperEntity>,
    medicalRecords: List<MedicalEntity>,
    activityRecords: List<ActivityEntity>,
    caregivers: List<String>,
    currentCaregiver: String,
    nightWakeCount: Int,
    breastfeedingTimer: BreastfeedingTimerState,
    pendingQuickAction: RecordQuickAction?,
    feedingFormDefaults: FeedingFormDefaults,
    contentPadding: PaddingValues,
    onSelectTab: (RecordTab) -> Unit,
    onConsumeQuickAction: () -> Unit,
    onStartBreastfeedingTimer: (FeedingType) -> Unit,
    onCancelBreastfeedingTimer: () -> Unit,
    onSaveBreastfeedingTimer: () -> Unit,
    onAddFeeding: (FeedingDraft) -> Unit,
    onUpdateFeeding: (Long, FeedingDraft) -> Unit,
    onDeleteFeeding: (Long) -> Unit,
    onAddSleep: (SleepDraft) -> Unit,
    onUpdateSleep: (Long, SleepDraft) -> Unit,
    onDeleteSleep: (Long) -> Unit,
    onAddDiaper: (DiaperDraft) -> Unit,
    onUpdateDiaper: (Long, DiaperDraft) -> Unit,
    onDeleteDiaper: (Long) -> Unit,
    onAddMedical: (MedicalDraft) -> Unit,
    onUpdateMedical: (Long, MedicalDraft) -> Unit,
    onDeleteMedical: (Long) -> Unit,
    onAddActivity: (ActivityDraft) -> Unit,
    onUpdateActivity: (Long, ActivityDraft) -> Unit,
    onDeleteActivity: (Long) -> Unit,
    onOpenBatchRecord: (RecordTab) -> Unit,
    onOpenHandoverSummary: () -> Unit,
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var showTimerStarter by rememberSaveable { mutableStateOf(false) }
    var editingFeeding by remember { mutableStateOf<FeedingEntity?>(null) }
    var editingSleep by remember { mutableStateOf<SleepEntity?>(null) }
    var editingDiaper by remember { mutableStateOf<DiaperEntity?>(null) }
    var editingMedical by remember { mutableStateOf<MedicalEntity?>(null) }
    var editingActivity by remember { mutableStateOf<ActivityEntity?>(null) }

    // Whether we are showing the bento overview or a detail tab
    var showingTabDetail by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(pendingQuickAction, selectedTab) {
        when (pendingQuickAction) {
            RecordQuickAction.ADD -> {
                showAddDialog = true
                onConsumeQuickAction()
            }
            RecordQuickAction.TIMER -> {
                if (selectedTab == RecordTab.FEEDING) {
                    showTimerStarter = true
                    onConsumeQuickAction()
                }
            }
            null -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = showingTabDetail,
            transitionSpec = {
                if (targetState) {
                    (slideInHorizontally { it / 3 } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it / 3 } + fadeOut())
                } else {
                    (slideInHorizontally { -it / 3 } + fadeIn()) togetherWith
                        (slideOutHorizontally { it / 3 } + fadeOut())
                }
            },
            label = "records-view-switch",
        ) { isDetail ->
            if (!isDetail) {
                // Bento overview (hero + grid + recent activity)
                BentoOverview(
                    feedings = feedings,
                    sleeps = sleeps,
                    diapers = diapers,
                    contentPadding = contentPadding,
                    onSelectTab = { tab ->
                        onSelectTab(tab)
                        showingTabDetail = true
                    },
                    onViewAllRecent = {
                        showingTabDetail = true
                    },
                )
            } else {
                // Detail tab view with back navigation
                Column(modifier = Modifier.padding(top = contentPadding.calculateTopPadding())) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { showingTabDetail = false }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "返回",
                            )
                        }
                        Text(
                            text = selectedTab.label,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    when (selectedTab) {
                        RecordTab.FEEDING -> FeedingsTab(
                            items = feedings,
                            timerState = breastfeedingTimer,
                            feedingFormDefaults = feedingFormDefaults,
                            contentPadding = contentPadding,
                            onStartTimer = onStartBreastfeedingTimer,
                            onCancelTimer = onCancelBreastfeedingTimer,
                            onSaveTimer = onSaveBreastfeedingTimer,
                            onEdit = { editingFeeding = it; showAddDialog = true },
                            onDelete = onDeleteFeeding,
                            onOpenBatchRecord = { onOpenBatchRecord(RecordTab.FEEDING) },
                            onOpenHandoverSummary = onOpenHandoverSummary,
                        )
                        RecordTab.SLEEP -> SleepTab(
                            items = sleeps,
                            nightWakeCount = nightWakeCount,
                            contentPadding = contentPadding,
                            onEdit = { editingSleep = it; showAddDialog = true },
                            onDelete = onDeleteSleep,
                            onOpenBatchRecord = { onOpenBatchRecord(RecordTab.SLEEP) },
                            onOpenHandoverSummary = onOpenHandoverSummary,
                        )
                        RecordTab.DIAPER -> DiaperTab(
                            items = diapers,
                            contentPadding = contentPadding,
                            onEdit = { editingDiaper = it; showAddDialog = true },
                            onDelete = onDeleteDiaper,
                            onOpenBatchRecord = { onOpenBatchRecord(RecordTab.DIAPER) },
                            onOpenHandoverSummary = onOpenHandoverSummary,
                        )
                        RecordTab.MEDICAL -> MedicalTab(
                            items = medicalRecords,
                            contentPadding = contentPadding,
                            onEdit = { editingMedical = it; showAddDialog = true },
                            onDelete = onDeleteMedical,
                            onOpenBatchRecord = { onOpenBatchRecord(RecordTab.MEDICAL) },
                            onOpenHandoverSummary = onOpenHandoverSummary,
                        )
                        RecordTab.ACTIVITY -> ActivityTab(
                            items = activityRecords,
                            contentPadding = contentPadding,
                            onEdit = { editingActivity = it; showAddDialog = true },
                            onDelete = onDeleteActivity,
                            onOpenBatchRecord = { onOpenBatchRecord(RecordTab.ACTIVITY) },
                            onOpenHandoverSummary = onOpenHandoverSummary,
                        )
                    }
                }
            }
        }
    }

    // Dialogs

    if (showTimerStarter) {
        AlertDialog(
            onDismissRequest = { showTimerStarter = false },
            title = { Text("开始母乳计时") },
            text = { Text("选择本次先喂哪一侧。") },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { onStartBreastfeedingTimer(FeedingType.BREAST_LEFT); showTimerStarter = false }) { Text("左侧开始") }
                    TextButton(onClick = { onStartBreastfeedingTimer(FeedingType.BREAST_RIGHT); showTimerStarter = false }) { Text("右侧开始") }
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimerStarter = false }) { Text("取消") }
            },
        )
    }

    if (showAddDialog) {
        when (selectedTab) {
            RecordTab.FEEDING -> AddFeedingDialog(editingFeeding, caregivers, currentCaregiver, { editingFeeding = null; showAddDialog = false }, { draft ->
                val editing = editingFeeding
                if (editing == null) onAddFeeding(draft) else onUpdateFeeding(editing.id, draft)
                editingFeeding = null
                showAddDialog = false
            }, feedingFormDefaults)
            RecordTab.SLEEP -> AddSleepDialog(editingSleep, caregivers, currentCaregiver, { editingSleep = null; showAddDialog = false }, { draft ->
                val editing = editingSleep
                if (editing == null) onAddSleep(draft) else onUpdateSleep(editing.id, draft)
                editingSleep = null
                showAddDialog = false
            })
            RecordTab.DIAPER -> AddDiaperDialog(editingDiaper, caregivers, currentCaregiver, { editingDiaper = null; showAddDialog = false }, { draft ->
                val editing = editingDiaper
                if (editing == null) onAddDiaper(draft) else onUpdateDiaper(editing.id, draft)
                editingDiaper = null
                showAddDialog = false
            })
            RecordTab.MEDICAL -> AddMedicalDialog(editingMedical, caregivers, currentCaregiver, { editingMedical = null; showAddDialog = false }, { draft ->
                val editing = editingMedical
                if (editing == null) onAddMedical(draft) else onUpdateMedical(editing.id, draft)
                editingMedical = null
                showAddDialog = false
            })
            RecordTab.ACTIVITY -> AddActivityDialog(editingActivity, caregivers, currentCaregiver, { editingActivity = null; showAddDialog = false }, { draft ->
                val editing = editingActivity
                if (editing == null) onAddActivity(draft) else onUpdateActivity(editing.id, draft)
                editingActivity = null
                showAddDialog = false
            })
        }
    }
}

// ──────────────────────────────────────────────────────────────────────
// Bento Overview - hero section + 2x2 grid + recent activity
// ──────────────────────────────────────────────────────────────────────

@Composable
private fun BentoOverview(
    feedings: List<FeedingEntity>,
    sleeps: List<SleepEntity>,
    diapers: List<DiaperEntity>,
    contentPadding: PaddingValues,
    onSelectTab: (RecordTab) -> Unit,
    onViewAllRecent: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val recentItems = remember(feedings, sleeps, diapers) {
        buildRecentActivityList(feedings, sleeps, diapers)
    }

    LazyColumn(
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
            start = 20.dp,
            end = 20.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // Hero Section
        item(key = "hero") {
            HeroSection()
        }

        // 2x2 Bento Grid
        item(key = "bento-grid") {
            Column(
                modifier = Modifier.padding(bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Feeding card
                    BentoGridCard(
                        label = "喂奶",
                        icon = Icons.Rounded.LocalDining,
                        cardColor = Color.White.copy(alpha = 0.70f),
                        iconBackgroundColor = Color(0xFFFFF3E0),
                        iconTint = Color(0xFFE65100),
                        labelColor = Color(0xFFBF360C),
                        modifier = Modifier.weight(1f),
                        onClick = { onSelectTab(RecordTab.FEEDING) },
                    )
                    // Diaper card
                    BentoGridCard(
                        label = "纸尿裤",
                        icon = Icons.Rounded.Opacity,
                        cardColor = colors.secondaryContainer.copy(alpha = 0.30f),
                        iconBackgroundColor = colors.secondaryContainer,
                        iconTint = colors.secondary,
                        labelColor = colors.secondary,
                        modifier = Modifier.weight(1f),
                        onClick = { onSelectTab(RecordTab.DIAPER) },
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Sleep card
                    BentoGridCard(
                        label = "睡眠",
                        icon = Icons.Rounded.Bedtime,
                        cardColor = colors.tertiaryContainer.copy(alpha = 0.20f),
                        iconBackgroundColor = colors.tertiaryContainer.copy(alpha = 0.40f),
                        iconTint = colors.tertiary,
                        labelColor = colors.tertiary,
                        modifier = Modifier.weight(1f),
                        onClick = { onSelectTab(RecordTab.SLEEP) },
                    )
                    // Growth / Activity card
                    BentoGridCard(
                        label = "成长",
                        icon = Icons.Rounded.Straighten,
                        cardColor = colors.primaryContainer.copy(alpha = 0.20f),
                        iconBackgroundColor = colors.primaryContainer.copy(alpha = 0.50f),
                        iconTint = colors.primary,
                        labelColor = colors.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { onSelectTab(RecordTab.ACTIVITY) },
                    )
                }
            }
        }

        // Recent Activity header
        item(key = "recent-header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = "最近活动",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                )
                Text(
                    text = "查看全部",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primary,
                    modifier = Modifier.clickable { onViewAllRecent() },
                )
            }
        }

        if (recentItems.isEmpty()) {
            item(key = "recent-empty") {
                EmptyRecordCard("还没有今天的记录，快去添加第一条吧。")
            }
        } else {
            items(recentItems, key = { it.uniqueKey }) { item ->
                RecentActivityItem(
                    item = item,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────
// Hero Section
// ──────────────────────────────────────────────────────────────────────

@Composable
private fun HeroSection() {
    val colors = MaterialTheme.colorScheme
    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
        // Decorative blur circles
        Box(
            modifier = Modifier
                .size(192.dp)
                .offset(x = (-40).dp, y = (-40).dp)
                .blur(80.dp)
                .background(
                    color = colors.primaryContainer.copy(alpha = 0.10f),
                    shape = CircleShape,
                )
        )
        Box(
            modifier = Modifier
                .size(128.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-20).dp)
                .blur(60.dp)
                .background(
                    color = colors.secondaryContainer.copy(alpha = 0.20f),
                    shape = CircleShape,
                )
        )
        Column {
            Text(
                text = "今天记录",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = colors.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "宝宝的成长每一步都值得珍藏",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────
// Bento Grid Card
// ──────────────────────────────────────────────────────────────────────

@Composable
private fun BentoGridCard(
    label: String,
    icon: ImageVector,
    cardColor: Color,
    iconBackgroundColor: Color,
    iconTint: Color,
    labelColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.04f),
            ),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.20f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(color = iconBackgroundColor, shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = labelColor,
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────
// Recent Activity unified item model + list builder
// ──────────────────────────────────────────────────────────────────────

private data class RecentActivityData(
    val uniqueKey: String,
    val category: String,  // "feeding", "sleep", "diaper"
    val title: String,
    val time: LocalDateTime,
    val description: String,
    val icon: ImageVector,
    val borderColor: Color,
    val iconBackgroundColor: Color,
    val iconTint: Color,
)

private fun buildRecentActivityList(
    feedings: List<FeedingEntity>,
    sleeps: List<SleepEntity>,
    diapers: List<DiaperEntity>,
): List<RecentActivityData> {
    val feedingItems = feedings.map { f ->
        RecentActivityData(
            uniqueKey = "f_${f.id}",
            category = "feeding",
            title = f.type.label,
            time = f.happenedAt,
            description = buildList {
                f.durationMinutes?.let { add("${it}分钟") }
                f.amountMl?.let { add("${it} ml") }
                f.foodName?.let { add(it) }
            }.joinToString(", ").ifEmpty { "已记录" },
            icon = Icons.Rounded.Restaurant,
            borderColor = Color(0xFFFFA726),
            iconBackgroundColor = Color(0xFFFFF3E0),
            iconTint = Color(0xFFE65100),
        )
    }
    val sleepItems = sleeps.map { s ->
        val minutes = java.time.Duration.between(s.startTime, s.endTime).toMinutes()
        RecentActivityData(
            uniqueKey = "s_${s.id}",
            category = "sleep",
            title = s.sleepType.label,
            time = s.startTime,
            description = "时长：${formatMinutesReadable(minutes)}",
            icon = Icons.Rounded.NightsStay,
            // Placeholder colors; resolved at render time with theme
            borderColor = Color.Unspecified,
            iconBackgroundColor = Color.Unspecified,
            iconTint = Color.Unspecified,
        )
    }
    val diaperItems = diapers.map { d ->
        RecentActivityData(
            uniqueKey = "d_${d.id}",
            category = "diaper",
            title = "更换纸尿裤",
            time = d.happenedAt,
            description = buildList {
                add("类型：${d.type.label}")
                d.poopColor?.let { add(it.label) }
            }.joinToString(", "),
            icon = Icons.Rounded.Wash,
            borderColor = Color.Unspecified,
            iconBackgroundColor = Color.Unspecified,
            iconTint = Color.Unspecified,
        )
    }
    return (feedingItems + sleepItems + diaperItems)
        .sortedByDescending { it.time }
        .take(8)
}

private fun formatMinutesReadable(minutes: Long): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) "${hours}小时${mins}分钟" else "${mins}分钟"
}

// ──────────────────────────────────────────────────────────────────────
// Recent Activity Item
// ──────────────────────────────────────────────────────────────────────

@Composable
private fun RecentActivityItem(
    item: RecentActivityData,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    // Resolve themed colors for sleep/diaper items
    val borderColor = when (item.category) {
        "feeding" -> item.borderColor
        "sleep" -> colors.tertiary
        "diaper" -> colors.secondary
        else -> colors.outline
    }
    val iconBg = when (item.category) {
        "feeding" -> item.iconBackgroundColor
        "sleep" -> colors.tertiaryContainer.copy(alpha = 0.20f)
        "diaper" -> colors.secondaryContainer.copy(alpha = 0.30f)
        else -> colors.surfaceVariant
    }
    val iconTint = when (item.category) {
        "feeding" -> item.iconTint
        "sleep" -> colors.tertiary
        "diaper" -> colors.secondary
        else -> colors.onSurfaceVariant
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color.Black.copy(alpha = 0.02f),
                spotColor = Color.Black.copy(alpha = 0.02f),
            ),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceContainerLowest,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 4dp colored left border
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(72.dp)
                    .background(borderColor),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // 48dp circular icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(color = iconBg, shape = CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp),
                    )
                }
                // Text content
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Text(
                            text = item.time.format(timeOnlyFormatter),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = colors.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────
// Tab-specific content composables (preserved from original)
// ──────────────────────────────────────────────────────────────────────

@Composable
private fun FeedingsTab(
    items: List<FeedingEntity>,
    timerState: BreastfeedingTimerState,
    feedingFormDefaults: FeedingFormDefaults,
    contentPadding: PaddingValues,
    onStartTimer: (FeedingType) -> Unit,
    onCancelTimer: () -> Unit,
    onSaveTimer: () -> Unit,
    onEdit: (FeedingEntity) -> Unit,
    onDelete: (Long) -> Unit,
    onOpenBatchRecord: () -> Unit,
    onOpenHandoverSummary: () -> Unit,
) {
    RecordTabList(
        contentPadding = contentPadding,
        isEmpty = items.isEmpty(),
        emptyText = "还没有喂养记录。",
        headerContent = {
            item { HeroCard("喂养记录", "支持母乳、瓶喂和辅食。顶部会展示奶量趋势和母乳计时器。") }
            item { RecordActionRow(onOpenBatchRecord, onOpenHandoverSummary) }
            item { BottleAmountTrendCard(items) }
            item {
                BreastfeedingTimerCard(
                    timerState = timerState,
                    feedingFormDefaults = feedingFormDefaults,
                    onStartTimer = onStartTimer,
                    onCancelTimer = onCancelTimer,
                    onSaveTimer = onSaveTimer,
                )
            }
        },
        itemContent = {
            items(items, key = { it.id }) { feeding ->
                val detail = buildList {
                    feeding.durationMinutes?.let { add("${it} 分钟") }
                    feeding.amountMl?.let { add("${it} ml") }
                    feeding.foodName?.let { add(it) }
                    feeding.caregiver?.let { add("记录人 $it") }
                    feeding.note?.let { add(it) }
                }.joinToString(" · ")
                ActivityListItem(
                    icon = Icons.Rounded.Restaurant,
                    iconColor = Color(0xFFEA580C),
                    iconBgColor = Color(0xFFFFF7ED),
                    borderColor = Color(0xFFFB923C),
                    title = feeding.type.label,
                    time = feeding.happenedAt.formatDateTime(),
                    description = detail,
                    onEdit = { onEdit(feeding) },
                    onDelete = { onDelete(feeding.id) },
                ) {
                    if (feeding.allergyObservation != AllergyStatus.NONE) {
                        Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.small) {
                            Text(
                                text = buildString {
                                    append("辅食观察：${feeding.allergyObservation.label}")
                                    feeding.observationEndDate?.let { append("，到 ${it.formatDate()}") }
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                    feeding.photoPath?.let {
                        Spacer(Modifier.height(8.dp))
                        PhotoPreviewCard(filePath = it, contentDescription = "辅食照片")
                    }
                }
            }
        },
    )
}

@Composable
private fun BottleAmountTrendCard(items: List<FeedingEntity>) {
    var rangeDays by rememberSaveable { mutableStateOf(7) }
    val today = LocalDate.now()
    val values = remember(items, rangeDays) {
        (0 until rangeDays).map { offset ->
            val day = today.minusDays((rangeDays - 1 - offset).toLong())
            items.filter {
                it.happenedAt.toLocalDate() == day &&
                    it.type in listOf(FeedingType.BOTTLE_BREAST_MILK, FeedingType.BOTTLE_FORMULA)
            }.sumOf { it.amountMl ?: 0 }.toFloat()
        }
    }
    ElevatedCard {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("奶量趋势", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(7, 14, 30).forEach { days ->
                    OutlinedButton(onClick = { rangeDays = days }) { Text("最近 $days 天") }
                }
            }
            if (values.count { it > 0f } < 2) {
                Text("至少记录两天瓶喂奶量，才会在这里画出趋势。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                SimpleValueChart(values, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                Text("最近一日累计 ${values.last().toInt()} ml", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun BreastfeedingTimerCard(
    timerState: BreastfeedingTimerState,
    feedingFormDefaults: FeedingFormDefaults,
    onStartTimer: (FeedingType) -> Unit,
    onCancelTimer: () -> Unit,
    onSaveTimer: () -> Unit,
) {
    var nowMs by remember(timerState.startedAtEpochMillis) { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(timerState.startedAtEpochMillis) {
        if (!timerState.isRunning) return@LaunchedEffect
        while (true) {
            nowMs = androidx.compose.runtime.withFrameMillis { it }
        }
    }
    ElevatedCard {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("母乳计时器", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (!timerState.isRunning) {
                feedingFormDefaults.breastSideHint?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                val preferredSide = if (feedingFormDefaults.defaultType == FeedingType.BREAST_RIGHT) FeedingType.BREAST_RIGHT else FeedingType.BREAST_LEFT
                val secondarySide = if (preferredSide == FeedingType.BREAST_LEFT) FeedingType.BREAST_RIGHT else FeedingType.BREAST_LEFT
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimerActionButton("${preferredSide.label.removePrefix("母乳")}开始", Modifier.weight(1f)) { onStartTimer(preferredSide) }
                    TimerActionButton("${secondarySide.label.removePrefix("母乳")}开始", Modifier.weight(1f)) { onStartTimer(secondarySide) }
                }
            } else {
                val startedAt = timerState.startedAtEpochMillis ?: 0L
                val elapsedSeconds = ((nowMs - startedAt) / 1_000L).coerceAtLeast(0L)
                val startedText = LocalDateTime.ofInstant(Instant.ofEpochMilli(startedAt), ZoneId.systemDefault()).formatDateTime()
                Text("${timerState.activeType?.label} · ${elapsedSeconds.formatStopwatch()}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("开始于 $startedText", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimerActionButton("结束并保存", Modifier.weight(1f), onSaveTimer)
                    TimerActionButton("取消", Modifier.weight(1f), onCancelTimer)
                }
            }
        }
    }
}

@Composable
private fun SleepTab(
    items: List<SleepEntity>,
    nightWakeCount: Int,
    contentPadding: PaddingValues,
    onEdit: (SleepEntity) -> Unit,
    onDelete: (Long) -> Unit,
    onOpenBatchRecord: () -> Unit,
    onOpenHandoverSummary: () -> Unit,
) {
    RecordTabList(
        contentPadding = contentPadding,
        isEmpty = items.isEmpty(),
        emptyText = "还没有睡眠记录。",
        headerContent = {
            item { HeroCard("睡眠记录", "自动区分小睡和夜间睡眠，并统计昨晚夜醒次数。") }
            item { RecordActionRow(onOpenBatchRecord, onOpenHandoverSummary) }
            item {
                ElevatedCard {
                    Text(
                        text = "昨晚夜醒 $nightWakeCount 次",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        },
        itemContent = {
            items(items, key = { it.id }) { sleep ->
                val detail = listOfNotNull(
                    "时长 ${java.time.Duration.between(sleep.startTime, sleep.endTime).toMinutes()} 分钟",
                    sleep.caregiver?.let { "记录人 $it" },
                    sleep.note,
                ).joinToString(" · ")
                ActivityListItem(
                    icon = Icons.Rounded.Bedtime,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    iconBgColor = MaterialTheme.colorScheme.tertiaryContainer,
                    borderColor = MaterialTheme.colorScheme.tertiary,
                    title = "${sleep.sleepType.label} · ${sleep.fallingAsleepMethod?.label ?: "未记录入睡方式"}",
                    time = "${sleep.startTime.formatDateTime()} - ${sleep.endTime.formatDateTime()}",
                    description = detail,
                    onEdit = { onEdit(sleep) },
                    onDelete = { onDelete(sleep.id) },
                )
            }
        },
    )
}

@Composable
private fun DiaperTab(
    items: List<DiaperEntity>,
    contentPadding: PaddingValues,
    onEdit: (DiaperEntity) -> Unit,
    onDelete: (Long) -> Unit,
    onOpenBatchRecord: () -> Unit,
    onOpenHandoverSummary: () -> Unit,
) {
    RecordTabList(
        contentPadding = contentPadding,
        isEmpty = items.isEmpty(),
        emptyText = "还没有排泄记录。",
        headerContent = {
            item { HeroCard("排泄记录", "大便记录可附照片，红色和白色会直接高亮。") }
            item { RecordActionRow(onOpenBatchRecord, onOpenHandoverSummary) }
        },
        itemContent = {
            items(items, key = { it.id }) { diaper ->
                val detail = buildList {
                    diaper.poopColor?.let { add(it.label) }
                    diaper.poopTexture?.let { add(it.label) }
                    diaper.caregiver?.let { add("记录人 $it") }
                    diaper.note?.let { add(it) }
                }.joinToString(" · ")
                ActivityListItem(
                    icon = Icons.Rounded.WaterDrop,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    iconBgColor = MaterialTheme.colorScheme.secondaryContainer,
                    borderColor = MaterialTheme.colorScheme.secondary,
                    title = diaper.type.label,
                    time = diaper.happenedAt.formatDateTime(),
                    description = detail,
                    onEdit = { onEdit(diaper) },
                    onDelete = { onDelete(diaper.id) },
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        diaper.photoPath?.let { PhotoPreviewCard(filePath = it, contentDescription = "大便照片") }
                        if (diaper.poopColor == PoopColor.RED || diaper.poopColor == PoopColor.WHITE) {
                            Text("异常颜色提醒：建议结合宝宝状态尽快观察或咨询医生。", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun MedicalTab(
    items: List<MedicalEntity>,
    contentPadding: PaddingValues,
    onEdit: (MedicalEntity) -> Unit,
    onDelete: (Long) -> Unit,
    onOpenBatchRecord: () -> Unit,
    onOpenHandoverSummary: () -> Unit,
) {
    RecordTabList(
        contentPadding = contentPadding,
        isEmpty = items.isEmpty(),
        emptyText = "还没有健康记录。",
        headerContent = {
            item { HeroCard("健康记录", "把发热、用药和过敏史记成时间线；体温数据会自动画成小曲线。") }
            item { RecordActionRow(onOpenBatchRecord, onOpenHandoverSummary) }
            item { TemperatureTrendCard(records = items) }
        },
        itemContent = {
            items(items, key = { it.id }) { medical ->
                val detail = buildList {
                    medical.temperatureC?.let { add(String.format("%.1f ℃", it)) }
                    medical.dosage?.let { add("剂量 $it") }
                    medical.caregiver?.let { add("记录人 $it") }
                    medical.note?.let { add(it) }
                }.joinToString(" · ")
                ActivityListItem(
                    icon = Icons.Rounded.MedicalServices,
                    iconColor = MaterialTheme.colorScheme.error,
                    iconBgColor = MaterialTheme.colorScheme.errorContainer,
                    borderColor = MaterialTheme.colorScheme.error,
                    title = "${medical.type.label} · ${medical.title}",
                    time = medical.happenedAt.formatDateTime(),
                    description = detail,
                    onEdit = { onEdit(medical) },
                    onDelete = { onDelete(medical.id) },
                ) {
                    if ((medical.temperatureC ?: 0f) >= 38.0f) {
                        Text("体温偏高，请结合精神状态和持续时间继续观察。", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
    )
}

@Composable
private fun TemperatureTrendCard(records: List<MedicalEntity>) {
    val points = remember(records) { records.filter { it.temperatureC != null }.sortedBy { it.happenedAt }.map { it.happenedAt to (it.temperatureC ?: 0f) } }
    ElevatedCard {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("体温曲线", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (points.size < 2) {
                Text("至少记录两次体温，才会在这里画出趋势。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                SimpleValueChart(points.map { it.second }, MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.tertiary)
                Text("最新 ${String.format("%.1f ℃", points.last().second)} · ${points.last().first.formatDateTime()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ActivityTab(
    items: List<ActivityEntity>,
    contentPadding: PaddingValues,
    onEdit: (ActivityEntity) -> Unit,
    onDelete: (Long) -> Unit,
    onOpenBatchRecord: () -> Unit,
    onOpenHandoverSummary: () -> Unit,
) {
    RecordTabList(
        contentPadding = contentPadding,
        isEmpty = items.isEmpty(),
        emptyText = "还没有活动记录。",
        headerContent = {
            item { HeroCard("活动记录", "把洗澡、户外、趴玩和早教补进去，照护节奏会更完整。") }
            item { RecordActionRow(onOpenBatchRecord, onOpenHandoverSummary) }
        },
        itemContent = {
            items(items, key = { it.id }) { activity ->
                val detail = buildList {
                    activity.durationMinutes?.let { add("${it} 分钟") }
                    activity.caregiver?.let { add("记录人 $it") }
                    activity.note?.let { add(it) }
                }.joinToString(" · ")
                ActivityListItem(
                    icon = Icons.Rounded.Straighten,
                    iconColor = MaterialTheme.colorScheme.primary,
                    iconBgColor = MaterialTheme.colorScheme.primaryContainer,
                    borderColor = MaterialTheme.colorScheme.primary,
                    title = activity.type.label,
                    time = activity.happenedAt.formatDateTime(),
                    description = detail,
                    onEdit = { onEdit(activity) },
                    onDelete = { onDelete(activity.id) },
                )
            }
        },
    )
}

// ──────────────────────────────────────────────────────────────────────
// Activity List Item (used by tab detail views)
// ──────────────────────────────────────────────────────────────────────

@Composable
private fun ActivityListItem(
    icon: ImageVector,
    iconColor: Color,
    iconBgColor: Color,
    borderColor: Color,
    title: String,
    time: String,
    description: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    content: (@Composable () -> Unit)? = null,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(8.dp),
                spotColor = Color.Black.copy(alpha = 0.02f),
            ),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(
                        color = borderColor,
                        topLeft = Offset(0f, 0f),
                        size = Size(4.dp.toPx(), size.height),
                    )
                }
                .clickable { onEdit() }
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBgColor, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(time, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (description.isNotEmpty()) {
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                if (content != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    content()
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onEdit) { Text("编辑") }
                    TextButton(onClick = onDelete) { Text("删除") }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────
// Shared helpers
// ──────────────────────────────────────────────────────────────────────

private fun recordTabContentPadding(contentPadding: PaddingValues): PaddingValues {
    return PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = contentPadding.calculateBottomPadding() + 96.dp)
}

@Composable
private fun RecordTabList(
    contentPadding: PaddingValues,
    isEmpty: Boolean,
    emptyText: String,
    headerContent: LazyListScope.() -> Unit,
    itemContent: LazyListScope.() -> Unit,
) {
    LazyColumn(contentPadding = recordTabContentPadding(contentPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        headerContent()
        if (isEmpty) item { EmptyRecordCard(emptyText) } else itemContent()
    }
}

@Composable
private fun HeroCard(title: String, summary: String) {
    ElevatedCard {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RecordActionRow(onOpenBatchRecord: () -> Unit, onOpenHandoverSummary: () -> Unit) {
    AdaptiveActionBar(
        items = listOf(
            AdaptiveActionBarItem(
                label = "批量补录",
                onClick = onOpenBatchRecord,
            ),
            AdaptiveActionBarItem(
                label = "交接摘要",
                onClick = onOpenHandoverSummary,
                style = AdaptiveActionBarItemStyle.FilledTonal,
            ),
        ),
    )
}

@Composable
fun EmptyRecordCard(text: String, modifier: Modifier = Modifier) {
    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        alpha = 0.58f,
        shape = MaterialTheme.shapes.large,
        accentColor = MaterialTheme.colorScheme.secondary,
        shadowElevation = 10.dp,
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Today,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────
// Dialogs (unchanged from original)
// ──────────────────────────────────────────────────────────────────────

@Composable
private fun FormDialog(title: String, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { content() },
        confirmButton = {},
        dismissButton = {},
    )
}

@Composable
private fun AddFeedingDialog(
    initial: FeedingEntity?,
    caregivers: List<String>,
    currentCaregiver: String,
    onDismiss: () -> Unit,
    onSubmit: (FeedingDraft) -> Unit,
    defaults: FeedingFormDefaults,
) {
    var dismissForm by remember { mutableStateOf<() -> Unit>({ onDismiss() }) }
    FormDialog(
        title = if (initial == null) "添加喂养记录" else "编辑喂养记录",
        onDismiss = { dismissForm() },
    ) {
        AddFeedingForm(
            initial = initial,
            defaults = defaults,
            caregivers = caregivers,
            currentCaregiver = currentCaregiver,
            onSubmit = onSubmit,
            onCancel = onDismiss,
            bindDiscard = { discardDraft -> dismissForm = { discardDraft(); onDismiss() } },
        )
    }
}

@Composable
private fun AddSleepDialog(
    initial: SleepEntity?,
    caregivers: List<String>,
    currentCaregiver: String,
    onDismiss: () -> Unit,
    onSubmit: (SleepDraft) -> Unit,
) {
    FormDialog(
        title = if (initial == null) "添加睡眠记录" else "编辑睡眠记录",
        onDismiss = onDismiss,
    ) {
        AddSleepForm(
            initial = initial,
            caregivers = caregivers,
            currentCaregiver = currentCaregiver,
            onSubmit = onSubmit,
            onCancel = onDismiss,
        )
    }
}

@Composable
private fun AddDiaperDialog(
    initial: DiaperEntity?,
    caregivers: List<String>,
    currentCaregiver: String,
    onDismiss: () -> Unit,
    onSubmit: (DiaperDraft) -> Unit,
) {
    var dismissForm by remember { mutableStateOf<() -> Unit>({ onDismiss() }) }
    FormDialog(
        title = if (initial == null) "添加排泄记录" else "编辑排泄记录",
        onDismiss = { dismissForm() },
    ) {
        AddDiaperForm(
            initial = initial,
            caregivers = caregivers,
            currentCaregiver = currentCaregiver,
            onSubmit = onSubmit,
            onCancel = onDismiss,
            bindDiscard = { discardDraft -> dismissForm = { discardDraft(); onDismiss() } },
        )
    }
}

@Composable
private fun AddMedicalDialog(
    initial: MedicalEntity?,
    caregivers: List<String>,
    currentCaregiver: String,
    onDismiss: () -> Unit,
    onSubmit: (MedicalDraft) -> Unit,
) {
    FormDialog(
        title = if (initial == null) "添加健康记录" else "编辑健康记录",
        onDismiss = onDismiss,
    ) {
        AddMedicalForm(
            initial = initial,
            caregivers = caregivers,
            currentCaregiver = currentCaregiver,
            onSubmit = onSubmit,
            onCancel = onDismiss,
        )
    }
}

@Composable
private fun AddActivityDialog(
    initial: ActivityEntity?,
    caregivers: List<String>,
    currentCaregiver: String,
    onDismiss: () -> Unit,
    onSubmit: (ActivityDraft) -> Unit,
) {
    FormDialog(
        title = if (initial == null) "添加活动记录" else "编辑活动记录",
        onDismiss = onDismiss,
    ) {
        AddActivityForm(
            initial = initial,
            caregivers = caregivers,
            currentCaregiver = currentCaregiver,
            onSubmit = onSubmit,
            onCancel = onDismiss,
        )
    }
}

@Composable
private fun TimerActionButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(modifier = modifier, onClick = onClick) {
        Text(label)
    }
}

@Composable
private fun SimpleValueChart(values: List<Float>, lineColor: Color, pointColor: Color) {
    Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
        val minY = values.minOrNull() ?: 0f
        val maxY = values.maxOrNull() ?: 1f
        val spread = (maxY - minY).takeIf { it > 0f } ?: 1f
        val width = size.width
        val height = size.height
        val horizontalPadding = 24f
        val verticalPadding = 20f
        val points = values.mapIndexed { index, value ->
            val x = if (values.size == 1) width / 2f else horizontalPadding + index * ((width - horizontalPadding * 2) / values.lastIndex.coerceAtLeast(1))
            val normalized = (value - minY) / spread
            val y = height - verticalPadding - normalized * (height - verticalPadding * 2)
            Offset(x, y)
        }
        val path = Path().apply {
            points.forEachIndexed { index, point ->
                if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
            }
        }
        drawPath(path = path, color = lineColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f))
        points.forEach { point ->
            drawCircle(color = pointColor, radius = 8f, center = point)
        }
    }
}

private fun Long.formatStopwatch(): String {
    val hours = this / 3_600
    val minutes = (this % 3_600) / 60
    val seconds = this % 60
    return if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, seconds) else "%02d:%02d".format(minutes, seconds)
}
