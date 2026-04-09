package com.littlegrow.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.littlegrow.app.BreastfeedingTimerState
import com.littlegrow.app.RecordQuickAction
import com.littlegrow.app.data.ActivityDraft
import com.littlegrow.app.data.ActivityEntity
import com.littlegrow.app.data.ActivityType
import com.littlegrow.app.data.DiaperDraft
import com.littlegrow.app.data.DiaperEntity
import com.littlegrow.app.data.DiaperType
import com.littlegrow.app.data.FeedingDraft
import com.littlegrow.app.data.FeedingEntity
import com.littlegrow.app.data.FeedingType
import com.littlegrow.app.data.MedicalDraft
import com.littlegrow.app.data.MedicalEntity
import com.littlegrow.app.data.MedicalRecordType
import com.littlegrow.app.data.PoopColor
import com.littlegrow.app.data.PoopTexture
import com.littlegrow.app.data.RecordTab
import com.littlegrow.app.data.SleepDraft
import com.littlegrow.app.data.SleepEntity
import com.littlegrow.app.media.PendingPhotoCapture
import com.littlegrow.app.media.PhotoStore
import com.littlegrow.app.ui.PhotoActionRow
import com.littlegrow.app.ui.PhotoPreviewCard
import com.littlegrow.app.ui.dateTimeFormatter
import com.littlegrow.app.ui.formatDateTime
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.delay

