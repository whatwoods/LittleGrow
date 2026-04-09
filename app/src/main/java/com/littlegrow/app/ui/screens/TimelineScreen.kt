package com.littlegrow.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.littlegrow.app.data.BabyProfile
import com.littlegrow.app.data.MilestoneCategory
import com.littlegrow.app.data.MilestoneDraft
import com.littlegrow.app.data.MilestoneEntity
import com.littlegrow.app.media.PendingPhotoCapture
import com.littlegrow.app.media.PhotoStore
import com.littlegrow.app.ui.PhotoActionRow
import com.littlegrow.app.ui.PhotoPreviewCard
import com.littlegrow.app.ui.dateFormatter
import com.littlegrow.app.ui.formatDate
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun TimelineScreen(
    profile: BabyProfile?,
    milestones: List<MilestoneEntity>,
    contentPadding: PaddingValues,
    onAddMilestone: (MilestoneDraft) -> Unit,
    onUpdateMilestone: (Long, MilestoneDraft) -> Unit,
    onDeleteMilestone: (Long) -> Unit,
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var editingMilestone by remember { mutableStateOf<MilestoneEntity?>(null) }

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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddMilestoneDialog(
    initial: MilestoneEntity?,
    onDismiss: () -> Unit,
    onSubmit: (MilestoneDraft) -> Unit,
) {
    val context = LocalContext.current
    var title by rememberSaveable(initial?.id) { mutableStateOf(initial?.title.orEmpty()) }
    var category by rememberSaveable(initial?.id) { mutableStateOf(initial?.category ?: MilestoneCategory.GROSS_MOTOR) }
    var date by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.achievedDate?.format(dateFormatter) ?: LocalDate.now().format(dateFormatter))
    }
    var photoPath by rememberSaveable(initial?.id) { mutableStateOf(initial?.photoPath) }
    var note by rememberSaveable(initial?.id) { mutableStateOf(initial?.note.orEmpty()) }
    var errorText by rememberSaveable(initial?.id) { mutableStateOf<String?>(null) }
    var pendingCapture by remember { mutableStateOf<PendingPhotoCapture?>(null) }

    fun replacePhoto(newPath: String?) {
        if (photoPath != initial?.photoPath) {
            PhotoStore.deletePhoto(photoPath)
        }
        photoPath = newPath
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            runCatching { PhotoStore.importPhoto(context, it, "milestone") }
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
        if (photoPath != initial?.photoPath) {
            PhotoStore.deletePhoto(photoPath)
        }
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = ::dismissDialog,
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("类别", style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MilestoneCategory.entries.forEach { option ->
                            FilterChip(
                                selected = option == category,
                                onClick = { category = option },
                                label = { Text(option.label) },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("达成日期") },
                    supportingText = { Text("格式：yyyy-MM-dd") },
                    singleLine = true,
                )
                photoPath?.let {
                    PhotoPreviewCard(filePath = it, contentDescription = "里程碑照片预览")
                }
                PhotoActionRow(
                    hasPhoto = photoPath != null,
                    onTakePhoto = {
                        val capture = PhotoStore.createPendingCapture(context, "milestone")
                        pendingCapture = capture
                        takePhotoLauncher.launch(capture.uri)
                    },
                    onPickPhoto = {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onRemovePhoto = { replacePhoto(null) },
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
                    } else if (parsedDate == null) {
                        errorText = "日期格式不对，请使用 yyyy-MM-dd。"
                    } else {
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
            TextButton(onClick = ::dismissDialog) {
                Text("取消")
            }
        },
    )
}
