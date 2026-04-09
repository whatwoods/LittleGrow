package com.littlegrow.app.ui.screens

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.littlegrow.app.data.DiaperDraft
import com.littlegrow.app.data.DiaperEntity
import com.littlegrow.app.data.DiaperType
import com.littlegrow.app.data.FeedingDraft
import com.littlegrow.app.data.FeedingEntity
import com.littlegrow.app.data.FeedingType
import com.littlegrow.app.data.PoopColor
import com.littlegrow.app.data.PoopTexture
import com.littlegrow.app.data.RecordTab
import com.littlegrow.app.data.SleepDraft
import com.littlegrow.app.data.SleepEntity
import com.littlegrow.app.ui.dateTimeFormatter
import com.littlegrow.app.ui.formatDateTime
import java.time.Duration
import java.time.LocalDateTime

@Composable
fun RecordsScreen(
    selectedTab: RecordTab,
    feedings: List<FeedingEntity>,
    sleeps: List<SleepEntity>,
    diapers: List<DiaperEntity>,
    contentPadding: PaddingValues,
    onSelectTab: (RecordTab) -> Unit,
    onAddFeeding: (FeedingDraft) -> Unit,
    onUpdateFeeding: (Long, FeedingDraft) -> Unit,
    onDeleteFeeding: (Long) -> Unit,
    onAddSleep: (SleepDraft) -> Unit,
    onUpdateSleep: (Long, SleepDraft) -> Unit,
    onDeleteSleep: (Long) -> Unit,
    onAddDiaper: (DiaperDraft) -> Unit,
    onUpdateDiaper: (Long, DiaperDraft) -> Unit,
    onDeleteDiaper: (Long) -> Unit,
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var editingFeeding by remember { mutableStateOf<FeedingEntity?>(null) }
    var editingSleep by remember { mutableStateOf<SleepEntity?>(null) }
    var editingDiaper by remember { mutableStateOf<DiaperEntity?>(null) }

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
                    contentPadding = contentPadding,
                    onEdit = { feeding ->
                        editingFeeding = feeding
                        showAddDialog = true
                    },
                    onDelete = onDeleteFeeding,
                )

                RecordTab.SLEEP -> SleepTab(
                    items = sleeps,
                    contentPadding = contentPadding,
                    onEdit = { sleep ->
                        editingSleep = sleep
                        showAddDialog = true
                    },
                    onDelete = onDeleteSleep,
                )

                RecordTab.DIAPER -> DiaperTab(
                    items = diapers,
                    contentPadding = contentPadding,
                    onEdit = { diaper ->
                        editingDiaper = diaper
                        showAddDialog = true
                    },
                    onDelete = onDeleteDiaper,
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
                showAddDialog = true
            },
        ) {
            androidx.compose.material3.Icon(Icons.Rounded.Add, contentDescription = "添加记录")
        }
    }

    if (showAddDialog) {
        when (selectedTab) {
            RecordTab.FEEDING -> AddFeedingDialog(
                initial = editingFeeding,
                onDismiss = { showAddDialog = false },
                onSubmit = { draft ->
                    val editing = editingFeeding
                    if (editing == null) {
                        onAddFeeding(draft)
                    } else {
                        onUpdateFeeding(editing.id, draft)
                    }
                    editingFeeding = null
                    showAddDialog = false
                },
            )

            RecordTab.SLEEP -> AddSleepDialog(
                initial = editingSleep,
                onDismiss = { showAddDialog = false },
                onSubmit = { draft ->
                    val editing = editingSleep
                    if (editing == null) {
                        onAddSleep(draft)
                    } else {
                        onUpdateSleep(editing.id, draft)
                    }
                    editingSleep = null
                    showAddDialog = false
                },
            )

            RecordTab.DIAPER -> AddDiaperDialog(
                initial = editingDiaper,
                onDismiss = { showAddDialog = false },
                onSubmit = { draft ->
                    val editing = editingDiaper
                    if (editing == null) {
                        onAddDiaper(draft)
                    } else {
                        onUpdateDiaper(editing.id, draft)
                    }
                    editingDiaper = null
                    showAddDialog = false
                },
            )
        }
    }
}

