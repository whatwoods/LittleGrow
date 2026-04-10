package com.littlegrow.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.littlegrow.app.data.ActivityDraft
import com.littlegrow.app.data.ActivityType
import com.littlegrow.app.data.DiaperDraft
import com.littlegrow.app.data.DiaperType
import com.littlegrow.app.data.FeedingDraft
import com.littlegrow.app.data.FeedingType
import com.littlegrow.app.data.FallingAsleepMethod
import com.littlegrow.app.data.MedicalDraft
import com.littlegrow.app.data.MedicalRecordType
import com.littlegrow.app.data.PoopColor
import com.littlegrow.app.data.PoopTexture
import com.littlegrow.app.data.RecordTab
import com.littlegrow.app.data.SleepDraft
import com.littlegrow.app.data.SleepType
import com.littlegrow.app.ui.NativeDateTimePickerField
import com.littlegrow.app.ui.NativeDurationPickerField
import com.littlegrow.app.ui.components.AdaptiveActionBar
import com.littlegrow.app.ui.components.AdaptiveActionBarItem
import com.littlegrow.app.ui.components.AdaptiveActionBarItemStyle
import com.littlegrow.app.ui.components.ExpressiveTextButton as TextButton
import com.littlegrow.app.ui.dateTimeFormatter
import com.littlegrow.app.ui.toLocalDateTimeOrNull
import java.time.LocalDateTime

