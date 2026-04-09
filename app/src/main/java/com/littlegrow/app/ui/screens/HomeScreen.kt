package com.littlegrow.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.littlegrow.app.data.DiaperEntity
import com.littlegrow.app.data.FeedingEntity
import com.littlegrow.app.data.GrowthMetric
import com.littlegrow.app.data.HomeSummary
import com.littlegrow.app.data.RecordTab
import com.littlegrow.app.data.SleepEntity
import com.littlegrow.app.ui.formatDate
import com.littlegrow.app.ui.formatDateTime
import com.littlegrow.app.ui.formatMetric
import com.littlegrow.app.ui.formatMinutes
import java.time.Duration

@Composable
fun HomeScreen(
    summary: HomeSummary,
    contentPadding: PaddingValues,
    onOpenRecords: (RecordTab) -> Unit,
    onOpenGrowth: () -> Unit,
    onOpenTimeline: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = summary.profile?.name ?: "欢迎来到长呀长",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = summary.ageText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "离线记录照护节奏，把每天的成长留在本地。",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "今日喂养",
                    value = "${summary.todayFeedings} 次",
                    subtitle = "最近三条记录一眼看完",
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "今日排泄",
                    value = "${summary.todayDiapers} 次",
                    subtitle = "异常颜色会高亮提醒",
                )
            }
        }

        item {
            SummaryCard(
                title = "今日睡眠",
                value = summary.todaySleepMinutes.formatMinutes(),
                subtitle = summary.latestGrowth?.let {
                    "最新生长：${it.date.formatDate()} 体重 ${it.weightKg.formatMetric(GrowthMetric.WEIGHT)}"
                } ?: "还没有生长记录，建议补一条体重或身高。",
            )
        }

        item {
            SectionTitle("快捷入口")
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                QuickActionChip("记喂奶", Modifier.weight(1f)) { onOpenRecords(RecordTab.FEEDING) }
                QuickActionChip("记睡眠", Modifier.weight(1f)) { onOpenRecords(RecordTab.SLEEP) }
                QuickActionChip("记尿布", Modifier.weight(1f)) { onOpenRecords(RecordTab.DIAPER) }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                QuickActionChip("健康记录", Modifier.weight(1f)) { onOpenRecords(RecordTab.MEDICAL) }
                QuickActionChip("活动记录", Modifier.weight(1f)) { onOpenRecords(RecordTab.ACTIVITY) }
                QuickActionChip("成长曲线", Modifier.weight(1f), onOpenGrowth)
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                QuickActionChip("里程碑", Modifier.weight(1f), onOpenTimeline)
                QuickActionChip("宝宝资料", Modifier.weight(1f), onOpenSettings)
            }
        }

        item {
            SectionTitle("最近喂养")
        }
        if (summary.recentFeedings.isEmpty()) {
            item {
                EmptyHint("还没有喂养记录。")
            }
        } else {
            items(summary.recentFeedings, key = { it.id }) { feeding ->
                HomeFeedingCard(feeding = feeding)
            }
        }

        item {
            SectionTitle("最近睡眠")
        }
        if (summary.recentSleeps.isEmpty()) {
            item {
                EmptyHint("还没有睡眠记录。")
            }
        } else {
            items(summary.recentSleeps, key = { it.id }) { sleep ->
                HomeSleepCard(sleep = sleep)
            }
        }

        item {
            SectionTitle("最近排泄")
        }
        if (summary.recentDiapers.isEmpty()) {
            item {
                EmptyHint("还没有排泄记录。")
            }
        } else {
            items(summary.recentDiapers, key = { it.id }) { diaper ->
                HomeDiaperCard(diaper = diaper)
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun QuickActionChip(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    AssistChip(
        modifier = modifier,
        onClick = onClick,
        label = {
            Text(label)
        },
    )
}

@Composable
private fun HomeFeedingCard(feeding: FeedingEntity) {
    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(feeding.type.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(feeding.happenedAt.formatDateTime(), style = MaterialTheme.typography.bodyMedium)
            val details = buildList {
                feeding.durationMinutes?.let { add("${it} 分钟") }
                feeding.amountMl?.let { add("${it} ml") }
                feeding.foodName?.let { add(it) }
                feeding.note?.let { add(it) }
            }
            if (details.isNotEmpty()) {
                Text(details.joinToString(" · "), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HomeSleepCard(sleep: SleepEntity) {
    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("睡眠", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text("${sleep.startTime.formatDateTime()} - ${sleep.endTime.formatDateTime()}")
            Text(
                Duration.between(sleep.startTime, sleep.endTime).formatMinutesCompat(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            sleep.note?.let {
                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HomeDiaperCard(diaper: DiaperEntity) {
    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(diaper.type.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(diaper.happenedAt.formatDateTime())
            val extra = buildList {
                diaper.poopColor?.let { add(it.label) }
                diaper.poopTexture?.let { add(it.label) }
                diaper.note?.let { add(it) }
            }
            if (extra.isNotEmpty()) {
                Text(extra.joinToString(" · "), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (diaper.poopColor == com.littlegrow.app.data.PoopColor.RED ||
                diaper.poopColor == com.littlegrow.app.data.PoopColor.WHITE
            ) {
                Text(
                    text = "注意：该颜色建议尽快留意宝宝状态。",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    ElevatedCard {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun Duration.formatMinutesCompat(): String = toMinutes().formatMinutes()
