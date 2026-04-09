package com.littlegrow.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.littlegrow.app.AppDestination
import com.littlegrow.app.AppLaunchTarget
import com.littlegrow.app.RecordQuickAction
import com.littlegrow.app.buildAppLaunchIntent
import com.littlegrow.app.data.AppDatabase
import com.littlegrow.app.data.RecordTab
import com.littlegrow.app.data.toProfile
import java.time.LocalDate
import java.time.Period
import kotlinx.coroutines.flow.first

private val littleGrowWidget = LittleGrowGlanceWidget()

object LittleGrowWidgetUpdater {
    suspend fun updateAll(context: Context) {
        littleGrowWidget.updateAll(context)
    }
}

class LittleGrowWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = littleGrowWidget
}

private class LittleGrowGlanceWidget : GlanceAppWidget() {
    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val database = AppDatabase.getInstance(context)
        val profile = database.babyDao().observeProfile().first()?.toProfile()
        val feedings = database.feedingDao().observeAll().first()
        val diapers = database.diaperDao().observeAll().first()
        val today = LocalDate.now()
        val snapshot = WidgetSnapshot(
            title = profile?.name ?: "长呀长",
            ageText = profile?.birthday?.let(::formatWidgetAge) ?: "先填写宝宝资料",
            todayFeedings = feedings.count { it.happenedAt.toLocalDate() == today },
            todayDiapers = diapers.count { it.happenedAt.toLocalDate() == today },
            latestFeeding = feedings.firstOrNull()?.let {
                "${it.type.label} · ${it.happenedAt.hour.toString().padStart(2, '0')}:${it.happenedAt.minute.toString().padStart(2, '0')}"
            } ?: "今天还没有喂养记录",
        )

        provideContent {
            WidgetContent(snapshot)
        }
    }
}

private data class WidgetSnapshot(
    val title: String,
    val ageText: String,
    val todayFeedings: Int,
    val todayDiapers: Int,
    val latestFeeding: String,
)

@Composable
private fun WidgetContent(snapshot: WidgetSnapshot) {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(day = Color(0xFFFFF7ED), night = Color(0xFF1F2937)))
            .padding(16.dp),
        verticalAlignment = Alignment.Vertical.Top,
        horizontalAlignment = Alignment.Horizontal.Start,
    ) {
        Text(
            text = snapshot.title,
            style = TextStyle(
                color = ColorProvider(day = Color(0xFF7C2D12), night = Color.White),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = snapshot.ageText,
            style = TextStyle(
                color = ColorProvider(day = Color(0xFF9A3412), night = Color(0xFFFDE68A)),
                fontSize = 13.sp,
            ),
        )
        Spacer(modifier = GlanceModifier.height(12.dp))
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(ColorProvider(day = Color.White, night = Color(0xFF374151)))
                .padding(12.dp),
        ) {
            Text(
                text = "今日喂养 ${snapshot.todayFeedings} 次 · 今日尿布 ${snapshot.todayDiapers} 次",
                style = TextStyle(
                    color = ColorProvider(day = Color(0xFF7C2D12), night = Color.White),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
        Spacer(modifier = GlanceModifier.height(10.dp))
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(ColorProvider(day = Color(0xFFFFEDD5), night = Color(0xFF374151)))
                .padding(12.dp),
        ) {
            Text(
                text = "最近一次：${snapshot.latestFeeding}",
                style = TextStyle(
                    color = ColorProvider(day = Color(0xFF7C2D12), night = Color(0xFFF9FAFB)),
                    fontSize = 13.sp,
                ),
            )
        }
        Spacer(modifier = GlanceModifier.height(12.dp))
        QuickActionChip(
            label = "母乳计时",
            intent = buildAppLaunchIntent(
                context,
                AppLaunchTarget(
                    destination = AppDestination.RECORDS,
                    recordTab = RecordTab.FEEDING,
                    quickAction = RecordQuickAction.TIMER,
                ),
            ),
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        QuickActionChip(
            label = "记睡眠",
            intent = buildAppLaunchIntent(
                context,
                AppLaunchTarget(
                    destination = AppDestination.RECORDS,
                    recordTab = RecordTab.SLEEP,
                    quickAction = RecordQuickAction.ADD,
                ),
            ),
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        QuickActionChip(
            label = "记尿布",
            intent = buildAppLaunchIntent(
                context,
                AppLaunchTarget(
                    destination = AppDestination.RECORDS,
                    recordTab = RecordTab.DIAPER,
                    quickAction = RecordQuickAction.ADD,
                ),
            ),
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        QuickActionChip(
            label = "健康记录",
            intent = buildAppLaunchIntent(
                context,
                AppLaunchTarget(
                    destination = AppDestination.RECORDS,
                    recordTab = RecordTab.MEDICAL,
                    quickAction = RecordQuickAction.ADD,
                ),
            ),
        )
    }
}

@Composable
private fun QuickActionChip(
    label: String,
    intent: android.content.Intent,
) {
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(ColorProvider(day = Color(0xFFFDBA74), night = Color(0xFF4B5563)))
            .clickable(actionStartActivity(intent))
            .padding(vertical = 10.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = ColorProvider(day = Color(0xFF7C2D12), night = Color(0xFFFDE68A)),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

private fun formatWidgetAge(birthday: LocalDate): String {
    val today = LocalDate.now()
    val days = java.time.temporal.ChronoUnit.DAYS.between(birthday, today)
    return if (days < 31) {
        "出生第 ${days + 1} 天"
    } else {
        val period = Period.between(birthday, today)
        "${period.toTotalMonths()} 个月 ${period.days} 天"
    }
}