@Composable
private fun FeedingsTab(
    items: List<FeedingEntity>,
    contentPadding: PaddingValues,
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
                summary = "支持母乳、瓶喂和辅食。母乳记录时长，瓶喂记录奶量，辅食记录食材。",
            )
        }
        if (items.isEmpty()) {
            item { EmptyRecordCard("还没有喂养记录。") }
        } else {
            items(items, key = { it.id }) { feeding ->
                ElevatedCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(onClick = { onEdit(feeding) }) {
                                Text("编辑")
                            }
                            TextButton(onClick = { onDelete(feeding.id) }) {
                                Text("删除")
                            }
                        }
                    }
                }
            }
        }
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
                            TextButton(onClick = { onEdit(sleep) }) {
                                Text("编辑")
                            }
                            TextButton(onClick = { onDelete(sleep.id) }) {
                                Text("删除")
                            }
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
                            TextButton(onClick = { onEdit(diaper) }) {
                                Text("编辑")
                            }
                            TextButton(onClick = { onDelete(diaper.id) }) {
                                Text("删除")
                            }
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
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddFeedingDialog(
    initial: FeedingEntity?,
    onDismiss: () -> Unit,
    onSubmit: (FeedingDraft) -> Unit,
) {
    var type by rememberSaveable(initial?.id) { mutableStateOf(initial?.type ?: FeedingType.BREAST_LEFT) }
    var happenedAt by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.happenedAt?.format(dateTimeFormatter) ?: LocalDateTime.now().format(dateTimeFormatter))
    }
    var durationMinutes by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.durationMinutes?.toString() ?: "15")
    }
    var amountMl by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.amountMl?.toString() ?: "90")
    }
    var foodName by rememberSaveable(initial?.id) { mutableStateOf(initial?.foodName.orEmpty()) }
    var note by rememberSaveable(initial?.id) { mutableStateOf(initial?.note.orEmpty()) }
    var errorText by rememberSaveable(initial?.id) { mutableStateOf<String?>(null) }

    FormDialog(
        title = if (initial == null) "添加喂养记录" else "编辑喂养记录",
        onDismiss = onDismiss,
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
                            onSubmit(FeedingDraft(type, happened, minutes, null, null, note))
                        }
                    }

                    FeedingType.BOTTLE_BREAST_MILK, FeedingType.BOTTLE_FORMULA -> {
                        val amount = amountMl.toIntOrNull()
                        if (amount == null || amount <= 0) {
                            errorText = "瓶喂记录需要奶量。"
                        } else {
                            onSubmit(FeedingDraft(type, happened, null, amount, null, note))
                        }
                    }

                    FeedingType.SOLID_FOOD -> {
                        if (foodName.isBlank()) {
                            errorText = "辅食记录需要食材名称。"
                        } else {
                            onSubmit(FeedingDraft(type, happened, null, null, foodName, note))
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
            }
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("备注") },
            )
            errorText?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
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
            errorText?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
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
            OptionSection(
                title = "类型",
                options = DiaperType.entries,
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
            if (type == DiaperType.POOP) {
                OptionSection(
                    title = "颜色",
                    options = PoopColor.entries,
                    selected = selectedColor,
                    label = { it.label },
                    onSelect = { selectedColor = it },
                )
                OptionSection(
                    title = "性状",
                    options = PoopTexture.entries,
                    selected = selectedTexture,
                    label = { it.label },
                    onSelect = { selectedTexture = it },
                )
            }
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("备注") },
            )
            errorText?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
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

private fun String.toLocalDateTimeOrNull(): LocalDateTime? {
    return runCatching { LocalDateTime.parse(this.trim(), dateTimeFormatter) }.getOrNull()
}
