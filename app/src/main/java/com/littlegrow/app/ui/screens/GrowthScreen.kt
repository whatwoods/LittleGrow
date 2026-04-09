package com.littlegrow.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.littlegrow.app.data.BabyProfile
import com.littlegrow.app.data.GrowthDraft
import com.littlegrow.app.data.GrowthEntity
import com.littlegrow.app.data.GrowthMetric
import com.littlegrow.app.data.VaccineEntity
import com.littlegrow.app.data.WhoGrowthStandards
import com.littlegrow.app.ui.NativeDatePickerField
import com.littlegrow.app.ui.dateFormatter
import com.littlegrow.app.ui.formatDate
import com.littlegrow.app.ui.formatMetric
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun GrowthScreen(
    profile: BabyProfile?,
    growthRecords: List<GrowthEntity>,
    vaccines: List<VaccineEntity>,
    contentPadding: PaddingValues,
    onAddGrowth: (GrowthDraft) -> Unit,
    onUpdateGrowth: (Long, GrowthDraft) -> Unit,
    onDeleteGrowth: (Long) -> Unit,
    onToggleVaccineDone: (String, Boolean) -> Unit,
) {
    var metric by rememberSaveable { mutableStateOf(GrowthMetric.WEIGHT) }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var editingGrowth by remember { mutableStateOf<GrowthEntity?>(null) }

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
                        Text("成长发育", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "体重、身高和头围会叠加 WHO 官方百分位参考线，方便在就诊时快速说明趋势。",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GrowthMetric.entries.forEach { candidate ->
                        FilterChip(
                            selected = candidate == metric,
                            onClick = { metric = candidate },
                            label = { Text(candidate.label) },
                        )
                    }
                }
            }

            item {
                GrowthChartCard(
                    profile = profile,
                    records = growthRecords,
                    metric = metric,
                )
            }

            if (growthRecords.isEmpty()) {
                item { EmptyRecordCard("还没有生长记录。") }
            } else {
                items(growthRecords, key = { it.id }) { growth ->
                    ElevatedCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(growth.date.formatDate(), fontWeight = FontWeight.SemiBold)
                            Text(
                                "体重 ${growth.weightKg.formatMetric(GrowthMetric.WEIGHT)} · " +
                                    "身高 ${growth.heightCm.formatMetric(GrowthMetric.HEIGHT)} · " +
                                    "头围 ${growth.headCircCm.formatMetric(GrowthMetric.HEAD)}",
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                TextButton(
                                    onClick = {
                                        editingGrowth = growth
                                        showDialog = true
                                    },
                                ) {
                                    Text("编辑")
                                }
                                TextButton(onClick = { onDeleteGrowth(growth.id) }) {
                                    Text("删除")
                                }
                            }
                        }
                    }
                }
            }

            item {
                VaccineOverviewCard(hasVaccines = vaccines.isNotEmpty())
            }

            if (vaccines.isNotEmpty()) {
                items(vaccines, key = { it.scheduleKey }) { vaccine ->
                    VaccineCard(
                        vaccine = vaccine,
                        onToggleDone = onToggleVaccineDone,
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
                editingGrowth = null
                showDialog = true
            },
        ) {
            androidx.compose.material3.Icon(Icons.Rounded.Add, contentDescription = "添加生长记录")
        }
    }

    if (showDialog) {
        AddGrowthDialog(
            initial = editingGrowth,
            onDismiss = {
                editingGrowth = null
                showDialog = false
            },
            onSubmit = { draft ->
                val editing = editingGrowth
                if (editing == null) {
                    onAddGrowth(draft)
                } else {
                    onUpdateGrowth(editing.id, draft)
                }
                editingGrowth = null
                showDialog = false
            },
        )
    }
}

