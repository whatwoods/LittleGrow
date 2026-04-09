package com.littlegrow.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.littlegrow.app.BuildConfig
import com.littlegrow.app.data.BabyProfile
import com.littlegrow.app.data.Gender
import com.littlegrow.app.data.ThemeMode
import com.littlegrow.app.ui.dateFormatter
import java.time.LocalDate

@Composable
fun SettingsScreen(
    profile: BabyProfile?,
    themeMode: ThemeMode,
    vaccineRemindersEnabled: Boolean,
    exportMessage: String?,
    isExporting: Boolean,
    contentPadding: PaddingValues,
    onSaveProfile: (BabyProfile) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onVaccineRemindersChange: (Boolean) -> Unit,
    onExportCsv: (Uri) -> Unit,
    onExportPdf: (Uri) -> Unit,
    onClearExportMessage: () -> Unit,
) {
    val context = LocalContext.current
    val notificationPermissionGranted =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            onVaccineRemindersChange(true)
        }
    }
    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri ->
        uri?.let(onExportCsv)
    }
    val pdfExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
    ) { uri ->
        uri?.let(onExportPdf)
    }
    var name by rememberSaveable(profile?.name) { mutableStateOf(profile?.name.orEmpty()) }
    var birthday by rememberSaveable(profile?.birthday) {
        mutableStateOf(profile?.birthday?.format(dateFormatter) ?: LocalDate.now().format(dateFormatter))
    }
    var gender by rememberSaveable(profile?.gender) { mutableStateOf(profile?.gender ?: Gender.GIRL) }
    var errorText by rememberSaveable { mutableStateOf<String?>(null) }

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
                        "资料、主题和应用说明都集中在这里。数据只保存在本地，不需要账号。",
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
                    Text("数据导出", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "将当前本地数据导出为 CSV 或 PDF，适合手动备份、打印或就诊时展示。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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
                            Text(if (isExporting) "处理中..." else "导出 CSV")
                        }
                        OutlinedButton(
                            enabled = !isExporting,
                            onClick = {
                                onClearExportMessage()
                                pdfExportLauncher.launch("littlegrow-${LocalDate.now()}-export.pdf")
                            },
                        ) {
                            Text(if (isExporting) "处理中..." else "导出 PDF")
                        }
                    }
                    exportMessage?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Text(
                        "系统会让你选择保存位置；Android 13 及以上如果开启疫苗提醒，还需要授予通知权限。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
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
                    Text("提醒管理", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("疫苗接种提醒")
                            Switch(
                                checked = vaccineRemindersEnabled,
                                onCheckedChange = { enabled ->
                                    if (!enabled) {
                                        onVaccineRemindersChange(false)
                                    } else if (notificationPermissionGranted) {
                                        onVaccineRemindersChange(true)
                                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                },
                            )
                        }
                        Text(
                            if (notificationPermissionGranted) {
                                "在建议接种日前 3 天发送本地通知。"
                            } else {
                                "开启前需要授予通知权限。"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
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
                    Text("宝宝资料", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("昵称") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = birthday,
                        onValueChange = { birthday = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("生日") },
                        supportingText = { Text("格式：yyyy-MM-dd") },
                        singleLine = true,
                    )
                    ChipRow(
                        title = "性别",
                        entries = Gender.entries,
                        selected = gender,
                        label = { it.label },
                        onSelect = { gender = it },
                    )
                    errorText?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                    ElevatedButton(
                        onClick = {
                            val parsedBirthday = runCatching { LocalDate.parse(birthday.trim(), dateFormatter) }.getOrNull()
                            if (name.isBlank()) {
                                errorText = "昵称不能为空。"
                                return@ElevatedButton
                            }
                            if (parsedBirthday == null) {
                                errorText = "生日格式不对，请使用 yyyy-MM-dd。"
                                return@ElevatedButton
                            }
                            errorText = null
                            onSaveProfile(
                                BabyProfile(
                                    name = name.trim(),
                                    birthday = parsedBirthday,
                                    gender = gender,
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
                    Text("主题模式", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    ChipRow(
                        title = "外观",
                        entries = ThemeMode.entries,
                        selected = themeMode,
                        label = { it.label },
                        onSelect = onThemeModeChange,
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
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("关于", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("版本 ${BuildConfig.VERSION_NAME}")
                    Text(
                        "当前提供离线宝宝资料、母乳计时器、喂养/睡眠/排泄/健康/活动记录、WHO 生长百分位线、疫苗计划、里程碑照片、CSV/PDF 导出和桌面快捷小组件。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> ChipRow(
    title: String,
    entries: List<T>,
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
            entries.forEach { entry ->
                FilterChip(
                    selected = entry == selected,
                    onClick = { onSelect(entry) },
                    label = { Text(label(entry)) },
                )
            }
        }
    }
}
