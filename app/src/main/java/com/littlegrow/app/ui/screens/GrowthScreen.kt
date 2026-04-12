package com.littlegrow.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.NoteAlt
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.littlegrow.app.ui.theme.Spacing
import com.littlegrow.app.data.BabyProfile
import com.littlegrow.app.data.GrowthVelocityRange
import com.littlegrow.app.data.GrowthDraft
import com.littlegrow.app.data.GrowthEntity
import com.littlegrow.app.data.GrowthMetric
import com.littlegrow.app.data.ReactionSeverity
import com.littlegrow.app.data.VaccineEntity
import com.littlegrow.app.data.VaccineCategory
import com.littlegrow.app.data.VaccineReactionDraft
import com.littlegrow.app.data.WhoGrowthStandards
import com.littlegrow.app.ui.NativeDatePickerField
import com.littlegrow.app.ui.components.EmptyRecordCard
import com.littlegrow.app.ui.components.GlassSurface
import com.littlegrow.app.ui.components.staggeredFadeSlideIn
import com.littlegrow.app.ui.components.ExpressiveFilterChip as FilterChip
import com.littlegrow.app.ui.components.ExpressiveOutlinedButton as OutlinedButton
import com.littlegrow.app.ui.components.ExpressiveTextButton as TextButton
import com.littlegrow.app.ui.dateFormatter
import com.littlegrow.app.ui.formatDate
import com.littlegrow.app.ui.formatMetric
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthScreen(
    profile: BabyProfile?,
    growthRecords: List<GrowthEntity>,
    vaccines: List<VaccineEntity>,
    refreshing: Boolean,
    contentPadding: PaddingValues,
    onRefresh: () -> Unit,
    onAddGrowth: (GrowthDraft) -> Unit,
    onUpdateGrowth: (Long, GrowthDraft) -> Unit,
    onDeleteGrowth: (Long) -> Unit,
    onToggleVaccineDone: (String, Boolean) -> Unit,
    onUpdateVaccineReaction: (String, VaccineReactionDraft) -> Unit,
) {
    var metric by rememberSaveable { mutableStateOf(GrowthMetric.WEIGHT) }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var showRecommendedVaccines by rememberSaveable { mutableStateOf(false) }
    var editingGrowth by remember { mutableStateOf<GrowthEntity?>(null) }
    var editingVaccine by remember { mutableStateOf<VaccineEntity?>(null) }

    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = onRefresh,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = Spacing.lg,
                    end = Spacing.lg,
                    top = contentPadding.calculateTopPadding() + Spacing.lg,
                    bottom = contentPadding.calculateBottomPadding() + 96.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
            // -- Monthly Growth Summary Card --
            item {
                Box(modifier = Modifier.staggeredFadeSlideIn(0)) {
                    MonthlyGrowthSummaryCard(
                        profile = profile,
                        growthRecords = growthRecords,
                    )
                }
            }

            // -- Height Trend Section --
            item {
                Box(modifier = Modifier.staggeredFadeSlideIn(1)) {
                    GrowthTrendSection(
                        profile = profile,
                        records = growthRecords,
                        metric = GrowthMetric.HEIGHT,
                    )
                }
            }

            // -- Weight Trend Section --
            item {
                Box(modifier = Modifier.staggeredFadeSlideIn(2)) {
                    GrowthTrendSection(
                        profile = profile,
                        records = growthRecords,
                        metric = GrowthMetric.WEIGHT,
                    )
                }
            }

            // -- Bottom Bento Cards --
            item {
                Box(modifier = Modifier.staggeredFadeSlideIn(3)) {
                    BentoCardsRow()
                }
            }

            // -- Metric filter chips (for head/BMI etc.) --
            item {
                Box(modifier = Modifier.staggeredFadeSlideIn(4)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
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
            }

            // -- Full chart card (existing logic, all metrics) --
            item {
                Box(modifier = Modifier.staggeredFadeSlideIn(5)) {
                    GrowthChartCard(
                        profile = profile,
                        records = growthRecords,
                        metric = metric,
                    )
                }
            }

            // -- Growth records list --
            if (growthRecords.isEmpty()) {
                item {
                    Box(modifier = Modifier.staggeredFadeSlideIn(6)) {
                        EmptyRecordCard("还没有生长记录。")
                    }
                }
            } else {
                itemsIndexed(growthRecords, key = { _, it -> it.id }) { index, growth ->
                    ElevatedCard(modifier = Modifier.staggeredFadeSlideIn(index + 6)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(growth.date.formatDate(), fontWeight = FontWeight.SemiBold)
                            Text(
                                "体重 ${growth.weightKg.formatMetric(GrowthMetric.WEIGHT)} · " +
                                    "身高 ${growth.heightCm.formatMetric(GrowthMetric.HEIGHT)} · " +
                                    "头围 ${growth.headCircCm.formatMetric(GrowthMetric.HEAD)}" +
                                    growth.bmiValue()?.let { " · BMI ${String.format("%.1f", it)}" }.orEmpty(),
                            )
                            growthVelocityText(profile, growthRecords, growth)?.let { velocityText ->
                                Text(
                                    velocityText,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
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

            // -- Vaccine sections --
            item {
                Box(modifier = Modifier.staggeredFadeSlideIn(7)) {
                    VaccineOverviewCard(hasVaccines = vaccines.isNotEmpty())
                }
            }

            if (vaccines.isNotEmpty()) {
                item {
                    Box(modifier = Modifier.staggeredFadeSlideIn(8)) {
                        VaccineSectionHeader(
                            showRecommendedVaccines = showRecommendedVaccines,
                            onToggleRecommended = { showRecommendedVaccines = !showRecommendedVaccines },
                        )
                    }
                }
                val nationalVaccines = vaccines.filter { it.category == VaccineCategory.NATIONAL }
                val recommendedVaccines = vaccines.filter { it.category == VaccineCategory.RECOMMENDED }
                itemsIndexed(nationalVaccines, key = { _, it -> it.scheduleKey }) { index, vaccine ->
                    Box(modifier = Modifier.staggeredFadeSlideIn(index + 9)) {
                        VaccineCard(
                            vaccine = vaccine,
                            onToggleDone = onToggleVaccineDone,
                            onEditReaction = { editingVaccine = it },
                        )
                    }
                }
                if (showRecommendedVaccines) {
                    itemsIndexed(recommendedVaccines, key = { _, it -> it.scheduleKey }) { index, vaccine ->
                        Box(modifier = Modifier.staggeredFadeSlideIn(index + 9 + nationalVaccines.size)) {
                            VaccineCard(
                                vaccine = vaccine,
                                onToggleDone = onToggleVaccineDone,
                                onEditReaction = { editingVaccine = it },
                            )
                        }
                    }
                }
            }
        }
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

    editingVaccine?.let { vaccine ->
        VaccineReactionDialog(
            vaccine = vaccine,
            onDismiss = { editingVaccine = null },
            onSubmit = { draft ->
                onUpdateVaccineReaction(vaccine.scheduleKey, draft)
                editingVaccine = null
            },
        )
    }
}

@Composable
private fun MonthlyGrowthSummaryCard(
    profile: BabyProfile?,
    growthRecords: List<GrowthEntity>,
) {
    val ageMonths = profile?.let {
        ChronoUnit.MONTHS.between(it.birthday, LocalDate.now()).toInt()
    }
    val sorted = growthRecords.sortedBy { it.date }
    val weightGain = if (sorted.size >= 2) {
        val latest = sorted.last().weightKg
        val previous = sorted[sorted.size - 2].weightKg
        if (latest != null && previous != null) String.format("%.2f", latest - previous) else "--"
    } else "--"
    val heightGain = if (sorted.size >= 2) {
        val latest = sorted.last().heightCm
        val previous = sorted[sorted.size - 2].heightCm
        if (latest != null && previous != null) String.format("%.1f", latest - previous) else "--"
    } else "--"

    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        alpha = 0.70f,
        shape = RoundedCornerShape(24.dp),
        accentColor = MaterialTheme.colorScheme.primary,
        shadowElevation = 18.dp,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Decorative blur circles
            Box(
                modifier = Modifier
                    .size(192.dp)
                    .offset(x = 200.dp, y = (-48).dp)
                    .blur(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.30f))
            )
            Box(
                modifier = Modifier
                    .size(192.dp)
                    .offset(x = (-48).dp, y = 100.dp)
                    .blur(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.20f))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column {
                        Text(
                            "本月成长摘要",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "茁壮成长中",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    if (ageMonths != null) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Text(
                                "${ageMonths}个月大",
                                modifier = Modifier.padding(horizontal = Spacing.lg, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    // Weight gain card
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(Spacing.lg),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.40f),
                    ) {
                        Column(
                            modifier = Modifier.padding(Spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MonitorWeight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(Spacing.lg2),
                                )
                                Text(
                                    "体重增长",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    weightGain,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    "kg",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 2.dp),
                                )
                            }
                        }
                    }

                    // Height gain card
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(Spacing.lg),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.40f),
                    ) {
                        Column(
                            modifier = Modifier.padding(Spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Straighten,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(Spacing.lg2),
                                )
                                Text(
                                    "身高增长",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    heightGain,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    "cm",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 2.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GrowthTrendSection(
    profile: BabyProfile?,
    records: List<GrowthEntity>,
    metric: GrowthMetric,
) {
    val isHeight = metric == GrowthMetric.HEIGHT
    val sectionTitle = if (isHeight) "身高趋势 (cm)" else "体重趋势 (kg)"
    val barColor = if (isHeight) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.primary
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp)
                        .background(
                            color = barColor,
                            shape = RoundedCornerShape(50),
                        )
                )
                Text(
                    sectionTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            if (isHeight) {
                // Legend for height chart
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(Spacing.sm)
                                .background(
                                    color = MaterialTheme.colorScheme.secondary,
                                    shape = CircleShape,
                                )
                        )
                        Text(
                            "测量值",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(Spacing.sm)
                                .background(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = CircleShape,
                                )
                        )
                        Text(
                            "基准",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                // WHO standard label for weight chart
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(Spacing.lg),
                    )
                    Text(
                        "符合WHO标准",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Chart card with surfaceContainerLowest background
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Spacing.lg2),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shadowElevation = 1.dp,
        ) {
            GrowthChartContent(
                profile = profile,
                records = records,
                metric = metric,
            )
        }
    }
}

@Composable
private fun GrowthChartContent(
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
    val predictionPoints = remember(measurementPoints, visibleMaxAgeDays) {
        buildPredictionPoints(measurementPoints, visibleMaxAgeDays)
    }

    var animationPlayed by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(metric) {
        animationPlayed = false
        kotlinx.coroutines.delay(50)
        animationPlayed = true
    }
    val animProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 1200, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "trendChartReveal"
    )

    val isHeight = metric == GrowthMetric.HEIGHT
    val lineColor = if (isHeight) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.primary
    }
    val refBandColor = if (isHeight) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val gridColor = MaterialTheme.colorScheme.surfaceContainer
    val accentColor = MaterialTheme.colorScheme.tertiary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.lg2),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        if (measurementPoints.isEmpty() && profile == null) {
            Text(
                "先填写宝宝生日和性别，再补一条生长记录。",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
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
            val yPadding = when (metric) {
                GrowthMetric.WEIGHT -> 0.5f
                GrowthMetric.HEIGHT, GrowthMetric.HEAD -> 1f
                GrowthMetric.BMI -> 0.6f
            }
            val minY = (yCandidates.minOrNull() ?: 0f) - yPadding
            val maxY = (yCandidates.maxOrNull() ?: 1f) + yPadding

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(192.dp),
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

                // Grid lines
                repeat(4) { index ->
                    val y = verticalPadding + chartHeight * index / 3f
                    drawLine(
                        color = gridColor,
                        start = Offset(horizontalPadding, y),
                        end = Offset(width - horizontalPadding, y),
                        strokeWidth = 1f,
                    )
                }

                // Reference band (draw wide translucent P50 line as band)
                if (visibleReferencePoints.isNotEmpty()) {
                    val refPath = Path().apply {
                        visibleReferencePoints.forEachIndexed { pointIndex, point ->
                            val offset = project(point.ageDays, point.p50)
                            if (pointIndex == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
                        }
                    }
                    drawPath(
                        path = refPath,
                        color = refBandColor.copy(alpha = 0.30f),
                        style = Stroke(width = 20f, cap = StrokeCap.Round),
                    )
                }

                // Main measurement curve
                if (measurementPoints.isNotEmpty()) {
                    val measurementPath = Path().apply {
                        measurementPoints.forEachIndexed { index, point ->
                            val offset = project(point.first, point.second)
                            if (index == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
                        }
                    }

                    val pathMeasure = androidx.compose.ui.graphics.PathMeasure()
                    pathMeasure.setPath(measurementPath, false)
                    val animatedPath = Path()
                    pathMeasure.getSegment(0f, pathMeasure.length * animProgress, animatedPath, true)

                    if (measurementPoints.size > 1) {
                        drawPath(
                            path = animatedPath,
                            color = lineColor,
                            style = Stroke(width = 4f, cap = StrokeCap.Round),
                        )
                    }

                    // Data points
                    val pointsToDraw = (measurementPoints.size * animProgress).toInt()
                    measurementPoints.take(pointsToDraw + 1).forEachIndexed { index, point ->
                        val isLast = index == measurementPoints.lastIndex
                        val radius = if (isLast) 6f else 4f
                        drawCircle(
                            color = lineColor,
                            radius = radius,
                            center = project(point.first, point.second),
                        )
                    }
                }

                // Prediction line
                if (predictionPoints.isNotEmpty()) {
                    val predictionPath = Path().apply {
                        predictionPoints.forEachIndexed { index, point ->
                            val offset = project(point.first, point.second)
                            if (index == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
                        }
                    }
                    drawPath(
                        path = predictionPath,
                        color = accentColor.copy(alpha = 0.7f * animProgress),
                        style = Stroke(
                            width = 3f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)),
                            cap = StrokeCap.Round,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun BentoCardsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        // Left card: Milestones
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(160.dp),
            shape = RoundedCornerShape(Spacing.lg),
            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.30f),
        ) {
            Column(
                modifier = Modifier.padding(Spacing.lg2),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoStories,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.weight(1f))
                Column {
                    Text(
                        "成长里程碑",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    Text(
                        "本月达成 3 个新技能",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.80f),
                    )
                }
            }
        }

        // Right card: Expert advice
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(160.dp),
            shape = RoundedCornerShape(Spacing.lg),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.30f),
        ) {
            Column(
                modifier = Modifier.padding(Spacing.lg2),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Icon(
                    imageVector = Icons.Rounded.NoteAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.weight(1f))
                Column {
                    Text(
                        "专家建议",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                        "当前阶段饮食重点",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.80f),
                    )
                }
            }
        }
    }
}

