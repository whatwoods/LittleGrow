package com.littlegrow.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
import com.littlegrow.app.data.SleepDraft
import com.littlegrow.app.data.SleepEntity
import com.littlegrow.app.ui.PhotoActionRow
import com.littlegrow.app.ui.PhotoPreviewCard
import com.littlegrow.app.ui.dateTimeFormatter
import com.littlegrow.app.ui.rememberManagedPhotoAttachment
import com.littlegrow.app.ui.toLocalDateTimeOrNull
import java.time.LocalDateTime

@Composable
fun QuickInputRow(onAdd: (Int) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedButton(onClick = { onAdd(10) }, modifier = Modifier.weight(1f)) {
            Text("+10")
        }
        OutlinedButton(onClick = { onAdd(30) }, modifier = Modifier.weight(1f)) {
            Text("+30")
        }
        OutlinedButton(onClick = { onAdd(50) }, modifier = Modifier.weight(1f)) {
            Text("+50")
        }
    }
}

@Composable
fun AddFeedingForm(
    initial: FeedingEntity?,
    onSubmit: (FeedingDraft) -> Unit,
    onCancel: () -> Unit,
    bindDiscard: (((() -> Unit)) -> Unit)? = null,
) {
    var type by rememberSaveable(initial?.id) { mutableStateOf(initial?.type ?: FeedingType.BREAST_LEFT) }
    var happenedAt by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.happenedAt?.format(dateTimeFormatter) ?: LocalDateTime.now().format(dateTimeFormatter))
    }
    var durationMinutes by rememberSaveable(initial?.id) { mutableStateOf(initial?.durationMinutes?.toString() ?: "15") }
    var amountMl by rememberSaveable(initial?.id) { mutableStateOf(initial?.amountMl?.toString() ?: "90") }
    var foodName by rememberSaveable(initial?.id) { mutableStateOf(initial?.foodName.orEmpty()) }
    var note by rememberSaveable(initial?.id) { mutableStateOf(initial?.note.orEmpty()) }
    var errorText by rememberSaveable(initial?.id) { mutableStateOf<String?>(null) }
    val photoAttachment = rememberManagedPhotoAttachment(
        initialPhotoPath = initial?.photoPath,
        photoTag = "feeding",
        onError = { errorText = it },
    )
    val photoPath = photoAttachment.photoPath
    val discardDraft = { photoAttachment.discardChanges() }
    val cancelForm = {
        discardDraft()
        onCancel()
    }

    SideEffect {
        bindDiscard?.invoke(discardDraft)
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LargeOptionSection(
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
            singleLine = true,
        )
        if (type == FeedingType.BREAST_LEFT || type == FeedingType.BREAST_RIGHT) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = durationMinutes,
                    onValueChange = { durationMinutes = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("时长（分钟）") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                QuickInputRow { added ->
                    val current = durationMinutes.toIntOrNull() ?: 0
                    durationMinutes = (current + added).toString()
                }
            }
        }
        if (type == FeedingType.BOTTLE_BREAST_MILK || type == FeedingType.BOTTLE_FORMULA) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amountMl,
                    onValueChange = { amountMl = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("奶量（ml）") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                QuickInputRow { added ->
                    val current = amountMl.toIntOrNull() ?: 0
                    amountMl = (current + added).toString()
                }
            }
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
                onTakePhoto = photoAttachment.onTakePhoto,
                onPickPhoto = photoAttachment.onPickPhoto,
                onRemovePhoto = photoAttachment.onRemovePhoto,
            )
        }
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("备注") },
        )
        errorText?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = cancelForm) { Text("取消") }
            Button(onClick = {
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
                                photoAttachment.commitChanges()
                                onSubmit(FeedingDraft(type, happened, minutes, null, null, null, note))
                            }
                        }
                        FeedingType.BOTTLE_BREAST_MILK, FeedingType.BOTTLE_FORMULA -> {
                            val amount = amountMl.toIntOrNull()
                            if (amount == null || amount <= 0) {
                                errorText = "瓶喂记录需要奶量。"
                            } else {
                                photoAttachment.commitChanges()
                                onSubmit(FeedingDraft(type, happened, null, amount, null, null, note))
                            }
                        }
                        FeedingType.SOLID_FOOD -> {
                            if (foodName.isBlank()) {
                                errorText = "辅食记录需要食材名称。"
                            } else {
                                photoAttachment.commitChanges()
                                onSubmit(FeedingDraft(type, happened, null, null, foodName, photoPath, note))
                            }
                        }
                    }
                }
            }) { Text("保存") }
        }
    }
}