@Composable
private fun VaccineOverviewCard(hasVaccines: Boolean) {
    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("疫苗管家", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "根据生日自动生成国家免疫规划接种时间表，支持标记已接种。",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!hasVaccines) {
                Text("宝宝资料保存后会自动生成接种计划。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun VaccineCard(
    vaccine: VaccineEntity,
    onToggleDone: (String, Boolean) -> Unit,
) {
    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("${vaccine.vaccineName} · 第 ${vaccine.doseNumber} 针", fontWeight = FontWeight.SemiBold)
            Text("建议日期 ${vaccine.scheduledDate.formatDate()}")
            val statusText = if (vaccine.isDone) {
                val actual = vaccine.actualDate?.formatDate() ?: vaccine.scheduledDate.formatDate()
                "已接种 · 实际日期 $actual"
            } else {
                val days = ChronoUnit.DAYS.between(LocalDate.now(), vaccine.scheduledDate)
                when {
                    days < 0 -> "已逾期 ${-days} 天"
                    days == 0L -> "今天建议接种"
                    else -> "距建议日期还有 ${days} 天"
                }
            }
            Text(
                statusText,
                color = if (vaccine.isDone) {
                    MaterialTheme.colorScheme.primary
                } else if (vaccine.scheduledDate.isBefore(LocalDate.now())) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Text(
                "提醒会在建议日期前 3 天发出。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(onClick = { onToggleDone(vaccine.scheduleKey, !vaccine.isDone) }) {
                Text(if (vaccine.isDone) "撤销已接种" else "标记已接种")
            }
        }
    }
}

@Composable
private fun GrowthChartCard(
    profile: BabyProfile?,
    records: List<GrowthEntity>,
    metric: GrowthMetric,
) {
    val context = LocalContext.current
    val measurementPoints = remember(profile, records, metric) {
        buildMeasurementPoints(profile, records, metric)
    }
    val reference = remember(profile?.gender, metric) {
        profile?.let { WhoGrowthStandards.load(context, it.gender, metric) }
    }
    val visibleMaxAgeDays = remember(profile, measurementPoints) {
        determineChartWindowDays(profile, measurementPoints)
    }
    val visibleReferencePoints = remember(reference, visibleMaxAgeDays) {
        reference?.points?.filter { it.ageDays <= visibleMaxAgeDays }.orEmpty()
    }
    val latestBand = remember(reference, measurementPoints) {
        val latest = measurementPoints.lastOrNull() ?: return@remember null
        reference?.percentileBand(latest.first, latest.second)
    }

    ElevatedCard {
        val primaryColor = MaterialTheme.colorScheme.primary
        val accentColor = MaterialTheme.colorScheme.tertiary
        val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        val referenceColors = listOf(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
            MaterialTheme.colorScheme.outline,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("${metric.label}趋势", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (measurementPoints.isEmpty() && profile == null) {
                Text("先填写宝宝生日和性别，再补一条生长记录，这里会叠加 WHO 百分位线。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                val yCandidates = buildList {
                    addAll(measurementPoints.map { it.second })
                    visibleReferencePoints.forEach { point ->
                        add(point.p3)
                        add(point.p15)
                        add(point.p50)
                        add(point.p85)
                        add(point.p97)
                    }
                }
                val minY = (yCandidates.minOrNull() ?: 0f) - when (metric) {
                    GrowthMetric.WEIGHT -> 0.5f
                    GrowthMetric.HEIGHT, GrowthMetric.HEAD -> 1f
                }
                val maxY = (yCandidates.maxOrNull() ?: 1f) + when (metric) {
                    GrowthMetric.WEIGHT -> 0.5f
                    GrowthMetric.HEIGHT, GrowthMetric.HEAD -> 1f
                }
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                ) {
                    val width = size.width
                    val height = size.height
                    val horizontalPadding = 32f
                    val verticalPadding = 24f
                    val ySpread = (maxY - minY).takeIf { it > 0f } ?: 1f
                    val chartWidth = width - horizontalPadding * 2
                    val chartHeight = height - verticalPadding * 2
                    fun project(ageDays: Int, value: Float): Offset {
                        val xProgress = ageDays.toFloat() / visibleMaxAgeDays.coerceAtLeast(1)
                        val yProgress = (value - minY) / ySpread
                        return Offset(
                            x = horizontalPadding + chartWidth * xProgress,
                            y = height - verticalPadding - chartHeight * yProgress,
                        )
                    }

                    repeat(4) { index ->
                        val y = verticalPadding + chartHeight * index / 3f
                        drawLine(
                            color = gridColor,
                            start = Offset(horizontalPadding, y),
                            end = Offset(width - horizontalPadding, y),
                            strokeWidth = 2f,
                        )
                    }

                    if (visibleReferencePoints.isNotEmpty()) {
                        reference?.percentiles?.forEachIndexed { index, percentile ->
                            val path = Path().apply {
                                visibleReferencePoints.forEachIndexed { pointIndex, point ->
                                    val offset = project(point.ageDays, point.valueFor(percentile))
                                    if (pointIndex == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = referenceColors[index],
                                style = Stroke(
                                    width = if (percentile == 50) 4f else 2.5f,
                                    pathEffect = if (percentile == 50) null else PathEffect.dashPathEffect(floatArrayOf(10f, 10f)),
                                    cap = StrokeCap.Round,
                                ),
                            )
                        }
                    }

                    if (measurementPoints.isNotEmpty()) {
                        val measurementPath = Path().apply {
                            measurementPoints.forEachIndexed { index, point ->
                                val offset = project(point.first, point.second)
                                if (index == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
                            }
                        }
                        if (measurementPoints.size > 1) {
                            drawPath(
                                path = measurementPath,
                                color = primaryColor,
                                style = Stroke(width = 8f, cap = StrokeCap.Round),
                            )
                        }
                        measurementPoints.forEach { point ->
                            drawCircle(color = accentColor, radius = 9f, center = project(point.first, point.second))
                        }
                    }
                }
                Text(
                    when {
                        latestBand != null -> "最新 ${metric.label} 约位于 WHO $latestBand。"
                        profile == null -> "补充宝宝资料后，会叠加 WHO 官方百分位线。"
                        else -> "WHO 百分位线显示为 P3 / P15 / P50 / P85 / P97。"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (metric == GrowthMetric.HEIGHT) {
                    Text("身高参考线采用 WHO 0-5 岁身长/身高标准；2 岁前主要对应身长。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("参考线来源为 WHO Child Growth Standards 0-5 岁百分位表。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun AddGrowthDialog(
    initial: GrowthEntity?,
    onDismiss: () -> Unit,
    onSubmit: (GrowthDraft) -> Unit,
) {
    var date by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.date?.format(dateFormatter) ?: LocalDate.now().format(dateFormatter))
    }
    var weight by rememberSaveable(initial?.id) { mutableStateOf(initial?.weightKg?.toString().orEmpty()) }
    var height by rememberSaveable(initial?.id) { mutableStateOf(initial?.heightCm?.toString().orEmpty()) }
    var head by rememberSaveable(initial?.id) { mutableStateOf(initial?.headCircCm?.toString().orEmpty()) }
    var errorText by rememberSaveable(initial?.id) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "添加生长记录" else "编辑生长记录") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NativeDatePickerField(
                    value = date,
                    onValueChange = { date = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = "日期",
                    supportingText = "点击选择日期",
                    maxDate = LocalDate.now(),
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("体重（kg）") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("身高（cm）") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = head,
                    onValueChange = { head = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("头围（cm）") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )
                errorText?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsedDate = runCatching { LocalDate.parse(date.trim(), dateFormatter) }.getOrNull()
                    if (date.isBlank() || parsedDate == null) {
                        errorText = "请选择日期。"
                    } else {
                        val weightValue = weight.trim().takeIf { it.isNotEmpty() }?.toFloatOrNull()
                        val heightValue = height.trim().takeIf { it.isNotEmpty() }?.toFloatOrNull()
                        val headValue = head.trim().takeIf { it.isNotEmpty() }?.toFloatOrNull()
                        if (weightValue == null && heightValue == null && headValue == null) {
                            errorText = "至少填写一项生长数据。"
                        } else {
                            onSubmit(GrowthDraft(parsedDate, weightValue, heightValue, headValue))
                        }
                    }
                },
            ) {
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

private fun buildMeasurementPoints(
    profile: BabyProfile?,
    records: List<GrowthEntity>,
    metric: GrowthMetric,
): List<Pair<Int, Float>> {
    if (profile == null) return emptyList()
    return records
        .sortedBy { it.date }
        .mapNotNull { record ->
            val ageDays = ChronoUnit.DAYS.between(profile.birthday, record.date).toInt()
            val value = when (metric) {
                GrowthMetric.WEIGHT -> record.weightKg
                GrowthMetric.HEIGHT -> record.heightCm
                GrowthMetric.HEAD -> record.headCircCm
            }
            if (ageDays < 0 || value == null) null else ageDays to value
        }
}

private fun determineChartWindowDays(
    profile: BabyProfile?,
    points: List<Pair<Int, Float>>,
): Int {
    val currentAgeDays = profile?.let { ChronoUnit.DAYS.between(it.birthday, LocalDate.now()).toInt() } ?: 0
    val relevantAge = maxOf(currentAgeDays, points.maxOfOrNull { it.first } ?: 0)
    return when {
        relevantAge <= 180 -> 180
        relevantAge <= 365 -> 365
        relevantAge <= 730 -> 730
        else -> 1_856
    }
}
