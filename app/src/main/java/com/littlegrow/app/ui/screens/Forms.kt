package com.littlegrow.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.littlegrow.app.data.AllergyStatus
import com.littlegrow.app.data.DiaperDraft
import com.littlegrow.app.data.DiaperEntity
import com.littlegrow.app.data.DiaperType
import com.littlegrow.app.data.FallingAsleepMethod
import com.littlegrow.app.data.FeedingFormDefaults
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
import com.littlegrow.app.data.SleepType
import com.littlegrow.app.ui.NativeDateTimePickerField
import com.littlegrow.app.ui.NativeDurationPickerField
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
private fun CaregiverSection(
    caregivers: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    if (caregivers.isEmpty()) return
    FilterChipSection(
        title = "记录人",
        entries = caregivers,
        selected = selected.ifBlank { caregivers.first() },
        label = { it },
        onSelect = onSelect,
    )
}

@Composable
fun AddFeedingForm(
    initial: FeedingEntity?,
    defaults: FeedingFormDefaults = FeedingFormDefaults(),
    caregivers: List<String> = emptyList(),
    currentCaregiver: String? = null,
    onSubmit: (FeedingDraft) -> Unit,
    onCancel: () -> Unit,
    bindDiscard: (((() -> Unit)) -> Unit)? = null,
) {
    var type by rememberSaveable(initial?.id, defaults.defaultType.name) {
        mutableStateOf(initial?.type ?: defaults.defaultType)
    }
    var happenedAt by rememberSaveable(initial?.id, defaults.defaultHappenedAt.toString()) {
        mutableStateOf(initial?.happenedAt?.format(dateTimeFormatter) ?: defaults.defaultHappenedAt.format(dateTimeFormatter))
    }
    var durationMinutes by rememberSaveable(initial?.id, defaults.defaultDurationMinutes) {
        mutableStateOf(initial?.durationMinutes ?: defaults.defaultDurationMinutes)
    }
    var amountMl by rememberSaveable(initial?.id, defaults.defaultBottleAmountMl) {
        mutableStateOf(initial?.amountMl?.toString() ?: defaults.defaultBottleAmountMl.toString())
    }
    var foodName by rememberSaveable(initial?.id) { mutableStateOf(initial?.foodName.orEmpty()) }
    var note by rememberSaveable(initial?.id) { mutableStateOf(initial?.note.orEmpty()) }
    var caregiver by rememberSaveable(initial?.id, currentCaregiver) {
        mutableStateOf(initial?.caregiver ?: currentCaregiver.orEmpty())
    }
    var errorText by rememberSaveable(initial?.id) { mutableStateOf<String?>(null) }
    var revealHiddenTypes by rememberSaveable(initial?.id, defaults.hiddenTypes.joinToString()) {
        mutableStateOf(initial?.type == FeedingType.SOLID_FOOD || defaults.hiddenTypes.isEmpty())
    }
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

    val visibleTypes = defaults.orderedTypes.filterNot { type ->
        !revealHiddenTypes && type in defaults.hiddenTypes
    }

    LaunchedEffect(visibleTypes, initial?.id) {
        if (type !in visibleTypes) {
            type = visibleTypes.firstOrNull() ?: FeedingType.BREAST_LEFT
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LargeOptionSection(
            title = "类型",
            options = visibleTypes,
            selected = type,
            label = { it.label },
            onSelect = { type = it },
        )
        CaregiverSection(
            caregivers = caregivers,
            selected = caregiver,
            onSelect = { caregiver = it },
        )
        if (initial == null && defaults.breastSideHint != null && type in listOf(FeedingType.BREAST_LEFT, FeedingType.BREAST_RIGHT)) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    text = defaults.breastSideHint,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        if (initial == null && !revealHiddenTypes && defaults.hiddenTypesHint != null) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = defaults.hiddenTypesHint,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(onClick = { revealHiddenTypes = true }) {
                        Text("仍要记录辅食")
                    }
                }
            }
        }
        if (initial != null && initial.allergyObservation != AllergyStatus.NONE) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    text = buildString {
                        append("辅食观察：${initial.allergyObservation.label}")
                        initial.observationEndDate?.let { append("，到 $it") }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
        NativeDateTimePickerField(
            value = happenedAt,
            onValueChange = { happenedAt = it },
            modifier = Modifier.fillMaxWidth(),
            label = "发生时间",
            supportingText = "点击选择日期和时间",
        )
        if (type == FeedingType.BREAST_LEFT || type == FeedingType.BREAST_RIGHT) {
            NativeDurationPickerField(
                valueMinutes = durationMinutes,
                onValueChange = { durationMinutes = it ?: 0 },
                modifier = Modifier.fillMaxWidth(),
                label = "时长",
                supportingText = "点击选择时长",
                initialMinutesWhenEmpty = 15,
            )
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
                    errorText = "请选择发生时间。"
                } else {
                    when (type) {
                        FeedingType.BREAST_LEFT, FeedingType.BREAST_RIGHT -> {
                            if (durationMinutes <= 0) {
                                errorText = "母乳记录需要有效时长。"
                            } else {
                                photoAttachment.commitChanges()
                                onSubmit(
                                    FeedingDraft(
                                        type = type,
                                        happenedAt = happened,
                                        durationMinutes = durationMinutes,
                                        amountMl = null,
                                        foodName = null,
                                        photoPath = null,
                                        note = note,
                                        allergyObservation = initial?.allergyObservation ?: AllergyStatus.NONE,
                                        observationEndDate = initial?.observationEndDate,
                                        caregiver = caregiver.takeIf { it.isNotBlank() },
                                    ),
                                )
                            }
                        }
                        FeedingType.BOTTLE_BREAST_MILK, FeedingType.BOTTLE_FORMULA -> {
                            val amount = amountMl.toIntOrNull()
                            if (amount == null || amount <= 0) {
                                errorText = "瓶喂记录需要奶量。"
                            } else {
                                photoAttachment.commitChanges()
                                onSubmit(
                                    FeedingDraft(
                                        type = type,
                                        happenedAt = happened,
                                        durationMinutes = null,
                                        amountMl = amount,
                                        foodName = null,
                                        photoPath = null,
                                        note = note,
                                        allergyObservation = initial?.allergyObservation ?: AllergyStatus.NONE,
                                        observationEndDate = initial?.observationEndDate,
                                        caregiver = caregiver.takeIf { it.isNotBlank() },
                                    ),
                                )
                            }
                        }
                        FeedingType.SOLID_FOOD -> {
                            if (foodName.isBlank()) {
                                errorText = "辅食记录需要食材名称。"
                            } else {
                                photoAttachment.commitChanges()
                                onSubmit(
                                    FeedingDraft(
                                        type = type,
                                        happenedAt = happened,
                                        durationMinutes = null,
                                        amountMl = null,
                                        foodName = foodName,
                                        photoPath = photoPath,
                                        note = note,
                                        allergyObservation = initial?.allergyObservation ?: AllergyStatus.NONE,
                                        observationEndDate = initial?.observationEndDate,
                                        caregiver = caregiver.takeIf { it.isNotBlank() },
                                    ),
                                )
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
    caregivers: List<String> = emptyList(),
    currentCaregiver: String? = null,
    onSubmit: (SleepDraft) -> Unit,
    onCancel: () -> Unit,
) {
    val now = LocalDateTime.now()
    var startTime by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.startTime?.format(dateTimeFormatter) ?: now.minusHours(1).format(dateTimeFormatter))
    }
    var endTime by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.endTime?.format(dateTimeFormatter) ?: now.format(dateTimeFormatter))
    }
    var note by rememberSaveable(initial?.id) { mutableStateOf(initial?.note.orEmpty()) }
    var sleepType by rememberSaveable(initial?.id) { mutableStateOf(initial?.sleepType ?: SleepType.NAP) }
    var fallingMethod by rememberSaveable(initial?.id) { mutableStateOf(initial?.fallingAsleepMethod ?: FallingAsleepMethod.NURSING) }
    var caregiver by rememberSaveable(initial?.id, currentCaregiver) {
        mutableStateOf(initial?.caregiver ?: currentCaregiver.orEmpty())
    }
    var errorText by rememberSaveable(initial?.id) { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CaregiverSection(
            caregivers = caregivers,
            selected = caregiver,
            onSelect = { caregiver = it },
        )
        LargeOptionSection("睡眠类型", SleepType.entries, sleepType, { it.label }) { sleepType = it }
        LargeOptionSection("入睡方式", FallingAsleepMethod.entries, fallingMethod, { it.label }) { fallingMethod = it }
        NativeDateTimePickerField(
            value = startTime,
            onValueChange = { startTime = it },
            modifier = Modifier.fillMaxWidth(),
            label = "开始时间",
            supportingText = "点击选择日期和时间",
        )
        NativeDateTimePickerField(
            value = endTime,
            onValueChange = { endTime = it },
            modifier = Modifier.fillMaxWidth(),
            label = "结束时间",
            supportingText = "点击选择日期和时间",
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
                    errorText = "请选择开始和结束时间。"
                } else if (!end.isAfter(start)) {
                    errorText = "结束时间需要晚于开始时间。"
                } else {
                    onSubmit(
                        SleepDraft(
                            startTime = start,
                            endTime = end,
                            note = note,
                            sleepType = sleepType,
                            fallingAsleepMethod = fallingMethod,
                            caregiver = caregiver.takeIf { it.isNotBlank() },
                        ),
                    )
                }
            }) { Text("保存") }
        }
    }
}