@Composable
fun AddSleepForm(
    initial: SleepEntity?,
    onSubmit: (SleepDraft) -> Unit,
    onCancel: () -> Unit
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

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = startTime,
            onValueChange = { startTime = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("开始时间") },
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) { Text("取消") }
            Button(onClick = {
                val start = startTime.toLocalDateTimeOrNull()
                val end = endTime.toLocalDateTimeOrNull()
                if (start == null || end == null) {
                    errorText = "时间格式不对，请使用 yyyy-MM-dd HH:mm。"
                } else if (!end.isAfter(start)) {
                    errorText = "结束时间需要晚于开始时间。"
                } else {
                    onSubmit(SleepDraft(start, end, note))
                }
            }) { Text("保存") }
        }
    }
}

@Composable
fun AddDiaperForm(
    initial: DiaperEntity?,
    onSubmit: (DiaperDraft) -> Unit,
    onCancel: () -> Unit
) {
    var type by rememberSaveable(initial?.id) { mutableStateOf(initial?.type ?: DiaperType.PEE) }
    var happenedAt by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.happenedAt?.format(dateTimeFormatter) ?: LocalDateTime.now().format(dateTimeFormatter))
    }
    var selectedColor by rememberSaveable(initial?.id) { mutableStateOf(initial?.poopColor ?: PoopColor.YELLOW) }
    var selectedTexture by rememberSaveable(initial?.id) { mutableStateOf(initial?.poopTexture ?: PoopTexture.NORMAL) }
    var note by rememberSaveable(initial?.id) { mutableStateOf(initial?.note.orEmpty()) }
    var errorText by rememberSaveable(initial?.id) { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LargeOptionSection("类型", DiaperType.entries, type, { it.label }) { type = it }
        OutlinedTextField(
            value = happenedAt,
            onValueChange = { happenedAt = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("发生时间") },
            singleLine = true,
        )
        if (type == DiaperType.POOP) {
            LargeOptionSection("颜色", PoopColor.entries, selectedColor, { it.label }) { selectedColor = it }
            LargeOptionSection("性状", PoopTexture.entries, selectedTexture, { it.label }) { selectedTexture = it }
        }
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("备注") },
        )
        errorText?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) { Text("取消") }
            Button(onClick = {
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
            }) { Text("保存") }
        }
    }
}

@Composable
fun AddMedicalForm(
    initial: MedicalEntity?,
    onSubmit: (MedicalDraft) -> Unit,
    onCancel: () -> Unit
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

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LargeOptionSection("类型", MedicalRecordType.entries, type, { it.label }) { type = it }
        OutlinedTextField(
            value = happenedAt,
            onValueChange = { happenedAt = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("发生时间") },
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) { Text("取消") }
            Button(onClick = {
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
            }) { Text("保存") }
        }
    }
}

@Composable
fun AddActivityForm(
    initial: ActivityEntity?,
    onSubmit: (ActivityDraft) -> Unit,
    onCancel: () -> Unit
) {
    var type by rememberSaveable(initial?.id) { mutableStateOf(initial?.type ?: ActivityType.OUTDOOR) }
    var happenedAt by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.happenedAt?.format(dateTimeFormatter) ?: LocalDateTime.now().format(dateTimeFormatter))
    }
    var durationMinutes by rememberSaveable(initial?.id) { mutableStateOf(initial?.durationMinutes?.toString().orEmpty()) }
    var note by rememberSaveable(initial?.id) { mutableStateOf(initial?.note.orEmpty()) }
    var errorText by rememberSaveable(initial?.id) { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LargeOptionSection("类型", ActivityType.entries, type, { it.label }) { type = it }
        OutlinedTextField(
            value = happenedAt,
            onValueChange = { happenedAt = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("发生时间") },
            singleLine = true,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = durationMinutes,
                onValueChange = { durationMinutes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("时长（分钟，可选）") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
            QuickInputRow { added ->
                val current = durationMinutes.toIntOrNull() ?: 0
                durationMinutes = (current + added).toString()
            }
        }
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("备注") },
        )
        errorText?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) { Text("取消") }
            Button(onClick = {
                val happened = happenedAt.toLocalDateTimeOrNull()
                val durationValue = durationMinutes.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
                if (happened == null) {
                    errorText = "时间格式不对，请使用 yyyy-MM-dd HH:mm。"
                } else if (durationMinutes.isNotBlank() && durationValue == null) {
                    errorText = "时长格式不对，请输入整数分钟。"
                } else {
                    onSubmit(ActivityDraft(happened, type, durationValue, note))
                }
            }) { Text("保存") }
        }
    }
}