@Composable
fun RecordsScreen(
    selectedTab: RecordTab,
    feedings: List<FeedingEntity>,
    sleeps: List<SleepEntity>,
    diapers: List<DiaperEntity>,
    medicalRecords: List<MedicalEntity>,
    activityRecords: List<ActivityEntity>,
    breastfeedingTimer: BreastfeedingTimerState,
    pendingQuickAction: RecordQuickAction?,
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
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var showTimerStarter by rememberSaveable { mutableStateOf(false) }
    var editingFeeding by remember { mutableStateOf<FeedingEntity?>(null) }
    var editingSleep by remember { mutableStateOf<SleepEntity?>(null) }
    var editingDiaper by remember { mutableStateOf<DiaperEntity?>(null) }
    var editingMedical by remember { mutableStateOf<MedicalEntity?>(null) }
    var editingActivity by remember { mutableStateOf<ActivityEntity?>(null) }

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
        Column {
            PrimaryTabRow(selectedTabIndex = selectedTab.ordinal) {
                RecordTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { onSelectTab(tab) },
                        text = { Text(tab.label) },
                    )
                }
            }

            when (selectedTab) {
                RecordTab.FEEDING -> FeedingsTab(
                    items = feedings,
                    timerState = breastfeedingTimer,
                    contentPadding = contentPadding,
                    onStartTimer = onStartBreastfeedingTimer,
                    onCancelTimer = onCancelBreastfeedingTimer,
                    onSaveTimer = onSaveBreastfeedingTimer,
                    onEdit = {
                        editingFeeding = it
                        showAddDialog = true
                    },
                    onDelete = onDeleteFeeding,
                )

                RecordTab.SLEEP -> SleepTab(
                    items = sleeps,
                    contentPadding = contentPadding,
                    onEdit = {
                        editingSleep = it
                        showAddDialog = true
                    },
                    onDelete = onDeleteSleep,
                )

                RecordTab.DIAPER -> DiaperTab(
                    items = diapers,
                    contentPadding = contentPadding,
                    onEdit = {
                        editingDiaper = it
                        showAddDialog = true
                    },
                    onDelete = onDeleteDiaper,
                )

                RecordTab.MEDICAL -> MedicalTab(
                    items = medicalRecords,
                    contentPadding = contentPadding,
                    onEdit = {
                        editingMedical = it
                        showAddDialog = true
                    },
                    onDelete = onDeleteMedical,
                )

                RecordTab.ACTIVITY -> ActivityTab(
                    items = activityRecords,
                    contentPadding = contentPadding,
                    onEdit = {
                        editingActivity = it
                        showAddDialog = true
                    },
                    onDelete = onDeleteActivity,
                )
            }
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 20.dp,
                    bottom = contentPadding.calculateBottomPadding() + 20.dp,
                ),
            onClick = {
                editingFeeding = null
                editingSleep = null
                editingDiaper = null
                editingMedical = null
                editingActivity = null
                showAddDialog = true
            },
        ) {
            androidx.compose.material3.Icon(Icons.Rounded.Add, contentDescription = "添加记录")
        }
    }

    if (showTimerStarter) {
        AlertDialog(
            onDismissRequest = { showTimerStarter = false },
            title = { Text("开始母乳计时") },
            text = { Text("选择本次先喂哪一侧。开始后可在喂养页顶部随时结束并保存。") },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            onStartBreastfeedingTimer(FeedingType.BREAST_LEFT)
                            showTimerStarter = false
                        },
                    ) {
                        Text("左侧开始")
                    }
                    TextButton(
                        onClick = {
                            onStartBreastfeedingTimer(FeedingType.BREAST_RIGHT)
                            showTimerStarter = false
                        },
                    ) {
                        Text("右侧开始")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimerStarter = false }) {
                    Text("取消")
                }
            },
        )
    }

    if (showAddDialog) {
        when (selectedTab) {
            RecordTab.FEEDING -> AddFeedingDialog(
                initial = editingFeeding,
                onDismiss = {
                    editingFeeding = null
                    showAddDialog = false
                },
                onSubmit = { draft ->
                    val editing = editingFeeding
                    if (editing == null) onAddFeeding(draft) else onUpdateFeeding(editing.id, draft)
                    editingFeeding = null
                    showAddDialog = false
                },
            )

            RecordTab.SLEEP -> AddSleepDialog(
                initial = editingSleep,
                onDismiss = {
                    editingSleep = null
                    showAddDialog = false
                },
                onSubmit = { draft ->
                    val editing = editingSleep
                    if (editing == null) onAddSleep(draft) else onUpdateSleep(editing.id, draft)
                    editingSleep = null
                    showAddDialog = false
                },
            )

            RecordTab.DIAPER -> AddDiaperDialog(
                initial = editingDiaper,
                onDismiss = {
                    editingDiaper = null
                    showAddDialog = false
                },
                onSubmit = { draft ->
                    val editing = editingDiaper
                    if (editing == null) onAddDiaper(draft) else onUpdateDiaper(editing.id, draft)
                    editingDiaper = null
                    showAddDialog = false
                },
            )

            RecordTab.MEDICAL -> AddMedicalDialog(
                initial = editingMedical,
                onDismiss = {
                    editingMedical = null
                    showAddDialog = false
                },
                onSubmit = { draft ->
                    val editing = editingMedical
                    if (editing == null) onAddMedical(draft) else onUpdateMedical(editing.id, draft)
                    editingMedical = null
                    showAddDialog = false
                },
            )

            RecordTab.ACTIVITY -> AddActivityDialog(
                initial = editingActivity,
                onDismiss = {
                    editingActivity = null
                    showAddDialog = false
                },
                onSubmit = { draft ->
                    val editing = editingActivity
                    if (editing == null) onAddActivity(draft) else onUpdateActivity(editing.id, draft)
                    editingActivity = null
                    showAddDialog = false
                },
            )
        }
    }
}