@Composable
fun BatchRecordScreen(
    recordTab: RecordTab,
    contentPadding: PaddingValues,
    onAddFeeding: (FeedingDraft) -> Unit,
    onAddSleep: (SleepDraft) -> Unit,
    onAddDiaper: (DiaperDraft) -> Unit,
    onAddMedical: (MedicalDraft) -> Unit,
    onAddActivity: (ActivityDraft) -> Unit,
    onDone: () -> Unit,
) {
    val rows = remember(recordTab) { mutableStateListOf(BatchRecordRow.defaultFor(recordTab)) }
    var errorText by remember(recordTab) { mutableStateOf<String?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ElevatedCard {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("批量补录", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "当前按「${recordTab.label}」批量补录。适合一天结束后把遗漏记录一次性补齐。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    AdaptiveActionBar(
                        items = listOf(
                            AdaptiveActionBarItem(
                                label = "+ 添加一行",
                                onClick = { rows += BatchRecordRow.defaultFor(recordTab) },
                            ),
                            AdaptiveActionBarItem(
                                label = "保存全部",
                                onClick = onClick@{
                                    val drafts = rows.mapIndexed { index, row ->
                                        row.toDraft(recordTab) ?: run {
                                            errorText = "第 ${index + 1} 行还有未完成字段。"
                                            return@onClick
                                        }
                                    }
                                    errorText = null
                                    drafts.forEach { draft ->
                                        when (draft) {
                                            is FeedingDraft -> onAddFeeding(draft)
                                            is SleepDraft -> onAddSleep(draft)
                                            is DiaperDraft -> onAddDiaper(draft)
                                            is MedicalDraft -> onAddMedical(draft)
                                            is ActivityDraft -> onAddActivity(draft)
                                        }
                                    }
                                    onDone()
                                },
                                style = AdaptiveActionBarItemStyle.FilledTonal,
                            ),
                        ),
                    )
                    errorText?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        itemsIndexed(rows, key = { _, row -> row.key }) { index, row ->
            ElevatedCard {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("第 ${index + 1} 条", fontWeight = FontWeight.SemiBold)
                        if (rows.size > 1) {
                            TextButton(onClick = { rows.removeAt(index) }) {
                                Text("删除")
                            }
                        }
                    }
                    when (recordTab) {
                        RecordTab.FEEDING -> FeedingBatchRow(row = row) { rows[index] = it }
                        RecordTab.SLEEP -> SleepBatchRow(row = row) { rows[index] = it }
                        RecordTab.DIAPER -> DiaperBatchRow(row = row) { rows[index] = it }
                        RecordTab.MEDICAL -> MedicalBatchRow(row = row) { rows[index] = it }
                        RecordTab.ACTIVITY -> ActivityBatchRow(row = row) { rows[index] = it }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedingBatchRow(
    row: BatchRecordRow,
    onChange: (BatchRecordRow) -> Unit,
) {
    LargeOptionSection("类型", FeedingType.entries, row.feedingType, { it.label }) {
        onChange(row.copy(feedingType = it))
    }
    NativeDateTimePickerField(
        value = row.timeText,
        onValueChange = { onChange(row.copy(timeText = it)) },
        modifier = Modifier.fillMaxWidth(),
        label = "发生时间",
        supportingText = "点击选择日期和时间",
    )
    when (row.feedingType) {
        FeedingType.BREAST_LEFT, FeedingType.BREAST_RIGHT -> {
            NativeDurationPickerField(
                valueMinutes = row.durationMinutes,
                onValueChange = { onChange(row.copy(durationMinutes = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = "时长",
                supportingText = "点击选择时长",
                initialMinutesWhenEmpty = 15,
            )
        }

        FeedingType.BOTTLE_BREAST_MILK, FeedingType.BOTTLE_FORMULA -> {
            OutlinedTextField(
                value = row.amountText,
                onValueChange = { onChange(row.copy(amountText = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("奶量（ml）") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
        }

        FeedingType.SOLID_FOOD -> {
            OutlinedTextField(
                value = row.foodName,
                onValueChange = { onChange(row.copy(foodName = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("食材") },
                singleLine = true,
            )
        }
    }
    OutlinedTextField(
        value = row.note,
        onValueChange = { onChange(row.copy(note = it)) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("备注") },
    )
}

@Composable
private fun SleepBatchRow(
    row: BatchRecordRow,
    onChange: (BatchRecordRow) -> Unit,
) {
    LargeOptionSection("睡眠类型", SleepType.entries, row.sleepType, { it.label }) {
        onChange(row.copy(sleepType = it))
    }
    LargeOptionSection("入睡方式", FallingAsleepMethod.entries, row.fallingMethod, { it.label }) {
        onChange(row.copy(fallingMethod = it))
    }
    NativeDateTimePickerField(
        value = row.timeText,
        onValueChange = { onChange(row.copy(timeText = it)) },
        modifier = Modifier.fillMaxWidth(),
        label = "开始时间",
        supportingText = "点击选择日期和时间",
    )
    NativeDateTimePickerField(
        value = row.secondTimeText,
        onValueChange = { onChange(row.copy(secondTimeText = it)) },
        modifier = Modifier.fillMaxWidth(),
        label = "结束时间",
        supportingText = "点击选择日期和时间",
    )
    OutlinedTextField(
        value = row.note,
        onValueChange = { onChange(row.copy(note = it)) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("备注") },
    )
}

@Composable
private fun DiaperBatchRow(
    row: BatchRecordRow,
    onChange: (BatchRecordRow) -> Unit,
) {
    LargeOptionSection("类型", DiaperType.entries, row.diaperType, { it.label }) {
        onChange(row.copy(diaperType = it))
    }
    NativeDateTimePickerField(
        value = row.timeText,
        onValueChange = { onChange(row.copy(timeText = it)) },
        modifier = Modifier.fillMaxWidth(),
        label = "发生时间",
        supportingText = "点击选择日期和时间",
    )
    if (row.diaperType == DiaperType.POOP) {
        LargeOptionSection("颜色", PoopColor.entries, row.poopColor, { it.label }) {
            onChange(row.copy(poopColor = it))
        }
        LargeOptionSection("性状", PoopTexture.entries, row.poopTexture, { it.label }) {
            onChange(row.copy(poopTexture = it))
        }
    }
    OutlinedTextField(
        value = row.note,
        onValueChange = { onChange(row.copy(note = it)) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("备注") },
    )
}

@Composable
private fun MedicalBatchRow(
    row: BatchRecordRow,
    onChange: (BatchRecordRow) -> Unit,
) {
    LargeOptionSection("类型", MedicalRecordType.entries, row.medicalType, { it.label }) {
        onChange(row.copy(medicalType = it))
    }
    NativeDateTimePickerField(
        value = row.timeText,
        onValueChange = { onChange(row.copy(timeText = it)) },
        modifier = Modifier.fillMaxWidth(),
        label = "发生时间",
        supportingText = "点击选择日期和时间",
    )
    OutlinedTextField(
        value = row.title,
        onValueChange = { onChange(row.copy(title = it)) },
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(
                when (row.medicalType) {
                    MedicalRecordType.ILLNESS -> "症状 / 诊断"
                    MedicalRecordType.MEDICATION -> "药品名"
                    MedicalRecordType.ALLERGY -> "过敏原"
                },
            )
        },
        singleLine = true,
    )
    if (row.medicalType == MedicalRecordType.ILLNESS) {
        OutlinedTextField(
            value = row.temperatureText,
            onValueChange = { onChange(row.copy(temperatureText = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("体温（℃）") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
        )
    }
    if (row.medicalType == MedicalRecordType.MEDICATION) {
        OutlinedTextField(
            value = row.dosage,
            onValueChange = { onChange(row.copy(dosage = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("剂量 / 用法") },
            singleLine = true,
        )
    }
    OutlinedTextField(
        value = row.note,
        onValueChange = { onChange(row.copy(note = it)) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("备注") },
    )
}

@Composable
private fun ActivityBatchRow(
    row: BatchRecordRow,
    onChange: (BatchRecordRow) -> Unit,
) {
    LargeOptionSection("类型", ActivityType.entries, row.activityType, { it.label }) {
        onChange(row.copy(activityType = it))
    }
    NativeDateTimePickerField(
        value = row.timeText,
        onValueChange = { onChange(row.copy(timeText = it)) },
        modifier = Modifier.fillMaxWidth(),
        label = "发生时间",
        supportingText = "点击选择日期和时间",
    )
    NativeDurationPickerField(
        valueMinutes = row.durationMinutes,
        onValueChange = { onChange(row.copy(durationMinutes = it)) },
        modifier = Modifier.fillMaxWidth(),
        label = "时长（可选）",
        supportingText = "点击选择时长",
        allowClear = true,
        initialMinutesWhenEmpty = 15,
    )
    OutlinedTextField(
        value = row.note,
        onValueChange = { onChange(row.copy(note = it)) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("备注") },
    )
}

private data class BatchRecordRow(
    val key: Long = System.nanoTime(),
    val timeText: String = LocalDateTime.now().format(dateTimeFormatter),
    val secondTimeText: String = LocalDateTime.now().plusHours(1).format(dateTimeFormatter),
    val feedingType: FeedingType = FeedingType.BREAST_LEFT,
    val durationMinutes: Int? = 15,
    val amountText: String = "",
    val foodName: String = "",
    val diaperType: DiaperType = DiaperType.PEE,
    val poopColor: PoopColor = PoopColor.YELLOW,
    val poopTexture: PoopTexture = PoopTexture.NORMAL,
    val medicalType: MedicalRecordType = MedicalRecordType.ILLNESS,
    val title: String = "",
    val temperatureText: String = "",
    val dosage: String = "",
    val activityType: ActivityType = ActivityType.OUTDOOR,
    val sleepType: SleepType = SleepType.NAP,
    val fallingMethod: FallingAsleepMethod = FallingAsleepMethod.NURSING,
    val note: String = "",
) {
    fun toDraft(tab: RecordTab): Any? {
        return when (tab) {
            RecordTab.FEEDING -> {
                val happenedAt = timeText.toLocalDateTimeOrNull() ?: return null
                when (feedingType) {
                    FeedingType.BREAST_LEFT, FeedingType.BREAST_RIGHT -> {
                        val minutes = durationMinutes?.takeIf { it > 0 } ?: return null
                        FeedingDraft(
                            type = feedingType,
                            happenedAt = happenedAt,
                            durationMinutes = minutes,
                            amountMl = null,
                            foodName = null,
                            photoPath = null,
                            note = note,
                        )
                    }
                    FeedingType.BOTTLE_BREAST_MILK, FeedingType.BOTTLE_FORMULA -> {
                        val amount = amountText.toIntOrNull()?.takeIf { it > 0 } ?: return null
                        FeedingDraft(
                            type = feedingType,
                            happenedAt = happenedAt,
                            durationMinutes = null,
                            amountMl = amount,
                            foodName = null,
                            photoPath = null,
                            note = note,
                        )
                    }
                    FeedingType.SOLID_FOOD -> {
                        if (foodName.isBlank()) return null
                        FeedingDraft(
                            type = feedingType,
                            happenedAt = happenedAt,
                            durationMinutes = null,
                            amountMl = null,
                            foodName = foodName,
                            photoPath = null,
                            note = note,
                        )
                    }
                }
            }

            RecordTab.SLEEP -> {
                val start = timeText.toLocalDateTimeOrNull() ?: return null
                val end = secondTimeText.toLocalDateTimeOrNull() ?: return null
                if (!end.isAfter(start)) return null
                SleepDraft(
                    startTime = start,
                    endTime = end,
                    note = note,
                    sleepType = sleepType,
                    fallingAsleepMethod = fallingMethod,
                )
            }

            RecordTab.DIAPER -> {
                val happenedAt = timeText.toLocalDateTimeOrNull() ?: return null
                DiaperDraft(
                    happenedAt = happenedAt,
                    type = diaperType,
                    poopColor = if (diaperType == DiaperType.POOP) poopColor else null,
                    poopTexture = if (diaperType == DiaperType.POOP) poopTexture else null,
                    note = note,
                )
            }

            RecordTab.MEDICAL -> {
                val happenedAt = timeText.toLocalDateTimeOrNull() ?: return null
                if (title.isBlank()) return null
                val temperature = temperatureText.takeIf { it.isNotBlank() }?.toFloatOrNull()
                if (temperatureText.isNotBlank() && temperature == null) return null
                MedicalDraft(
                    happenedAt = happenedAt,
                    type = medicalType,
                    title = title,
                    temperatureC = if (medicalType == MedicalRecordType.ILLNESS) temperature else null,
                    dosage = if (medicalType == MedicalRecordType.MEDICATION) dosage else null,
                    note = note,
                )
            }

            RecordTab.ACTIVITY -> {
                val happenedAt = timeText.toLocalDateTimeOrNull() ?: return null
                ActivityDraft(
                    happenedAt = happenedAt,
                    type = activityType,
                    durationMinutes = durationMinutes,
                    note = note,
                )
            }
        }
    }

    companion object {
        fun defaultFor(tab: RecordTab): BatchRecordRow {
            return when (tab) {
                RecordTab.FEEDING -> BatchRecordRow(durationMinutes = 15)
                RecordTab.SLEEP -> BatchRecordRow(
                    timeText = LocalDateTime.now().minusHours(1).format(dateTimeFormatter),
                    secondTimeText = LocalDateTime.now().format(dateTimeFormatter),
                )
                else -> BatchRecordRow()
            }
        }
    }
}
