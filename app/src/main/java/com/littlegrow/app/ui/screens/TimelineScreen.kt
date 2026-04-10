package com.littlegrow.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.littlegrow.app.data.BabyProfile
import com.littlegrow.app.data.DiaperEntity
import com.littlegrow.app.data.FeedingEntity
import com.littlegrow.app.data.GrowthEntity
import com.littlegrow.app.data.MilestoneCategory
import com.littlegrow.app.data.MilestoneDraft
import com.littlegrow.app.data.MilestoneEntity
import com.littlegrow.app.data.MonthlyGuide
import com.littlegrow.app.data.StageReportEntry
import com.littlegrow.app.data.StageReportGenerator
import com.littlegrow.app.data.SleepEntity
import com.littlegrow.app.ui.NativeDatePickerField
import com.littlegrow.app.ui.PhotoActionRow
import com.littlegrow.app.ui.PhotoPreviewCard
import com.littlegrow.app.ui.dateFormatter
import com.littlegrow.app.ui.formatDate
import com.littlegrow.app.ui.rememberManagedPhotoAttachment
import java.time.Duration
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun TimelineScreen(
    profile: BabyProfile?,
    feedings: List<FeedingEntity>,
    sleeps: List<SleepEntity>,
    diapers: List<DiaperEntity>,
    growthRecords: List<GrowthEntity>,
    milestones: List<MilestoneEntity>,
    contentPadding: PaddingValues,
    onAddMilestone: (MilestoneDraft) -> Unit,
    onUpdateMilestone: (Long, MilestoneDraft) -> Unit,
    onDeleteMilestone: (Long) -> Unit,
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var editingMilestone by remember { mutableStateOf<MilestoneEntity?>(null) }
    val ageDays = profile?.birthday?.let { ChronoUnit.DAYS.between(it, LocalDate.now()).toInt() } ?: 0
    val stageReports = profile?.birthday?.let { birthday ->
        StageReportGenerator.reachedReports(
            birthday = birthday,
            ageDays = ageDays,
            feedings = feedings,
            sleeps = sleeps,
            diapers = diapers,
            growthRecords = growthRecords,
            milestones = milestones,
        )
    }.orEmpty()
    val monthlyGuides = (0..(profile?.birthday?.let { ChronoUnit.MONTHS.between(it, LocalDate.now()).toInt() } ?: 0).coerceAtMost(24))
        .mapNotNull(MonthlyGuide::guideFor)
        .reversed()

    Box(modifier = Modifier.fillMaxSize()) {
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
                ElevatedCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("时光轴", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "把翻身、坐稳、发出第一声“咿呀”这些节点记下来，还能给里程碑附一张照片。",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (stageReports.isNotEmpty()) {
                item {
                    TimelineSectionTitle("阶段小结")
                }
                items(stageReports, key = { it.day }) { report ->
                    StageReportCard(report)
                }
            }

            if (monthlyGuides.isNotEmpty()) {
                item {
                    TimelineSectionTitle("月龄指南")
                }
                items(monthlyGuides, key = { it.month }) { guide ->
                    ElevatedCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(guide.title, fontWeight = FontWeight.SemiBold)
                            Text(guide.developmentHighlights.joinToString(" · "), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                TimelineSectionTitle("年度总结")
            }
            item {
                YearlySummaryCard(
                    feedings = feedings,
                    sleeps = sleeps,
                    diapers = diapers,
                )
            }

            item {
                TimelineSectionTitle("里程碑")
            }

            if (milestones.isEmpty()) {
                item { EmptyRecordCard("还没有里程碑记录。") }
            } else {
                items(milestones, key = { it.id }) { milestone ->
                    ElevatedCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(milestone.title, fontWeight = FontWeight.SemiBold)
                            Text("${milestone.category.label} · ${milestone.achievedDate.formatDate()}")
                            profile?.birthday?.let { birthday ->
                                val day = ChronoUnit.DAYS.between(birthday, milestone.achievedDate) + 1
                                Text("出生第 ${day} 天", color = MaterialTheme.colorScheme.tertiary)
                            }
                            milestone.photoPath?.let {
                                PhotoPreviewCard(filePath = it, contentDescription = "里程碑照片")
                            }
                            milestone.note?.let {
                                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            androidx.compose.foundation.layout.Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                TextButton(
                                    onClick = {
                                        editingMilestone = milestone
                                        showDialog = true
                                    },
                                ) {
                                    Text("编辑")
                                }
                                TextButton(onClick = { onDeleteMilestone(milestone.id) }) {
                                    Text("删除")
                                }
                            }
                        }
                    }
                }
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
                editingMilestone = null
                showDialog = true
            },
        ) {
            androidx.compose.material3.Icon(Icons.Rounded.Add, contentDescription = "添加里程碑")
        }
    }

    if (showDialog) {
        AddMilestoneDialog(
            initial = editingMilestone,
            onDismiss = {
                editingMilestone = null
                showDialog = false
            },
            onSubmit = { draft ->
                val editing = editingMilestone
                if (editing == null) {
                    onAddMilestone(draft)
                } else {
                    onUpdateMilestone(editing.id, draft)
                }
                editingMilestone = null
                showDialog = false
            },
        )
    }
}

