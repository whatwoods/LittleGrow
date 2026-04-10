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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.littlegrow.app.ui.NativeDatePickerField
import com.littlegrow.app.ui.PhotoActionRow
import com.littlegrow.app.ui.dateFormatter
import com.littlegrow.app.ui.rememberManagedPhotoAttachment
import com.littlegrow.app.ui.theme.ThemePreviewColors
import com.littlegrow.app.ui.theme.previewColors
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("设置", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "这里统一管理提醒、主题、备份恢复、看护人和首页显示模块。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            ElevatedCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("导出、备份与恢复", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            enabled = !isExporting,
                            onClick = {
                                onClearExportMessage()
                                csvExportLauncher.launch("littlegrow-${LocalDate.now()}-export.csv")
                            },
                        ) {
                            Text("导出 CSV")
                        }
                        OutlinedButton(
                            enabled = !isExporting,
                            onClick = {
                                onClearExportMessage()
                                pdfExportLauncher.launch("littlegrow-${LocalDate.now()}-export.pdf")
                            },
                        ) {
                            Text("导出 PDF")
                        }
                        OutlinedButton(
                            enabled = !isExporting,
                            onClick = {
                                onClearExportMessage()
                                backupExportLauncher.launch("littlegrow-${LocalDate.now()}.lgbackup")
                            },
                        ) {
                            Text("完整备份")
                        }
                        OutlinedButton(
                            enabled = !isExporting,
                            onClick = {
                                onClearExportMessage()
                                backupRestoreLauncher.launch(arrayOf("*/*"))
                            },
                        ) {
                            Text("恢复备份")
                        }
                        OutlinedButton(
                            enabled = !isExporting,
                            onClick = {
                                onClearExportMessage()
                                csvImportLauncher.launch(arrayOf("text/*", "*/*"))
                            },
                        ) {
                            Text("导入 CSV")
                        }
                    }
                    Text(
                        "完整备份会打包数据和照片；CSV 导入是增量合并，不会直接覆盖现有记录。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    exportMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        item {
            ElevatedCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("自动备份与工具", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "自动备份会把最近一次完整快照写入系统下载目录；就医摘要可直接复制或分享。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FilterChipSection(
                        title = "自动备份频率",
                        entries = BackupFrequency.entries,
                        selected = autoBackupFrequency,
                        label = { it.label },
                        onSelect = onAutoBackupFrequencyChange,
                    )
                    ElevatedButton(onClick = onOpenMedicalSummary) {
                        Text("打开就医摘要")
                    }
                }
            }
        }

        item {
            ElevatedCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("提醒管理", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    NotificationSwitchRow(
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
                    NotificationSwitchRow(
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
                    NotificationSwitchRow(
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
                    NotificationSwitchRow(
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
                }
            }
        }

        item {
            ElevatedCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("宝宝资料", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        BabyAvatar(
                            avatarPath = avatarAttachment.photoPath,
                            contentDescription = "宝宝头像",
                            modifier = Modifier.size(92.dp),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
                            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text("头像", style = MaterialTheme.typography.labelLarge)
                            Text(
                                "不上传也会使用新的默认宝贝头像。拍照和选图都会存到应用管理目录里。",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                    PhotoActionRow(
                        title = "头像",
                        removeLabel = "移除头像",
                        hasPhoto = avatarAttachment.photoPath != null,
                        onTakePhoto = avatarAttachment.onTakePhoto,
                        onPickPhoto = avatarAttachment.onPickPhoto,
                        onRemovePhoto = avatarAttachment.onRemovePhoto,
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("昵称") },
                        singleLine = true,
                    )
                    NativeDatePickerField(
                        value = birthday,
                        onValueChange = { birthday = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = "生日",
                        supportingText = "点击选择日期",
                        maxDate = LocalDate.now(),
                    )
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
            }
        }

        item {
            ElevatedCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("看护人", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = caregiverText,
                        onValueChange = { caregiverText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("看护人列表") },
                        supportingText = { Text("用 顿号 / 逗号 / 换行 分隔，例如：妈妈、爸爸、奶奶") },
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
            }
        }

        item {
            ElevatedCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("主题与显示", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    FilterChipSection(
                        title = "外观",
                        entries = ThemeMode.entries,
                        selected = themeMode,
                        label = { it.label },
                        onSelect = onThemeModeChange,
                    )
                    SettingSwitchRow(
                        title = "大字体模式",
                        checked = largeTextModeEnabled,
                        onCheckedChange = onLargeTextModeChange,
                    )
                    SettingSwitchRow(
                        title = "夜间自动深色",
                        checked = darkModeScheduleEnabled,
                        onCheckedChange = {
                            val startHour = scheduleStartText.toIntOrNull()?.coerceIn(0, 23) ?: darkModeStartHour
                            val endHour = scheduleEndText.toIntOrNull()?.coerceIn(0, 23) ?: darkModeEndHour
                            onDarkModeScheduleChange(it, startHour, endHour)
                        },
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = scheduleStartText,
                            onValueChange = { scheduleStartText = it.filter(Char::isDigit).take(2) },
                            modifier = Modifier.weight(1f),
                            label = { Text("开始小时") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = scheduleEndText,
                            onValueChange = { scheduleEndText = it.filter(Char::isDigit).take(2) },
                            modifier = Modifier.weight(1f),
                            label = { Text("结束小时") },
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
                    Text("当前夜间时段：$darkModeStartHour:00 - $darkModeEndHour:00", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("风格", style = MaterialTheme.typography.labelLarge)
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
                }
            }
        }

        item {
            ElevatedCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("首页模块", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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
                }
            }
        }

        item {
            ElevatedCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("关于", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("版本 ${BuildConfig.VERSION_NAME}")
                    Text(
                        "当前提供离线记录、智能默认值、Widget 一键记录、成长曲线、疫苗计划、智能提醒、完整备份/恢复、CSV 导入、多看护人标记和阶段化首页。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        errorText?.let { message ->
            item {
                ElevatedCard {
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
private fun NotificationSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SettingSwitchRow(
        title = title,
        checked = checked,
        onCheckedChange = onCheckedChange,
    )
}

@Composable
private fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
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