@Composable
fun AddDiaperForm(
    initial: DiaperEntity?,
    caregivers: List<String> = emptyList(),
    currentCaregiver: String? = null,
    onSubmit: (DiaperDraft) -> Unit,
    onCancel: () -> Unit,
    bindDiscard: (((() -> Unit)) -> Unit)? = null,
) {
    var type by rememberSaveable(initial?.id) { mutableStateOf(initial?.type ?: DiaperType.PEE) }
    var happenedAt by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.happenedAt?.format(dateTimeFormatter) ?: LocalDateTime.now().format(dateTimeFormatter))
    }
    var selectedColor by rememberSaveable(initial?.id) { mutableStateOf(initial?.poopColor ?: PoopColor.YELLOW) }
    var selectedTexture by rememberSaveable(initial?.id) { mutableStateOf(initial?.poopTexture ?: PoopTexture.NORMAL) }
    var note by rememberSaveable(initial?.id) { mutableStateOf(initial?.note.orEmpty()) }
    var caregiver by rememberSaveable(initial?.id, currentCaregiver) {
        mutableStateOf(initial?.caregiver ?: currentCaregiver.orEmpty())
    }
    var errorText by rememberSaveable(initial?.id) { mutableStateOf<String?>(null) }
    val photoAttachment = rememberManagedPhotoAttachment(
        initialPhotoPath = initial?.photoPath,
        photoTag = "diaper",
        onError = { errorText = it },
    )
    val photoPath = photoAttachment.photoPath
    val discardDraft = { photoAttachment.discardChanges() }

    SideEffect {
        bindDiscard?.invoke(discardDraft)
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CaregiverSection(
            caregivers = caregivers,
            selected = caregiver,
            onSelect = { caregiver = it },
        )
        LargeOptionSection("类型", DiaperType.entries, type, { it.label }) { type = it }
        NativeDateTimePickerField(
            value = happenedAt,
            onValueChange = { happenedAt = it },
            modifier = Modifier.fillMaxWidth(),
            label = "发生时间",
            supportingText = "点击选择日期和时间",
        )
        if (type == DiaperType.POOP) {
            LargeOptionSection("颜色", PoopColor.entries, selectedColor, { it.label }) { selectedColor = it }
            LargeOptionSection("性状", PoopTexture.entries, selectedTexture, { it.label }) { selectedTexture = it }
            photoPath?.let {
                PhotoPreviewCard(filePath = it, contentDescription = "便便照片预览")
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
            TextButton(onClick = {
                discardDraft()
                onCancel()
            }) { Text("取消") }
            Button(onClick = {
                val happened = happenedAt.toLocalDateTimeOrNull()
                if (happened == null) {
                    errorText = "请选择发生时间。"
                } else {
                    photoAttachment.commitChanges()
                    onSubmit(
                        DiaperDraft(
                            happenedAt = happened,
                            type = type,
                            poopColor = if (type == DiaperType.POOP) selectedColor else null,
                            poopTexture = if (type == DiaperType.POOP) selectedTexture else null,
                            note = note,
                            photoPath = if (type == DiaperType.POOP) photoPath else null,
                            caregiver = caregiver.takeIf { it.isNotBlank() },
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
    caregivers: List<String> = emptyList(),
    currentCaregiver: String? = null,
    onSubmit: (MedicalDraft) -> Unit,
    onCancel: () -> Unit,
) {
    var type by rememberSaveable(initial?.id) { mutableStateOf(initial?.type ?: MedicalRecordType.ILLNESS) }
    var happenedAt by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.happenedAt?.format(dateTimeFormatter) ?: LocalDateTime.now().format(dateTimeFormatter))
    }
    var title by rememberSaveable(initial?.id) { mutableStateOf(initial?.title.orEmpty()) }
    var temperature by rememberSaveable(initial?.id) { mutableStateOf(initial?.temperatureC?.toString().orEmpty()) }
    var dosage by rememberSaveable(initial?.id) { mutableStateOf(initial?.dosage.orEmpty()) }
    var note by rememberSaveable(initial?.id) { mutableStateOf(initial?.note.orEmpty()) }
    var caregiver by rememberSaveable(initial?.id, currentCaregiver) {
        mutableStateOf(initial?.caregiver ?: currentCaregiver.orEmpty())
    }
    var errorText by rememberSaveable(initial?.id) { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CaregiverSection(
            caregivers = caregivers,
            selected = caregiver,
            onSelect = { caregiver = it },
        )
        LargeOptionSection("类型", MedicalRecordType.entries, type, { it.label }) { type = it }
        NativeDateTimePickerField(
            value = happenedAt,
            onValueChange = { happenedAt = it },
            modifier = Modifier.fillMaxWidth(),
            label = "发生时间",
            supportingText = "点击选择日期和时间",
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
                    errorText = "请选择发生时间。"
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
                            caregiver = caregiver.takeIf { it.isNotBlank() },
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
    caregivers: List<String> = emptyList(),
    currentCaregiver: String? = null,
    onSubmit: (ActivityDraft) -> Unit,
    onCancel: () -> Unit,
) {
    var type by rememberSaveable(initial?.id) { mutableStateOf(initial?.type ?: ActivityType.OUTDOOR) }
    var happenedAt by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.happenedAt?.format(dateTimeFormatter) ?: LocalDateTime.now().format(dateTimeFormatter))
    }
    var durationMinutes by rememberSaveable(initial?.id) { mutableStateOf(initial?.durationMinutes) }
    var note by rememberSaveable(initial?.id) { mutableStateOf(initial?.note.orEmpty()) }
    var caregiver by rememberSaveable(initial?.id, currentCaregiver) {
        mutableStateOf(initial?.caregiver ?: currentCaregiver.orEmpty())
    }
    var errorText by rememberSaveable(initial?.id) { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CaregiverSection(
            caregivers = caregivers,
            selected = caregiver,
            onSelect = { caregiver = it },
        )
        LargeOptionSection("类型", ActivityType.entries, type, { it.label }) { type = it }
        NativeDateTimePickerField(
            value = happenedAt,
            onValueChange = { happenedAt = it },
            modifier = Modifier.fillMaxWidth(),
            label = "发生时间",
            supportingText = "点击选择日期和时间",
        )
        NativeDurationPickerField(
            valueMinutes = durationMinutes,
            onValueChange = { durationMinutes = it },
            modifier = Modifier.fillMaxWidth(),
            label = "时长（可选）",
            supportingText = "点击选择时长",
            allowClear = true,
            initialMinutesWhenEmpty = 15,
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
                val happened = happenedAt.toLocalDateTimeOrNull()
                if (happened == null) {
                    errorText = "请选择发生时间。"
                } else {
                    onSubmit(
                        ActivityDraft(
                            happenedAt = happened,
                            type = type,
                            durationMinutes = durationMinutes,
                            note = note,
                            caregiver = caregiver.takeIf { it.isNotBlank() },
                        ),
                    )
                }
            }) { Text("保存") }
        }
    }
}