@Composable
private fun FeedingsTab(
    items: List<FeedingEntity>,
    timerState: BreastfeedingTimerState,
    contentPadding: PaddingValues,
    onStartTimer: (FeedingType) -> Unit,
    onCancelTimer: () -> Unit,
    onSaveTimer: () -> Unit,
    onEdit: (FeedingEntity) -> Unit,
    onDelete: (Long) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            HeroCard(
                title = "喂养记录",
                summary = "支持母乳、瓶喂和辅食。母乳可直接用计时器落记录，辅食可附一张照片。",
            )
        }
        item {
            BreastfeedingTimerCard(
                timerState = timerState,
                onStartTimer = onStartTimer,
                onCancelTimer = onCancelTimer,
                onSaveTimer = onSaveTimer,
            )
        }
        if (items.isEmpty()) {
            item { EmptyRecordCard("还没有喂养记录。") }
        } else {
            items(items, key = { it.id }) { feeding ->
                ElevatedCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(feeding.type.label, fontWeight = FontWeight.SemiBold)
                        Text(feeding.happenedAt.formatDateTime())
                        val detail = buildList {
                            feeding.durationMinutes?.let { add("${it} 分钟") }
                            feeding.amountMl?.let { add("${it} ml") }
                            feeding.foodName?.let { add(it) }
                            feeding.note?.let { add(it) }
                        }
                        if (detail.isNotEmpty()) {
                            Text(detail.joinToString(" · "), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        feeding.photoPath?.let {
                            PhotoPreviewCard(filePath = it, contentDescription = "辅食照片")
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(onClick = { onEdit(feeding) }) { Text("编辑") }
                            TextButton(onClick = { onDelete(feeding.id) }) { Text("删除") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BreastfeedingTimerCard(
    timerState: BreastfeedingTimerState,
    onStartTimer: (FeedingType) -> Unit,
    onCancelTimer: () -> Unit,
    onSaveTimer: () -> Unit,
) {
    var nowMs by remember(timerState.startedAtEpochMillis) {
        mutableLongStateOf(System.currentTimeMillis())
    }

    LaunchedEffect(timerState.startedAtEpochMillis) {
        if (!timerState.isRunning) return@LaunchedEffect
        while (true) {
            nowMs = System.currentTimeMillis()
            delay(1_000)
        }
    }

    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("母乳计时器", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (!timerState.isRunning) {
                Text("喂奶时直接开始计时，结束后自动生成母乳记录。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TimerActionButton("左侧开始", Modifier.weight(1f)) { onStartTimer(FeedingType.BREAST_LEFT) }
                    TimerActionButton("右侧开始", Modifier.weight(1f)) { onStartTimer(FeedingType.BREAST_RIGHT) }
                }
            } else {
                val startedAt = timerState.startedAtEpochMillis ?: 0L
                val elapsedSeconds = ((nowMs - startedAt) / 1_000L).coerceAtLeast(0L)
                val startedText = LocalDateTime.ofInstant(Instant.ofEpochMilli(startedAt), ZoneId.systemDefault()).formatDateTime()
                Text(
                    "${timerState.activeType?.label} · ${elapsedSeconds.formatStopwatch()}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text("开始于 $startedText", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TimerActionButton("结束并保存", Modifier.weight(1f), onSaveTimer)
                    TimerActionButton("取消", Modifier.weight(1f), onCancelTimer)
                }
            }
        }
    }
}

@Composable
private fun TimerActionButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    OutlinedButton(modifier = modifier, onClick = onClick) {
        Text(label)
    }
}

@Composable
private fun SleepTab(
    items: List<SleepEntity>,
    contentPadding: PaddingValues,
    onEdit: (SleepEntity) -> Unit,
    onDelete: (Long) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            HeroCard(
                title = "睡眠记录",
                summary = "用开始与结束时间记录作息，首页会自动汇总今日总睡眠时长。",
            )
        }
        if (items.isEmpty()) {
            item { EmptyRecordCard("还没有睡眠记录。") }
        } else {
            items(items, key = { it.id }) { sleep ->
                ElevatedCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("睡眠", fontWeight = FontWeight.SemiBold)
                        Text("${sleep.startTime.formatDateTime()} - ${sleep.endTime.formatDateTime()}")
                        Text(
                            "时长 ${Duration.between(sleep.startTime, sleep.endTime).toMinutes()} 分钟",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        sleep.note?.let {
                            Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(onClick = { onEdit(sleep) }) { Text("编辑") }
                            TextButton(onClick = { onDelete(sleep.id) }) { Text("删除") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiaperTab(
    items: List<DiaperEntity>,
    contentPadding: PaddingValues,
    onEdit: (DiaperEntity) -> Unit,
    onDelete: (Long) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            HeroCard(
                title = "排泄记录",
                summary = "大便可以补充颜色和性状，红色和白色会在列表中直接高亮。",
            )
        }
        if (items.isEmpty()) {
            item { EmptyRecordCard("还没有排泄记录。") }
        } else {
            items(items, key = { it.id }) { diaper ->
                ElevatedCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(diaper.type.label, fontWeight = FontWeight.SemiBold)
                        Text(diaper.happenedAt.formatDateTime())
                        val detail = buildList {
                            diaper.poopColor?.let { add(it.label) }
                            diaper.poopTexture?.let { add(it.label) }
                            diaper.note?.let { add(it) }
                        }
                        if (detail.isNotEmpty()) {
                            Text(detail.joinToString(" · "), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (diaper.poopColor == PoopColor.RED || diaper.poopColor == PoopColor.WHITE) {
                            Text(
                                "异常颜色提醒：建议结合宝宝状态尽快观察或咨询医生。",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(onClick = { onEdit(diaper) }) { Text("编辑") }
                            TextButton(onClick = { onDelete(diaper.id) }) { Text("删除") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicalTab(
    items: List<MedicalEntity>,
    contentPadding: PaddingValues,
    onEdit: (MedicalEntity) -> Unit,
    onDelete: (Long) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            HeroCard(
                title = "健康记录",
                summary = "把发热、用药和过敏史记成一条时间线；体温数据会自动画成小曲线。",
            )
        }
        item {
            TemperatureTrendCard(records = items)
        }
        if (items.isEmpty()) {
            item { EmptyRecordCard("还没有健康记录。") }
        } else {
            items(items, key = { it.id }) { medical ->
                ElevatedCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${medical.type.label} · ${medical.title}", fontWeight = FontWeight.SemiBold)
                        Text(medical.happenedAt.formatDateTime())
                        val detail = buildList {
                            medical.temperatureC?.let { add(String.format("%.1f ℃", it)) }
                            medical.dosage?.let { add("剂量 $it") }
                            medical.note?.let { add(it) }
                        }
                        if (detail.isNotEmpty()) {
                            Text(detail.joinToString(" · "), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if ((medical.temperatureC ?: 0f) >= 38.0f) {
                            Text(
                                "体温偏高，请结合精神状态和持续时间继续观察。",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(onClick = { onEdit(medical) }) { Text("编辑") }
                            TextButton(onClick = { onDelete(medical.id) }) { Text("删除") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TemperatureTrendCard(records: List<MedicalEntity>) {
    val points = remember(records) {
        records
            .filter { it.temperatureC != null }
            .sortedBy { it.happenedAt }
            .map { it.happenedAt to (it.temperatureC ?: 0f) }
    }

    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("体温曲线", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (points.size < 2) {
                Text("至少记录两次体温，才会在这里画出趋势。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                SimpleValueChart(
                    values = points.map { it.second },
                    lineColor = MaterialTheme.colorScheme.error,
                    pointColor = MaterialTheme.colorScheme.tertiary,
                )
                Text(
                    "最新 ${String.format("%.1f ℃", points.last().second)} · ${points.last().first.formatDateTime()}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            HeroCard(
                title = "活动记录",
                summary = "把洗澡、户外、趴玩和早教补进去，照护节奏会更完整。",
            )
        }
        if (items.isEmpty()) {
            item { EmptyRecordCard("还没有活动记录。") }
        } else {
            items(items, key = { it.id }) { activity ->
                ElevatedCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(activity.type.label, fontWeight = FontWeight.SemiBold)
                        Text(activity.happenedAt.formatDateTime())
                        val detail = buildList {
                            activity.durationMinutes?.let { add("${it} 分钟") }
                            activity.note?.let { add(it) }
                        }
                        if (detail.isNotEmpty()) {
                            Text(detail.joinToString(" · "), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(onClick = { onEdit(activity) }) { Text("编辑") }
                            TextButton(onClick = { onDelete(activity.id) }) { Text("删除") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCard(
    title: String,
    summary: String,
) {
    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun EmptyRecordCard(text: String) {
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.large,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FormDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { content() },
        confirmButton = { TextButton(onClick = onConfirm) { Text("保存") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddFeedingDialog(
    initial: FeedingEntity?,
    onDismiss: () -> Unit,
    onSubmit: (FeedingDraft) -> Unit,
) {
    val context = LocalContext.current
    var type by rememberSaveable(initial?.id) { mutableStateOf(initial?.type ?: FeedingType.BREAST_LEFT) }
    var happenedAt by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.happenedAt?.format(dateTimeFormatter) ?: LocalDateTime.now().format(dateTimeFormatter))
    }
    var durationMinutes by rememberSaveable(initial?.id) { mutableStateOf(initial?.durationMinutes?.toString() ?: "15") }
    var amountMl by rememberSaveable(initial?.id) { mutableStateOf(initial?.amountMl?.toString() ?: "90") }
    var foodName by rememberSaveable(initial?.id) { mutableStateOf(initial?.foodName.orEmpty()) }
    var photoPath by rememberSaveable(initial?.id) { mutableStateOf(initial?.photoPath) }
    var note by rememberSaveable(initial?.id) { mutableStateOf(initial?.note.orEmpty()) }
    var errorText by rememberSaveable(initial?.id) { mutableStateOf<String?>(null) }
    var pendingCapture by remember { mutableStateOf<PendingPhotoCapture?>(null) }

    fun replacePhoto(newPath: String?) {
        if (photoPath != initial?.photoPath) PhotoStore.deletePhoto(photoPath)
        photoPath = newPath
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            runCatching { PhotoStore.importPhoto(context, it, "feeding") }
                .onSuccess(::replacePhoto)
                .onFailure { errorText = it.message ?: "照片导入失败。" }
        }
    }
    val takePhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val capture = pendingCapture
        pendingCapture = null
        if (success && capture != null) {
            replacePhoto(capture.path)
        } else {
            PhotoStore.deletePhoto(capture?.path)
        }
    }

    fun dismissDialog() {
        if (photoPath != initial?.photoPath) PhotoStore.deletePhoto(photoPath)
        onDismiss()
    }

    FormDialog(
        title = if (initial == null) "添加喂养记录" else "编辑喂养记录",
        onDismiss = ::dismissDialog,
        onConfirm = {
            val happened = happenedAt.toLocalDateTimeOrNull()
            if (happened == null) {
                errorText = "时间格式不对，请使用 yyyy-MM-dd HH:mm。"
            } else {
                when (type) {
                    FeedingType.BREAST_LEFT, FeedingType.BREAST_RIGHT -> {
                        val minutes = durationMinutes.toIntOrNull()
                        if (minutes == null || minutes <= 0) {
                            errorText = "母乳记录需要有效时长。"
                        } else {
                            onSubmit(FeedingDraft(type, happened, minutes, null, null, null, note))
                        }
                    }

                    FeedingType.BOTTLE_BREAST_MILK, FeedingType.BOTTLE_FORMULA -> {
                        val amount = amountMl.toIntOrNull()
                        if (amount == null || amount <= 0) {
                            errorText = "瓶喂记录需要奶量。"
                        } else {
                            onSubmit(FeedingDraft(type, happened, null, amount, null, null, note))
                        }
                    }

                    FeedingType.SOLID_FOOD -> {
                        if (foodName.isBlank()) {
                            errorText = "辅食记录需要食材名称。"
                        } else {
                            onSubmit(FeedingDraft(type, happened, null, null, foodName, photoPath, note))
                        }
                    }
                }
            }
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OptionSection(
                title = "类型",
                options = FeedingType.entries,
                selected = type,
                label = { it.label },
                onSelect = { type = it },
            )
            OutlinedTextField(
                value = happenedAt,
                onValueChange = { happenedAt = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("发生时间") },
                supportingText = { Text("格式：yyyy-MM-dd HH:mm") },
                singleLine = true,
            )
            if (type == FeedingType.BREAST_LEFT || type == FeedingType.BREAST_RIGHT) {
                OutlinedTextField(
                    value = durationMinutes,
                    onValueChange = { durationMinutes = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("时长（分钟）") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
            }
            if (type == FeedingType.BOTTLE_BREAST_MILK || type == FeedingType.BOTTLE_FORMULA) {
                OutlinedTextField(
                    value = amountMl,
                    onValueChange = { amountMl = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("奶量（ml）") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
            }
            if (type == FeedingType.SOLID_FOOD) {
                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("食材") },
                    singleLine = true,
                )
                photoPath?.let {
                    PhotoPreviewCard(filePath = it, contentDescription = "辅食照片预览")
                }
                PhotoActionRow(
                    hasPhoto = photoPath != null,
                    onTakePhoto = {
                        val capture = PhotoStore.createPendingCapture(context, "feeding")
                        pendingCapture = capture
                        takePhotoLauncher.launch(capture.uri)
                    },
                    onPickPhoto = {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onRemovePhoto = { replacePhoto(null) },
                )
            }
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("备注") },
            )
            errorText?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun AddSleepDialog(
    initial: SleepEntity?,
    onDismiss: () -> Unit,
    onSubmit: (SleepDraft) -> Unit,
) {
    val now = LocalDateTime.now()
    var startTime by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.startTime?.format(dateTimeFormatter) ?: now.minusHours(1).format(dateTimeFormatter))
    }
    var endTime by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.endTime?.format(dateTimeFormatter) ?: now.format(dateTimeFormatter))
    }
    var note by rememberSaveable(initial?.id) { mutableStateOf(initial?.note.orEmpty()) }
    var errorText by rememberSaveable(initial?.id) { mutableStateOf<String?>(null) }

    FormDialog(
        title = if (initial == null) "添加睡眠记录" else "编辑睡眠记录",
        onDismiss = onDismiss,
        onConfirm = {
            val start = startTime.toLocalDateTimeOrNull()
            val end = endTime.toLocalDateTimeOrNull()
            if (start == null || end == null) {
                errorText = "时间格式不对，请使用 yyyy-MM-dd HH:mm。"
            } else if (!end.isAfter(start)) {
                errorText = "结束时间需要晚于开始时间。"
            } else {
                onSubmit(SleepDraft(start, end, note))
            }
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = startTime,
                onValueChange = { startTime = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("开始时间") },
                supportingText = { Text("格式：yyyy-MM-dd HH:mm") },
                singleLine = true,
            )
            OutlinedTextField(
                value = endTime,
                onValueChange = { endTime = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("结束时间") },
                singleLine = true,
            )
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("备注") },
            )
            errorText?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddDiaperDialog(
    initial: DiaperEntity?,
    onDismiss: () -> Unit,
    onSubmit: (DiaperDraft) -> Unit,
) {
    var type by rememberSaveable(initial?.id) { mutableStateOf(initial?.type ?: DiaperType.PEE) }
    var happenedAt by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.happenedAt?.format(dateTimeFormatter) ?: LocalDateTime.now().format(dateTimeFormatter))
    }
    var selectedColor by rememberSaveable(initial?.id) { mutableStateOf(initial?.poopColor ?: PoopColor.YELLOW) }
    var selectedTexture by rememberSaveable(initial?.id) { mutableStateOf(initial?.poopTexture ?: PoopTexture.NORMAL) }
    var note by rememberSaveable(initial?.id) { mutableStateOf(initial?.note.orEmpty()) }
    var errorText by rememberSaveable(initial?.id) { mutableStateOf<String?>(null) }

    FormDialog(
        title = if (initial == null) "添加排泄记录" else "编辑排泄记录",
        onDismiss = onDismiss,
        onConfirm = {
            val happened = happenedAt.toLocalDateTimeOrNull()
            if (happened == null) {
                errorText = "时间格式不对，请使用 yyyy-MM-dd HH:mm。"
            } else {
                onSubmit(
                    DiaperDraft(
                        happenedAt = happened,
                        type = type,
                        poopColor = if (type == DiaperType.POOP) selectedColor else null,
                        poopTexture = if (type == DiaperType.POOP) selectedTexture else null,
                        note = note,
                    ),
                )
            }
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OptionSection("类型", DiaperType.entries, type, { it.label }) { type = it }
            OutlinedTextField(
                value = happenedAt,
                onValueChange = { happenedAt = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("发生时间") },
                supportingText = { Text("格式：yyyy-MM-dd HH:mm") },
                singleLine = true,
            )
            if (type == DiaperType.POOP) {
                OptionSection("颜色", PoopColor.entries, selectedColor, { it.label }) { selectedColor = it }
                OptionSection("性状", PoopTexture.entries, selectedTexture, { it.label }) { selectedTexture = it }
            }
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("备注") },
            )
            errorText?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddMedicalDialog(
    initial: MedicalEntity?,
    onDismiss: () -> Unit,
    onSubmit: (MedicalDraft) -> Unit,
) {
    var type by rememberSaveable(initial?.id) { mutableStateOf(initial?.type ?: MedicalRecordType.ILLNESS) }
    var happenedAt by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.happenedAt?.format(dateTimeFormatter) ?: LocalDateTime.now().format(dateTimeFormatter))
    }
    var title by rememberSaveable(initial?.id) { mutableStateOf(initial?.title.orEmpty()) }
    var temperature by rememberSaveable(initial?.id) { mutableStateOf(initial?.temperatureC?.toString().orEmpty()) }
    var dosage by rememberSaveable(initial?.id) { mutableStateOf(initial?.dosage.orEmpty()) }
    var note by rememberSaveable(initial?.id) { mutableStateOf(initial?.note.orEmpty()) }
    var errorText by rememberSaveable(initial?.id) { mutableStateOf<String?>(null) }

    FormDialog(
        title = if (initial == null) "添加健康记录" else "编辑健康记录",
        onDismiss = onDismiss,
        onConfirm = {
            val happened = happenedAt.toLocalDateTimeOrNull()
            val temperatureValue = temperature.trim().takeIf { it.isNotEmpty() }?.toFloatOrNull()
            if (happened == null) {
                errorText = "时间格式不对，请使用 yyyy-MM-dd HH:mm。"
            } else if (title.isBlank()) {
                errorText = when (type) {
                    MedicalRecordType.ILLNESS -> "疾病记录需要填写症状或诊断。"
                    MedicalRecordType.MEDICATION -> "用药记录需要填写药品名。"
                    MedicalRecordType.ALLERGY -> "过敏记录需要填写过敏原。"
                }
            } else if (temperature.isNotBlank() && temperatureValue == null) {
                errorText = "体温格式不对，请输入数字。"
            } else {
                onSubmit(
                    MedicalDraft(
                        happenedAt = happened,
                        type = type,
                        title = title,
                        temperatureC = if (type == MedicalRecordType.ILLNESS) temperatureValue else null,
                        dosage = if (type == MedicalRecordType.MEDICATION) dosage else null,
                        note = note,
                    ),
                )
            }
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OptionSection("类型", MedicalRecordType.entries, type, { it.label }) { type = it }
            OutlinedTextField(
                value = happenedAt,
                onValueChange = { happenedAt = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("发生时间") },
                supportingText = { Text("格式：yyyy-MM-dd HH:mm") },
                singleLine = true,
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        when (type) {
                            MedicalRecordType.ILLNESS -> "症状 / 诊断"
                            MedicalRecordType.MEDICATION -> "药品名"
                            MedicalRecordType.ALLERGY -> "过敏原"
                        },
                    )
                },
                singleLine = true,
            )
            if (type == MedicalRecordType.ILLNESS) {
                OutlinedTextField(
                    value = temperature,
                    onValueChange = { temperature = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("体温（℃）") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )
            }
            if (type == MedicalRecordType.MEDICATION) {
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("剂量 / 用法") },
                    singleLine = true,
                )
            }
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("备注") },
            )
            errorText?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddActivityDialog(
    initial: ActivityEntity?,
    onDismiss: () -> Unit,
    onSubmit: (ActivityDraft) -> Unit,
) {
    var type by rememberSaveable(initial?.id) { mutableStateOf(initial?.type ?: ActivityType.OUTDOOR) }
    var happenedAt by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.happenedAt?.format(dateTimeFormatter) ?: LocalDateTime.now().format(dateTimeFormatter))
    }
    var durationMinutes by rememberSaveable(initial?.id) { mutableStateOf(initial?.durationMinutes?.toString().orEmpty()) }
    var note by rememberSaveable(initial?.id) { mutableStateOf(initial?.note.orEmpty()) }
    var errorText by rememberSaveable(initial?.id) { mutableStateOf<String?>(null) }

    FormDialog(
        title = if (initial == null) "添加活动记录" else "编辑活动记录",
        onDismiss = onDismiss,
        onConfirm = {
            val happened = happenedAt.toLocalDateTimeOrNull()
            val durationValue = durationMinutes.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
            if (happened == null) {
                errorText = "时间格式不对，请使用 yyyy-MM-dd HH:mm。"
            } else if (durationMinutes.isNotBlank() && durationValue == null) {
                errorText = "时长格式不对，请输入整数分钟。"
            } else {
                onSubmit(ActivityDraft(happened, type, durationValue, note))
            }
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OptionSection("类型", ActivityType.entries, type, { it.label }) { type = it }
            OutlinedTextField(
                value = happenedAt,
                onValueChange = { happenedAt = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("发生时间") },
                supportingText = { Text("格式：yyyy-MM-dd HH:mm") },
                singleLine = true,
            )
            OutlinedTextField(
                value = durationMinutes,
                onValueChange = { durationMinutes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("时长（分钟，可选）") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("备注") },
            )
            errorText?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> OptionSection(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { option ->
                androidx.compose.material3.FilterChip(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    label = { Text(label(option)) },
                )
            }
        }
    }
}

@Composable
private fun SimpleValueChart(
    values: List<Float>,
    lineColor: Color,
    pointColor: Color,
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
    ) {
        val minY = values.minOrNull() ?: 0f
        val maxY = values.maxOrNull() ?: 1f
        val spread = (maxY - minY).takeIf { it > 0f } ?: 1f
        val width = size.width
        val height = size.height
        val horizontalPadding = 24f
        val verticalPadding = 20f
        val points = values.mapIndexed { index, value ->
            val x = if (values.size == 1) {
                width / 2f
            } else {
                horizontalPadding + index * ((width - horizontalPadding * 2) / values.lastIndex.coerceAtLeast(1))
            }
            val normalized = (value - minY) / spread
            val y = height - verticalPadding - normalized * (height - verticalPadding * 2)
            Offset(x, y)
        }
        val path = Path().apply {
            points.forEachIndexed { index, point ->
                if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
            }
        }
        drawPath(
            path = path,
            color = lineColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f),
        )
        points.forEach { point ->
            drawCircle(color = pointColor, radius = 8f, center = point)
        }
    }
}

private fun String.toLocalDateTimeOrNull(): LocalDateTime? {
    return runCatching { LocalDateTime.parse(this.trim(), dateTimeFormatter) }.getOrNull()
}

private fun Long.formatStopwatch(): String {
    val hours = this / 3_600
    val minutes = (this % 3_600) / 60
    val seconds = this % 60
    return if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, seconds) else "%02d:%02d".format(minutes, seconds)
}