@Composable
private fun TimelineSectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun StageReportCard(report: StageReportEntry) {
    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(report.report.title, fontWeight = FontWeight.SemiBold)
            report.report.summary.forEach { line ->
                Text(line, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun YearlySummaryCard(
    feedings: List<FeedingEntity>,
    sleeps: List<SleepEntity>,
    diapers: List<DiaperEntity>,
) {
    val totalNightWakes = sleeps
        .sortedBy { it.startTime }
        .zipWithNext()
        .count { (previous, next) -> Duration.between(previous.endTime, next.startTime).toMinutes() > 10 }
    val busiestDay = feedings
        .groupingBy { it.happenedAt.toLocalDate() }
        .eachCount()
        .maxByOrNull { it.value }
    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("累计喂养 ${feedings.size} 次", fontWeight = FontWeight.SemiBold)
            Text(
                "累计母乳时长 ${feedings.sumOf { it.durationMinutes ?: 0 }} 分钟，换尿布 ${diapers.size} 次。",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "累计夜间醒来约 $totalNightWakes 次。",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            busiestDay?.let {
                Text(
                    "记录最密集的一天：${it.key.formatDate()}（${it.value} 次喂养）",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AddMilestoneDialog(
    initial: MilestoneEntity?,
    onDismiss: () -> Unit,
    onSubmit: (MilestoneDraft) -> Unit,
) {
    var title by rememberSaveable(initial?.id) { mutableStateOf(initial?.title.orEmpty()) }
    var category by rememberSaveable(initial?.id) { mutableStateOf(initial?.category ?: MilestoneCategory.GROSS_MOTOR) }
    var date by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.achievedDate?.format(dateFormatter) ?: LocalDate.now().format(dateFormatter))
    }
    var note by rememberSaveable(initial?.id) { mutableStateOf(initial?.note.orEmpty()) }
    var errorText by rememberSaveable(initial?.id) { mutableStateOf<String?>(null) }
    val photoAttachment = rememberManagedPhotoAttachment(
        initialPhotoPath = initial?.photoPath,
        photoTag = "milestone",
        onError = { errorText = it },
    )
    val photoPath = photoAttachment.photoPath

    AlertDialog(
        onDismissRequest = {
            photoAttachment.discardChanges()
            onDismiss()
        },
        title = { Text(if (initial == null) "添加里程碑" else "编辑里程碑") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("标题") },
                    singleLine = true,
                )
                FilterChipSection("类别", MilestoneCategory.entries, category, { it.label }) { category = it }
                NativeDatePickerField(
                    value = date,
                    onValueChange = { date = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = "达成日期",
                    supportingText = "点击选择日期",
                    maxDate = LocalDate.now(),
                )
                photoPath?.let {
                    PhotoPreviewCard(filePath = it, contentDescription = "里程碑照片预览")
                }
                PhotoActionRow(
                    hasPhoto = photoPath != null,
                    onTakePhoto = photoAttachment.onTakePhoto,
                    onPickPhoto = photoAttachment.onPickPhoto,
                    onRemovePhoto = photoAttachment.onRemovePhoto,
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
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsedDate = runCatching { LocalDate.parse(date.trim(), dateFormatter) }.getOrNull()
                    if (title.isBlank()) {
                        errorText = "里程碑标题不能为空。"
                    } else if (date.isBlank() || parsedDate == null) {
                        errorText = "请选择达成日期。"
                    } else {
                        photoAttachment.commitChanges()
                        onSubmit(
                            MilestoneDraft(
                                title = title,
                                category = category,
                                achievedDate = parsedDate,
                                photoPath = photoPath,
                                note = note,
                            ),
                        )
                    }
                },
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                photoAttachment.discardChanges()
                onDismiss()
            }) {
                Text("取消")
            }
        },
    )
}