@Composable
private fun VaccineOverviewCard(hasVaccines: Boolean) {
    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
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
    onEditReaction: (VaccineEntity) -> Unit,
) {
    val overdueDays = ChronoUnit.DAYS.between(vaccine.scheduledDate, LocalDate.now())
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
            if (!vaccine.isDone && overdueDays >= 30) {
                Text(
                    "建议尽快补种",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (vaccine.category == VaccineCategory.RECOMMENDED) {
                Text(
                    "推荐自费疫苗",
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(
                "提醒会在建议日期前 3 天发出。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (vaccine.reactionNote != null || vaccine.hadFever || vaccine.reactionSeverity != null) {
                Text(
                    buildString {
                        append("接种反应")
                        vaccine.reactionSeverity?.let { append(" · ${it.label}") }
                        if (vaccine.hadFever) append(" · 发烧")
                        vaccine.reactionNote?.takeIf { it.isNotBlank() }?.let { append(" · $it") }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedButton(onClick = { onToggleDone(vaccine.scheduleKey, !vaccine.isDone) }) {
                Text(if (vaccine.isDone) "撤销已接种" else "标记已接种")
            }
            TextButton(onClick = { onEditReaction(vaccine) }) {
                Text("记录接种反应")
            }
        }
    }
}

@Composable
private fun VaccineSectionHeader(
    showRecommendedVaccines: Boolean,
    onToggleRecommended: () -> Unit,
) {
    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Text("推荐自费疫苗", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                "常见如轮状病毒、13 价肺炎、流感、水痘、EV71 等，默认折叠避免干扰主流程。",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(if (showRecommendedVaccines) "已展开" else "默认折叠")
                Switch(checked = showRecommendedVaccines, onCheckedChange = { onToggleRecommended() })
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
    val predictionPoints = remember(measurementPoints, visibleMaxAgeDays) {
        buildPredictionPoints(measurementPoints, visibleMaxAgeDays)
    }
    var chartSize by remember(metric) { mutableStateOf(IntSize.Zero) }
    var selectedPointIndex by remember(metric) { mutableStateOf<Int?>(null) }
    val density = LocalDensity.current

    var animationPlayed by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(metric) {
        animationPlayed = false
        kotlinx.coroutines.delay(50)
        animationPlayed = true
    }
    val animProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 1200, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "chartReveal"
    )

    ElevatedCard {
        val primaryColor = MaterialTheme.colorScheme.primary
        val accentColor = MaterialTheme.colorScheme.tertiary
        val axisLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.86f)
        val axisTitleColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
        val referenceBandStart = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
        val referenceBandEnd = MaterialTheme.colorScheme.secondary.copy(alpha = 0.04f)
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
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
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
                    GrowthMetric.BMI -> 0.6f
                }
                val maxY = (yCandidates.maxOrNull() ?: 1f) + when (metric) {
                    GrowthMetric.WEIGHT -> 0.5f
                    GrowthMetric.HEIGHT, GrowthMetric.HEAD -> 1f
                    GrowthMetric.BMI -> 0.6f
                }
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 10f),
                ) {
                    val tooltipPoint = selectedPointIndex?.let { index ->
                        measurementPoints.getOrNull(index)?.let { point ->
                            projectGrowthChartPoint(
                                ageDays = point.first,
                                value = point.second,
                                chartSize = chartSize,
                                visibleMaxAgeDays = visibleMaxAgeDays,
                                minY = minY,
                                maxY = maxY,
                            )
                        }
                    }

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .onSizeChanged { chartSize = it }
                            .pointerInput(measurementPoints, chartSize, minY, maxY, visibleMaxAgeDays) {
                                detectTapGestures { tapOffset ->
                                    if (measurementPoints.isEmpty() || chartSize == IntSize.Zero) {
                                        selectedPointIndex = null
                                        return@detectTapGestures
                                    }
                                    val nearest = measurementPoints.mapIndexed { index, point ->
                                        val projected = projectGrowthChartPoint(
                                            ageDays = point.first,
                                            value = point.second,
                                            chartSize = chartSize,
                                            visibleMaxAgeDays = visibleMaxAgeDays,
                                            minY = minY,
                                            maxY = maxY,
                                        )
                                        index to hypot(
                                            (projected.x - tapOffset.x).toDouble(),
                                            (projected.y - tapOffset.y).toDouble(),
                                        ).toFloat()
                                    }.minByOrNull { it.second }
                                    selectedPointIndex = nearest?.takeIf { it.second <= 72f * density.density }?.first
                                }
                            },
                    ) {
                        val width = size.width
                        val height = size.height
                        val horizontalPadding = 52f
                        val rightPadding = 42f
                        val verticalPadding = 28f
                        val ySpread = (maxY - minY).takeIf { it > 0f } ?: 1f
                        val chartWidth = width - horizontalPadding - rightPadding
                        val chartHeight = height - verticalPadding * 2

                        fun project(ageDays: Int, value: Float): Offset {
                            val xProgress = ageDays.toFloat() / visibleMaxAgeDays.coerceAtLeast(1)
                            val yProgress = (value - minY) / ySpread
                            return Offset(
                                x = horizontalPadding + chartWidth * xProgress,
                                y = height - verticalPadding - chartHeight * yProgress,
                            )
                        }

                        val labelPaint = android.graphics.Paint().apply {
                            isAntiAlias = true
                            textSize = 11.sp.toPx()
                            color = axisLabelColor.toArgb()
                        }
                        val axisPaint = android.graphics.Paint(labelPaint).apply {
                            textSize = 12.sp.toPx()
                            color = axisTitleColor.toArgb()
                            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                        }

                        val yAxisValues = List(4) { index ->
                            maxY - (ySpread * index / 3f)
                        }
                        yAxisValues.forEachIndexed { index, value ->
                            val y = verticalPadding + chartHeight * index / 3f
                            drawLine(
                                color = gridColor,
                                start = Offset(horizontalPadding, y),
                                end = Offset(width - rightPadding, y),
                                strokeWidth = 2f,
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                if (metric == GrowthMetric.HEIGHT || metric == GrowthMetric.HEAD) "${value.roundToInt()}" else String.format("%.1f", value),
                                8f,
                                y + 4.dp.toPx(),
                                labelPaint,
                            )
                        }

                        val monthMarks = listOf(0, 3, 6, 9, 12, 18, 24).filter {
                            it * 30 <= visibleMaxAgeDays
                        }.ifEmpty { listOf(0) }
                        monthMarks.forEach { month ->
                            val x = horizontalPadding + chartWidth * ((month * 30f) / visibleMaxAgeDays.coerceAtLeast(1))
                            drawLine(
                                color = gridColor.copy(alpha = 0.55f),
                                start = Offset(x, verticalPadding),
                                end = Offset(x, height - verticalPadding),
                                strokeWidth = 1.5f,
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                "${month}月",
                                x - 12.dp.toPx(),
                                height - 6.dp.toPx(),
                                labelPaint,
                            )
                        }
                        drawContext.canvas.nativeCanvas.drawText(
                            when (metric) {
                                GrowthMetric.WEIGHT -> "kg"
                                GrowthMetric.HEIGHT -> "cm"
                                GrowthMetric.HEAD -> "cm"
                                GrowthMetric.BMI -> "BMI"
                            },
                            8f,
                            16.dp.toPx(),
                            axisPaint,
                        )

                        if (visibleReferencePoints.isNotEmpty()) {
                            val bandPath = Path().apply {
                                visibleReferencePoints.forEachIndexed { index, point ->
                                    val offset = project(point.ageDays, point.p3)
                                    if (index == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
                                }
                                visibleReferencePoints.asReversed().forEach { point ->
                                    val offset = project(point.ageDays, point.p97)
                                    lineTo(offset.x, offset.y)
                                }
                                close()
                            }
                            drawPath(
                                path = bandPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        referenceBandStart,
                                        referenceBandEnd,
                                    ),
                                    startY = verticalPadding,
                                    endY = height - verticalPadding,
                                ),
                            )

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

                            listOf(3, 50, 97).forEach { percentile ->
                                val point = visibleReferencePoints.lastOrNull() ?: return@forEach
                                val offset = project(point.ageDays, point.valueFor(percentile))
                                drawContext.canvas.nativeCanvas.drawText(
                                    "P$percentile",
                                    offset.x + 8.dp.toPx(),
                                    offset.y + 4.dp.toPx(),
                                    axisPaint,
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

                            val pathMeasure = androidx.compose.ui.graphics.PathMeasure()
                            pathMeasure.setPath(measurementPath, false)
                            val animatedPath = Path()
                            pathMeasure.getSegment(0f, pathMeasure.length * animProgress, animatedPath, true)

                            val fillPath = Path().apply {
                                addPath(animatedPath)
                                if (measurementPoints.isNotEmpty() && animProgress > 0f) {
                                    val firstOffset = project(measurementPoints.first().first, measurementPoints.first().second)
                                    val lastIndex = (measurementPoints.size * animProgress).toInt().coerceAtMost(measurementPoints.size - 1)
                                    val lastOffset = project(measurementPoints[lastIndex].first, measurementPoints[lastIndex].second)
                                    lineTo(lastOffset.x, height - verticalPadding)
                                    lineTo(firstOffset.x, height - verticalPadding)
                                    close()
                                }
                            }

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(primaryColor.copy(alpha = 0.4f), Color.Transparent),
                                    startY = verticalPadding,
                                    endY = height - verticalPadding,
                                ),
                            )

                            if (measurementPoints.size > 1) {
                                drawPath(
                                    path = animatedPath,
                                    color = primaryColor,
                                    style = Stroke(width = 8f, cap = StrokeCap.Round),
                                )
                            }

                            val pointsToDraw = (measurementPoints.size * animProgress).toInt()
                            measurementPoints.take(pointsToDraw + 1).forEachIndexed { index, point ->
                                val center = project(point.first, point.second)
                                val isSelected = selectedPointIndex == index
                                if (isSelected) {
                                    drawCircle(
                                        color = accentColor.copy(alpha = 0.22f),
                                        radius = 18f,
                                        center = center,
                                    )
                                }
                                drawCircle(
                                    color = accentColor,
                                    radius = if (isSelected) 11f else 9f,
                                    center = center,
                                )
                            }
                        }
                        if (predictionPoints.isNotEmpty()) {
                            val predictionPath = Path().apply {
                                predictionPoints.forEachIndexed { index, point ->
                                    val offset = project(point.first, point.second)
                                    if (index == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
                                }
                            }
                            drawPath(
                                path = predictionPath,
                                color = accentColor.copy(alpha = 0.7f * animProgress),
                                style = Stroke(
                                    width = 4f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)),
                                    cap = StrokeCap.Round,
                                ),
                            )
                        }
                    }

                    selectedPointIndex?.let { index ->
                        measurementPoints.getOrNull(index)?.let { point ->
                            tooltipPoint?.let { offset ->
                                Surface(
                                    modifier = Modifier.offset {
                                        IntOffset(
                                            x = (offset.x - 54.dp.toPx()).roundToInt().coerceAtLeast(0),
                                            y = (offset.y - 72.dp.toPx()).roundToInt().coerceAtLeast(0),
                                        )
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                                    tonalElevation = 6.dp,
                                    shadowElevation = 8.dp,
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp),
                                    ) {
                                        val record = records.find { growth ->
                                            ChronoUnit.DAYS.between(profile?.birthday ?: growth.date, growth.date).toInt() == point.first
                                        }
                                        Text(
                                            text = record?.date?.formatDate() ?: "第 ${point.first / 30} 月",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        Text(
                                            text = "${metric.label} ${point.second.formatMetric(metric)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                        reference?.percentileBand(point.first, point.second)?.let { band ->
                                            Text(
                                                text = "WHO $band",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                }
                            }
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
                } else if (metric == GrowthMetric.BMI) {
                    Text("BMI 参考线按现有 WHO 体重/身高百分位近似换算，用于辅助观察趋势。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("参考线来源为 WHO Child Growth Standards 0-5 岁百分位表。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (predictionPoints.isNotEmpty()) {
                    Text("虚线为基于现有数据点的线性趋势预测，仅作观察参考。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun VaccineReactionDialog(
    vaccine: VaccineEntity,
    onDismiss: () -> Unit,
    onSubmit: (VaccineReactionDraft) -> Unit,
) {
    var reactionNote by rememberSaveable(vaccine.scheduleKey) { mutableStateOf(vaccine.reactionNote.orEmpty()) }
    var hadFever by rememberSaveable(vaccine.scheduleKey) { mutableStateOf(vaccine.hadFever) }
    var reactionSeverity by rememberSaveable(vaccine.scheduleKey) { mutableStateOf(vaccine.reactionSeverity) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${vaccine.vaccineName} 接种反应") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("是否发烧")
                    Switch(checked = hadFever, onCheckedChange = { hadFever = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    ReactionSeverity.entries.forEach { severity ->
                        FilterChip(
                            selected = reactionSeverity == severity,
                            onClick = { reactionSeverity = if (reactionSeverity == severity) null else severity },
                            label = { Text(severity.label) },
                        )
                    }
                }
                OutlinedTextField(
                    value = reactionNote,
                    onValueChange = { reactionNote = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("反应描述") },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSubmit(
                        VaccineReactionDraft(
                            reactionNote = reactionNote,
                            hadFever = hadFever,
                            reactionSeverity = reactionSeverity,
                        ),
                    )
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

@Composable
fun AddGrowthDialog(
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
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
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
                GrowthMetric.BMI -> record.bmiValue()
            }
            if (ageDays < 0 || value == null) null else ageDays to value
        }
}

private fun projectGrowthChartPoint(
    ageDays: Int,
    value: Float,
    chartSize: IntSize,
    visibleMaxAgeDays: Int,
    minY: Float,
    maxY: Float,
): Offset {
    val width = chartSize.width.toFloat()
    val height = chartSize.height.toFloat()
    val horizontalPadding = 52f
    val rightPadding = 42f
    val verticalPadding = 28f
    val ySpread = (maxY - minY).takeIf { it > 0f } ?: 1f
    val chartWidth = width - horizontalPadding - rightPadding
    val chartHeight = height - verticalPadding * 2
    val xProgress = ageDays.toFloat() / visibleMaxAgeDays.coerceAtLeast(1)
    val yProgress = (value - minY) / ySpread
    return Offset(
        x = horizontalPadding + chartWidth * xProgress,
        y = height - verticalPadding - chartHeight * yProgress,
    )
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

private fun GrowthEntity.bmiValue(): Float? {
    val weight = weightKg ?: return null
    val height = heightCm ?: return null
    val meter = height / 100f
    return if (meter <= 0f) null else weight / meter.pow(2)
}

private fun buildPredictionPoints(
    points: List<Pair<Int, Float>>,
    visibleMaxAgeDays: Int,
): List<Pair<Int, Float>> {
    if (points.size < 3) return emptyList()
    val xs = points.map { it.first.toDouble() }
    val ys = points.map { it.second.toDouble() }
    val meanX = xs.average()
    val meanY = ys.average()
    val variance = xs.sumOf { (it - meanX) * (it - meanX) }
    if (variance == 0.0) return emptyList()
    val slope = xs.indices.sumOf { index -> (xs[index] - meanX) * (ys[index] - meanY) } / variance
    val intercept = meanY - slope * meanX
    val startX = points.first().first
    val endX = maxOf(points.last().first + 60, visibleMaxAgeDays)
    return listOf(startX, endX).map { ageDays ->
        ageDays to (intercept + slope * ageDays).toFloat()
    }
}

private fun growthVelocityText(
    profile: BabyProfile?,
    records: List<GrowthEntity>,
    current: GrowthEntity,
): String? {
    val sorted = records.sortedBy { it.date }
    val index = sorted.indexOfFirst { it.id == current.id }
    if (index <= 0) return null
    val previous = sorted[index - 1]
    val monthsBetween = (ChronoUnit.DAYS.between(previous.date, current.date).toFloat() / 30f).takeIf { it > 0f } ?: return null
    val ageMonths = profile?.let { ChronoUnit.MONTHS.between(it.birthday, current.date).toInt() } ?: 0
    val weightText = current.weightKg?.let { currentWeight ->
        previous.weightKg?.let { previousWeight ->
            val deltaPerMonth = ((currentWeight - previousWeight) * 1000f) / monthsBetween
            WhoGrowthStandards.monthlyVelocityRange(GrowthMetric.WEIGHT, ageMonths)?.let { range ->
                "体重月增约 ${deltaPerMonth.toInt()}g（正常范围 ${range.min.toInt()}-${range.max.toInt()}${range.unit.removePrefix("g")})"
            }
        }
    }
    val heightText = current.heightCm?.let { currentHeight ->
        previous.heightCm?.let { previousHeight ->
            val deltaPerMonth = (currentHeight - previousHeight) / monthsBetween
            WhoGrowthStandards.monthlyVelocityRange(GrowthMetric.HEIGHT, ageMonths)?.let { range ->
                "身高月增约 ${String.format("%.1f", deltaPerMonth)}cm（正常范围 ${String.format("%.1f", range.min)}-${String.format("%.1f", range.max)}${range.unit.removePrefix("cm")})"
            }
        }
    }
    return listOfNotNull(weightText, heightText).firstOrNull()
}

