package com.littlegrow.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
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
import com.littlegrow.app.ui.components.ExpressiveFloatingActionButton as FloatingActionButton
import com.littlegrow.app.ui.components.ExpressiveTextButton as TextButton
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

    // Combine all timeline items logically, assuming milestones are the primary timeline events
    // For simplicity, we just list the items with the timeline visual structure
    val totalMilestones = milestones.size

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding() + 16.dp,
                bottom = contentPadding.calculateBottomPadding() + 96.dp,
            ),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("时光轴", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "把翻身、坐稳、发出第一声“咿呀”这些节点记下来，还能给里程碑附一张照片。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (milestones.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("还没有里程碑记录。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                itemsIndexed(milestones, key = { _, m -> m.id }) { index, milestone ->
                    TimelineRow(
                        isFirst = index == 0,
                        isLast = index == totalMilestones - 1,
                        nodeColor = MaterialTheme.colorScheme.primary,
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(milestone.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                profile?.birthday?.let { birthday ->
                                    val day = ChronoUnit.DAYS.between(birthday, milestone.achievedDate) + 1
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            "出生第 $day 天",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                            
                            Text("${milestone.achievedDate.formatDate()} · ${milestone.category.label}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            
                            milestone.note?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            
                            milestone.photoPath?.let {
                                Box(modifier = Modifier.padding(top = 4.dp)) {
                                    PhotoPreviewCard(filePath = it, contentDescription = "里程碑照片")
                                }
                            }
                            
                            Row(
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

            if (stageReports.isNotEmpty() || monthlyGuides.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "更多记录",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (stageReports.isNotEmpty()) {
                itemsIndexed(stageReports, key = { _, it -> "sr_" + it.day }) { _, report ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        StageReportCard(report)
                    }
                }
            }

            if (monthlyGuides.isNotEmpty()) {
                itemsIndexed(monthlyGuides, key = { _, it -> "mg_" + it.month }) { _, guide ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
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
            }

            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                    YearlySummaryCard(
                        feedings = feedings,
                        sleeps = sleeps,
                        diapers = diapers,
                    )
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
private fun TimelineRow(
    isFirst: Boolean,
    isLast: Boolean,
    nodeColor: Color,
    content: @Composable () -> Unit
) {
    val lineColor = MaterialTheme.colorScheme.surfaceVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val nodeY = 20.dp.toPx()
                val lineX = 24.dp.toPx()
                
                if (!isFirst) {
                    drawLine(lineColor, androidx.compose.ui.geometry.Offset(lineX, 0f), androidx.compose.ui.geometry.Offset(lineX, nodeY), strokeWidth = 2.dp.toPx())
                }
                if (!isLast) {
                    drawLine(lineColor, androidx.compose.ui.geometry.Offset(lineX, nodeY), androidx.compose.ui.geometry.Offset(lineX, size.height), strokeWidth = 2.dp.toPx())
                }
                drawCircle(nodeColor, radius = 5.dp.toPx(), center = androidx.compose.ui.geometry.Offset(lineX, nodeY))
            }
            .padding(start = 48.dp, end = 16.dp, bottom = 24.dp)
    ) {
        content()
    }
}

@Composable
private fun StageReportCard(report: StageReportEntry) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
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
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("年度总结", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
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
                // FilterChipSection assumes there's a custom composable available. Since it was in the original, we keep it.
                // Assuming it is accessible (e.g. defined in Forms.kt or something).
                // Oh wait, FilterChipSection is not defined in this file, but it was in the original! So it's fine.
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
