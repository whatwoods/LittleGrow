package com.littlegrow.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.littlegrow.app.BuildConfig
import com.littlegrow.app.data.AppTheme
import com.littlegrow.app.data.BackupFrequency
import com.littlegrow.app.data.BabyProfile
import com.littlegrow.app.data.Gender
import com.littlegrow.app.data.HomeModule
import com.littlegrow.app.data.ThemeMode
import com.littlegrow.app.ui.BabyAvatar
import com.littlegrow.app.ui.PhotoActionRow
import com.littlegrow.app.ui.dateFormatter
import com.littlegrow.app.ui.rememberManagedPhotoAttachment
import com.littlegrow.app.ui.theme.ThemePreviewColors
import com.littlegrow.app.ui.theme.previewColors
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    profile: BabyProfile?,
    themeMode: ThemeMode,
    appTheme: AppTheme,
    vaccineRemindersEnabled: Boolean,
    quickActionNotificationsEnabled: Boolean,
    anomalyRemindersEnabled: Boolean,
    diaperRemindersEnabled: Boolean,
    largeTextModeEnabled: Boolean,
    darkModeScheduleEnabled: Boolean,
    darkModeStartHour: Int,
    darkModeEndHour: Int,
    homeModules: Set<HomeModule>,
    caregivers: List<String>,
    currentCaregiver: String,
    autoBackupFrequency: BackupFrequency,
    exportMessage: String?,
    isExporting: Boolean,
    contentPadding: PaddingValues,
    onSaveProfile: (BabyProfile) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onAppThemeChange: (AppTheme) -> Unit,
    onVaccineRemindersChange: (Boolean) -> Unit,
    onQuickActionNotificationsChange: (Boolean) -> Unit,
    onAnomalyRemindersChange: (Boolean) -> Unit,
    onDiaperRemindersChange: (Boolean) -> Unit,
    onLargeTextModeChange: (Boolean) -> Unit,
    onDarkModeScheduleChange: (Boolean, Int, Int) -> Unit,
    onHomeModulesChange: (Set<HomeModule>) -> Unit,
    onCaregiversChange: (String) -> Unit,
    onCurrentCaregiverChange: (String) -> Unit,
    onAutoBackupFrequencyChange: (BackupFrequency) -> Unit,
    onExportCsv: (Uri) -> Unit,
    onExportPdf: (Uri) -> Unit,
    onExportBackup: (Uri) -> Unit,
    onRestoreBackup: (Uri) -> Unit,
    onImportCsv: (Uri) -> Unit,
    onOpenMedicalSummary: () -> Unit,
    onClearExportMessage: () -> Unit,
) {
    val context = LocalContext.current
    val notificationPermissionGranted =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    val vaccinePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) onVaccineRemindersChange(true)
    }
    val quickPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) onQuickActionNotificationsChange(true)
    }
    val anomalyPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) onAnomalyRemindersChange(true)
    }
    val diaperPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) onDiaperRemindersChange(true)
    }

    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri -> uri?.let(onExportCsv) }
    val pdfExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
    ) { uri -> uri?.let(onExportPdf) }
    val backupExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri -> uri?.let(onExportBackup) }
    val backupRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let(onRestoreBackup) }
    val csvImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let(onImportCsv) }

    var name by rememberSaveable(profile?.name) { mutableStateOf(profile?.name.orEmpty()) }
    var birthday by rememberSaveable(profile?.birthday) {
        mutableStateOf(profile?.birthday?.format(dateFormatter) ?: LocalDate.now().format(dateFormatter))
    }
    var showDatePicker by remember { mutableStateOf(false) }
    
    var gender by rememberSaveable(profile?.gender) { mutableStateOf(profile?.gender ?: Gender.GIRL) }
    var caregiverText by rememberSaveable(caregivers) { mutableStateOf(caregivers.joinToString("、")) }
    var scheduleStartText by rememberSaveable(darkModeStartHour) { mutableStateOf(darkModeStartHour.toString()) }
    var scheduleEndText by rememberSaveable(darkModeEndHour) { mutableStateOf(darkModeEndHour.toString()) }
    var errorText by rememberSaveable { mutableStateOf<String?>(null) }
    var retainedAvatarPath by rememberSaveable(profile?.avatarPath) { mutableStateOf(profile?.avatarPath) }
    val avatarAttachment = rememberManagedPhotoAttachment(
        initialPhotoPath = profile?.avatarPath,
        photoTag = "avatar",
        onError = { errorText = it },
    )
    val latestAvatarPath by rememberUpdatedState(avatarAttachment.photoPath)
    val latestRetainedAvatarPath by rememberUpdatedState(retainedAvatarPath)
    val latestDiscardAvatarChanges by rememberUpdatedState(avatarAttachment.discardChanges)

    DisposableEffect(profile?.avatarPath) {
        onDispose {
            if (latestAvatarPath != latestRetainedAvatarPath) {
                latestDiscardAvatarChanges()
            }
        }
    }
    
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = runCatching { 
                LocalDate.parse(birthday, dateFormatter).atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
            }.getOrNull()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = java.time.Instant.ofEpochMilli(millis)
                        val localDate = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.of("UTC")).toLocalDate()
                        birthday = localDate.format(dateFormatter)
                    }
                    showDatePicker = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
    ) {
        item {
            Text(
                "设置",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            SettingsSectionTitle("提醒与通知")
            NotificationListItem(
                title = "疫苗接种提醒",
                checked = vaccineRemindersEnabled,
                onCheckedChange = { enabled ->
                    handleNotificationSwitch(
                        enabled = enabled,
                        granted = notificationPermissionGranted,
                        launcher = vaccinePermissionLauncher::launch,
                        onEnable = { onVaccineRemindersChange(true) },
                        onDisable = { onVaccineRemindersChange(false) },
                    )
                },
            )
            NotificationListItem(
                title = "智能异常提醒",
                checked = anomalyRemindersEnabled,
                onCheckedChange = { enabled ->
                    handleNotificationSwitch(
                        enabled = enabled,
                        granted = notificationPermissionGranted,
                        launcher = anomalyPermissionLauncher::launch,
                        onEnable = { onAnomalyRemindersChange(true) },
                        onDisable = { onAnomalyRemindersChange(false) },
                    )
                },
            )
            NotificationListItem(
                title = "大便频次提醒",
                checked = diaperRemindersEnabled,
                onCheckedChange = { enabled ->
                    handleNotificationSwitch(
                        enabled = enabled,
                        granted = notificationPermissionGranted,
                        launcher = diaperPermissionLauncher::launch,
                        onEnable = { onDiaperRemindersChange(true) },
                        onDisable = { onDiaperRemindersChange(false) },
                    )
                },
            )
            NotificationListItem(
                title = "通知栏快捷记录",
                checked = quickActionNotificationsEnabled,
                onCheckedChange = { enabled ->
                    handleNotificationSwitch(
                        enabled = enabled,
                        granted = notificationPermissionGranted,
                        launcher = quickPermissionLauncher::launch,
                        onEnable = { onQuickActionNotificationsChange(true) },
                        onDisable = { onQuickActionNotificationsChange(false) },
                    )
                },
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
        }

        item {
            SettingsSectionTitle("宝宝资料")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    BabyAvatar(
                        avatarPath = avatarAttachment.photoPath,
                        contentDescription = "宝宝头像",
                        modifier = Modifier.size(72.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                    )
                    PhotoActionRow(
                        title = "头像",
                        removeLabel = "移除",
                        hasPhoto = avatarAttachment.photoPath != null,
                        onTakePhoto = avatarAttachment.onTakePhoto,
                        onPickPhoto = avatarAttachment.onPickPhoto,
                        onRemovePhoto = avatarAttachment.onRemovePhoto,
                    )
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("昵称") },
                    singleLine = true,
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = birthday,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("生日") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Rounded.CalendarMonth, contentDescription = "选择日期")
                            }
                        }
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
                }
                FilterChipSection(
                    title = "性别",
                    entries = Gender.entries,
                    selected = gender,
                    label = { it.label },
                    onSelect = { gender = it },
                )
                ElevatedButton(
                    onClick = {
                        val parsedBirthday = runCatching { LocalDate.parse(birthday.trim(), dateFormatter) }.getOrNull()
                        if (name.isBlank()) {
                            errorText = "昵称不能为空。"
                            return@ElevatedButton
                        }
                        if (parsedBirthday == null) {
                            errorText = "请选择生日。"
                            return@ElevatedButton
                        }
                        errorText = null
                        avatarAttachment.commitChanges()
                        retainedAvatarPath = avatarAttachment.photoPath
                        onSaveProfile(
                            BabyProfile(
                                name = name.trim(),
                                birthday = parsedBirthday,
                                gender = gender,
                                avatarPath = avatarAttachment.photoPath,
                            ),
                        )
                    },
                ) {
                    Text("保存资料")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
        }

        item {
            SettingsSectionTitle("主题与外观")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ThemeMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = themeMode == mode,
                            onClick = { onThemeModeChange(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = ThemeMode.entries.size),
                            icon = {
                                SegmentedButtonDefaults.Icon(active = themeMode == mode) {
                                    Icon(
                                        imageVector = when(mode) {
                                            ThemeMode.SYSTEM -> Icons.Rounded.Smartphone
                                            ThemeMode.LIGHT -> Icons.Rounded.LightMode
                                            ThemeMode.DARK -> Icons.Rounded.DarkMode
                                        },
                                        contentDescription = mode.label,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        ) {
                            Text(mode.label)
                        }
                    }
                }

                ListItem(
                    headlineContent = { Text("大字体模式") },
                    trailingContent = { Switch(checked = largeTextModeEnabled, onCheckedChange = onLargeTextModeChange) },
                    modifier = Modifier.clickable { onLargeTextModeChange(!largeTextModeEnabled) },
                )

                ListItem(
                    headlineContent = { Text("夜间自动深色") },
                    supportingContent = { Text("当前时段：$darkModeStartHour:00 - $darkModeEndHour:00") },
                    trailingContent = {
                        Switch(
                            checked = darkModeScheduleEnabled,
                            onCheckedChange = {
                                val startHour = scheduleStartText.toIntOrNull()?.coerceIn(0, 23) ?: darkModeStartHour
                                val endHour = scheduleEndText.toIntOrNull()?.coerceIn(0, 23) ?: darkModeEndHour
                                onDarkModeScheduleChange(it, startHour, endHour)
                            }
                        )
                    },
                    modifier = Modifier.clickable { 
                        val startHour = scheduleStartText.toIntOrNull()?.coerceIn(0, 23) ?: darkModeStartHour
                        val endHour = scheduleEndText.toIntOrNull()?.coerceIn(0, 23) ?: darkModeEndHour
                        onDarkModeScheduleChange(!darkModeScheduleEnabled, startHour, endHour) 
                    },
                )

                if (darkModeScheduleEnabled) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = scheduleStartText,
                            onValueChange = { scheduleStartText = it.filter(Char::isDigit).take(2) },
                            modifier = Modifier.weight(1f),
                            label = { Text("开始 (0-23)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = scheduleEndText,
                            onValueChange = { scheduleEndText = it.filter(Char::isDigit).take(2) },
                            modifier = Modifier.weight(1f),
                            label = { Text("结束 (0-23)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                        )
                    }
                    ElevatedButton(
                        onClick = {
                            val startHour = scheduleStartText.toIntOrNull()
                            val endHour = scheduleEndText.toIntOrNull()
                            if (startHour == null || endHour == null || startHour !in 0..23 || endHour !in 0..23) {
                                errorText = "夜间时段需填写 0-23 的整数小时。"
                            } else {
                                errorText = null
                                onDarkModeScheduleChange(darkModeScheduleEnabled, startHour, endHour)
                            }
                        },
                    ) {
                        Text("保存夜间时段")
                    }
                }

                Text("应用风格", style = MaterialTheme.typography.labelLarge)
                AppTheme.entries.chunked(2).forEach { rowThemes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        rowThemes.forEach { theme ->
                            ThemeStyleCard(
                                theme = theme,
                                selected = theme == appTheme,
                                onClick = { onAppThemeChange(theme) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (rowThemes.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
        }

        item {
            SettingsSectionTitle("数据与备份")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                FilterChipSection(
                    title = "自动备份频率",
                    entries = BackupFrequency.entries,
                    selected = autoBackupFrequency,
                    label = { it.label },
                    onSelect = onAutoBackupFrequencyChange,
                )
                
                Text("手动导出与恢复", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ElevatedButton(
                        enabled = !isExporting,
                        onClick = {
                            onClearExportMessage()
                            csvExportLauncher.launch("littlegrow-${LocalDate.now()}-export.csv")
                        },
                    ) { Text("导出 CSV") }
                    ElevatedButton(
                        enabled = !isExporting,
                        onClick = {
                            onClearExportMessage()
                            pdfExportLauncher.launch("littlegrow-${LocalDate.now()}-export.pdf")
                        },
                    ) { Text("导出 PDF") }
                    ElevatedButton(
                        enabled = !isExporting,
                        onClick = {
                            onClearExportMessage()
                            backupExportLauncher.launch("littlegrow-${LocalDate.now()}.lgbackup")
                        },
                    ) { Text("完整备份") }
                    OutlinedButton(
                        enabled = !isExporting,
                        onClick = {
                            onClearExportMessage()
                            csvImportLauncher.launch(arrayOf("text/*", "*/*"))
                        },
                    ) { Text("导入 CSV") }
                    OutlinedButton(
                        enabled = !isExporting,
                        onClick = {
                            onClearExportMessage()
                            backupRestoreLauncher.launch(arrayOf("*/*"))
                        },
                    ) { Text("恢复备份") }
                }
                exportMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
        }
        
        item {
            SettingsSectionTitle("首页模块与看护人")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("首页显示模块", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HomeModule.entries.forEach { module ->
                        FilterChip(
                            selected = module in homeModules,
                            onClick = {
                                val next = homeModules.toMutableSet().apply {
                                    if (!add(module)) remove(module)
                                }
                                onHomeModulesChange(next)
                            },
                            label = { Text(module.label) },
                        )
                    }
                }
                
                OutlinedTextField(
                    value = caregiverText,
                    onValueChange = { caregiverText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("看护人列表") },
                    supportingText = { Text("用 顿号 / 逗号 / 换行 分隔") },
                )
                ElevatedButton(onClick = { onCaregiversChange(caregiverText) }) {
                    Text("保存看护人")
                }
                if (caregivers.isNotEmpty()) {
                    FilterChipSection(
                        title = "当前默认记录人",
                        entries = caregivers,
                        selected = currentCaregiver,
                        label = { it },
                        onSelect = onCurrentCaregiverChange,
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
        }

        item {
            SettingsSectionTitle("关于")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("长呀长 - LittleGrow", fontWeight = FontWeight.SemiBold)
                Text("版本 ${BuildConfig.VERSION_NAME}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "离线记录、智能默认值、成长曲线、疫苗计划、自动备份等核心功能永久免费。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        errorText?.let { message ->
            item {
                Spacer(modifier = Modifier.height(16.dp))
                ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp)
    )
}

@Composable
private fun NotificationListItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) },
        modifier = Modifier.clickable { onCheckedChange(!checked) }
    )
}

private fun handleNotificationSwitch(
    enabled: Boolean,
    granted: Boolean,
    launcher: (String) -> Unit,
    onEnable: () -> Unit,
    onDisable: () -> Unit,
) {
    if (!enabled) {
        onDisable()
    } else if (granted) {
        onEnable()
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        launcher(Manifest.permission.POST_NOTIFICATIONS)
    }
}

@Composable
private fun ThemeStyleCard(
    theme: AppTheme,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val preview = theme.previewColors()
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    }
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = if (selected) 2.dp else 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ThemePreviewStrip(preview = preview)
            Text(theme.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                text = themeStyleDescription(theme),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ThemePreviewStrip(preview: ThemePreviewColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(preview.background),
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 10.dp, top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ThemeColorDot(preview.primary)
            ThemeColorDot(preview.secondary)
            ThemeColorDot(preview.tertiary)
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ThemeColorBar(
                color = preview.primary.copy(alpha = 0.24f),
                modifier = Modifier.weight(1.2f),
            )
            ThemeColorBar(
                color = preview.secondary.copy(alpha = 0.22f),
                modifier = Modifier.weight(0.9f),
            )
            ThemeColorBar(
                color = preview.tertiary.copy(alpha = 0.2f),
                modifier = Modifier.weight(0.7f),
            )
        }
    }
}

@Composable
private fun ThemeColorDot(color: Color) {
    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun ThemeColorBar(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(12.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(color),
    )
}

private fun themeStyleDescription(theme: AppTheme): String = when (theme) {
    AppTheme.EARTHY -> "草木和蜂蜜色，日常使用最稳。"
    AppTheme.PEACH -> "奶油暖桃色，观感更柔和。"
    AppTheme.MINT -> "清透薄荷色，信息层次更轻。"
    AppTheme.LAVENDER -> "灰紫雾面色，氛围更安静。"
}
