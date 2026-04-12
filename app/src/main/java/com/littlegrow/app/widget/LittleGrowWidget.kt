package com.littlegrow.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProvider as dayNightColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.littlegrow.app.AppDestination
import com.littlegrow.app.AppLaunchTarget
import com.littlegrow.app.RecordQuickAction
import com.littlegrow.app.buildAppLaunchIntent
import com.littlegrow.app.data.AppDatabase
import com.littlegrow.app.data.AppTheme
import com.littlegrow.app.data.DiaperEntity
import com.littlegrow.app.data.DiaperType
import com.littlegrow.app.data.PreferencesRepository
import com.littlegrow.app.data.RecordTab
import com.littlegrow.app.data.toProfile
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
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

class QuickPeeAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: androidx.glance.action.ActionParameters,
    ) {
        AppDatabase.getInstance(context).diaperDao().upsert(
            DiaperEntity(
                happenedAt = LocalDateTime.now(),
                type = DiaperType.PEE,
                poopColor = null,
                poopTexture = null,
                note = null,
            ),
        )
        littleGrowWidget.updateAll(context)
    }
}

class QuickPoopAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: androidx.glance.action.ActionParameters,
    ) {
        AppDatabase.getInstance(context).diaperDao().upsert(
            DiaperEntity(
                happenedAt = LocalDateTime.now(),
                type = DiaperType.POOP,
                poopColor = null,
                poopTexture = null,
                note = null,
            ),
        )
        littleGrowWidget.updateAll(context)
    }
}

private class LittleGrowGlanceWidget : GlanceAppWidget() {
    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val database = AppDatabase.getInstance(context)
        val preferences = PreferencesRepository(context)
        val profile = database.babyDao().observeProfile().first()?.toProfile()
        val feedings = database.feedingDao().observeAll().first()
        val sleeps = database.sleepDao().observeAll().first()
        val diapers = database.diaperDao().observeAll().first()
        val vaccines = database.vaccineDao().observeAll().first()
        val today = LocalDate.now()
        val latestFeeding = feedings.firstOrNull()
        val nextVaccine = vaccines
            .filter { !it.isDone && !it.scheduledDate.isBefore(today) }
            .minByOrNull { it.scheduledDate }
        val todaySleepHours = sleeps
            .filter { it.startTime.toLocalDate() == today || it.endTime.toLocalDate() == today }
            .sumOf { Duration.between(it.startTime, it.endTime).toMinutes().coerceAtLeast(0) } / 60f

        val snapshot = WidgetSnapshot(
            title = profile?.name ?: "长呀长",
            ageText = profile?.birthday?.let(::formatWidgetAge) ?: "先填写宝宝资料",
            todayFeedings = feedings.count { it.happenedAt.toLocalDate() == today },
            todaySleepHours = todaySleepHours,
            todayDiapers = diapers.count { it.happenedAt.toLocalDate() == today },
            nextVaccine = nextVaccine?.let {
                "${it.vaccineName}${it.doseNumber} 剂 · ${formatDateShort(it.scheduledDate)}"
            } ?: "暂无近期疫苗提醒",
            lastFeedingElapsed = latestFeeding?.let { feeding ->
                formatElapsed(Duration.between(feeding.happenedAt, LocalDateTime.now()))
            } ?: "今天还没有喂养记录",
            themeColors = widgetThemeColors(preferences.appTheme.first()),
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
    val todaySleepHours: Float,
    val todayDiapers: Int,
    val nextVaccine: String,
    val lastFeedingElapsed: String,
    val themeColors: WidgetThemeColors,
)

private data class WidgetThemeColors(
    val background: ColorProvider,
    val surface: ColorProvider,
    val surfaceAlt: ColorProvider,
    val accent: ColorProvider,
    val accentStrong: ColorProvider,
    val textPrimary: ColorProvider,
    val textSecondary: ColorProvider,
)

@Composable
private fun WidgetContent(snapshot: WidgetSnapshot) {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(snapshot.themeColors.background)
            .padding(14.dp),
        verticalAlignment = Alignment.Vertical.Top,
        horizontalAlignment = Alignment.Horizontal.Start,
    ) {
        Text(
            text = snapshot.title,
            style = TextStyle(
                color = snapshot.themeColors.textPrimary,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(modifier = GlanceModifier.height(2.dp))
        Text(
            text = snapshot.ageText,
            style = TextStyle(
                color = snapshot.themeColors.textSecondary,
                fontSize = 12.sp,
            ),
        )
        Spacer(modifier = GlanceModifier.height(10.dp))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        ) {
            WidgetMetricCard(
                modifier = GlanceModifier.defaultWeight(),
                title = "今日喂养",
                value = "${snapshot.todayFeedings} 次",
                colors = snapshot.themeColors,
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            WidgetMetricCard(
                modifier = GlanceModifier.defaultWeight(),
                title = "睡眠时长",
                value = "${snapshot.todaySleepHours.formatOneDecimal()} 小时",
                colors = snapshot.themeColors,
            )
        }
        Spacer(modifier = GlanceModifier.height(8.dp))
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        ) {
            WidgetMetricCard(
                modifier = GlanceModifier.defaultWeight(),
                title = "换尿布",
                value = "${snapshot.todayDiapers} 次",
                colors = snapshot.themeColors,
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            WidgetMetricCard(
                modifier = GlanceModifier.defaultWeight(),
                title = "下次疫苗",
                value = snapshot.nextVaccine,
                colors = snapshot.themeColors,
                emphasize = true,
            )
        }

        Spacer(modifier = GlanceModifier.height(10.dp))
        WidgetInfoStrip(
            title = "距上次喂养",
            value = snapshot.lastFeedingElapsed,
            colors = snapshot.themeColors,
        )
        Spacer(modifier = GlanceModifier.height(10.dp))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        ) {
            WidgetActionButton(
                label = "喂养",
                modifier = GlanceModifier.defaultWeight(),
                colors = snapshot.themeColors,
                onClick = actionStartActivity(
                    buildAppLaunchIntent(
                        context,
                        AppLaunchTarget(
                            destination = AppDestination.RECORDS,
                            recordTab = RecordTab.FEEDING,
                            quickAction = RecordQuickAction.ADD,
                        ),
                    ),
                ),
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            WidgetActionButton(
                label = "尿布",
                modifier = GlanceModifier.defaultWeight(),
                colors = snapshot.themeColors,
                onClick = actionRunCallback<QuickPeeAction>(),
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            WidgetActionButton(
                label = "睡眠",
                modifier = GlanceModifier.defaultWeight(),
                colors = snapshot.themeColors,
                onClick = actionStartActivity(
                    buildAppLaunchIntent(
                        context,
                        AppLaunchTarget(
                            destination = AppDestination.RECORDS,
                            recordTab = RecordTab.SLEEP,
                            quickAction = RecordQuickAction.ADD,
                        ),
                    ),
                ),
            )
        }
    }
}

@Composable
private fun WidgetMetricCard(
    title: String,
    value: String,
    colors: WidgetThemeColors,
    modifier: GlanceModifier = GlanceModifier,
    emphasize: Boolean = false,
) {
    Column(
        modifier = modifier
            .background(if (emphasize) colors.surfaceAlt else colors.surface)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Vertical.Top,
    ) {
        Text(
            text = title,
            style = TextStyle(
                color = colors.textSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(modifier = GlanceModifier.height(6.dp))
        Text(
            text = value,
            style = TextStyle(
                color = colors.textPrimary,
                fontSize = if (emphasize) 12.sp else 14.sp,
                fontWeight = FontWeight.Bold,
            ),
            maxLines = 2,
        )
    }
}

@Composable
private fun WidgetInfoStrip(
    title: String,
    value: String,
    colors: WidgetThemeColors,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            text = title,
            style = TextStyle(
                color = colors.textSecondary,
                fontSize = 11.sp,
            ),
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = value,
            style = TextStyle(
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
private fun WidgetActionButton(
    label: String,
    colors: WidgetThemeColors,
    onClick: Action,
    modifier: GlanceModifier = GlanceModifier,
) {
    Box(
        modifier = modifier
            .background(colors.accent)
            .clickable(onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = colors.accentStrong,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

private fun widgetThemeColors(theme: AppTheme): WidgetThemeColors = when (theme) {
    AppTheme.EARTHY -> WidgetThemeColors(
        background = dayNightColorProvider(day = Color(0xFFFAF4EA), night = Color(0xFF181410)),
        surface = dayNightColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF2A231D)),
        surfaceAlt = dayNightColorProvider(day = Color(0xFFFFEDD8), night = Color(0xFF3C2F20)),
        accent = dayNightColorProvider(day = Color(0xFFFFC566), night = Color(0xFF6D5630)),
        accentStrong = dayNightColorProvider(day = Color(0xFF6D4700), night = Color(0xFFFDE7BE)),
        textPrimary = dayNightColorProvider(day = Color(0xFF49341B), night = Color(0xFFF4E5D2)),
        textSecondary = dayNightColorProvider(day = Color(0xFF7C6140), night = Color(0xFFD6C0A2)),
    )
    AppTheme.PEACH -> WidgetThemeColors(
        background = dayNightColorProvider(day = Color(0xFFFFF4EE), night = Color(0xFF251917)),
        surface = dayNightColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF3B2824)),
        surfaceAlt = dayNightColorProvider(day = Color(0xFFFFE0D8), night = Color(0xFF55332C)),
        accent = dayNightColorProvider(day = Color(0xFFFFB39F), night = Color(0xFF71433A)),
        accentStrong = dayNightColorProvider(day = Color(0xFF6A2A20), night = Color(0xFFFFDDD6)),
        textPrimary = dayNightColorProvider(day = Color(0xFF4C2A24), night = Color(0xFFF7DDD7)),
        textSecondary = dayNightColorProvider(day = Color(0xFF8B5B52), night = Color(0xFFE6BDB6)),
    )
    AppTheme.MINT -> WidgetThemeColors(
        background = dayNightColorProvider(day = Color(0xFFF1FBF4), night = Color(0xFF131A14)),
        surface = dayNightColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF233126)),
        surfaceAlt = dayNightColorProvider(day = Color(0xFFDDF7E1), night = Color(0xFF304437)),
        accent = dayNightColorProvider(day = Color(0xFFA8E6B0), night = Color(0xFF4C7655)),
        accentStrong = dayNightColorProvider(day = Color(0xFF1C4A24), night = Color(0xFFE0F6E4)),
        textPrimary = dayNightColorProvider(day = Color(0xFF1F3A24), night = Color(0xFFDBEFE0)),
        textSecondary = dayNightColorProvider(day = Color(0xFF4E6D55), night = Color(0xFFB7D0BC)),
    )
    AppTheme.LAVENDER -> WidgetThemeColors(
        background = dayNightColorProvider(day = Color(0xFFF5F2FC), night = Color(0xFF171521)),
        surface = dayNightColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF2A2437)),
        surfaceAlt = dayNightColorProvider(day = Color(0xFFE8DEF8), night = Color(0xFF3C3151)),
        accent = dayNightColorProvider(day = Color(0xFFD7C0F6), night = Color(0xFF665381)),
        accentStrong = dayNightColorProvider(day = Color(0xFF4A3768), night = Color(0xFFF0E7FF)),
        textPrimary = dayNightColorProvider(day = Color(0xFF352B4D), night = Color(0xFFF0E8FF)),
        textSecondary = dayNightColorProvider(day = Color(0xFF6A5D88), night = Color(0xFFD0C1EB)),
    )
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

private fun formatDateShort(date: LocalDate): String =
    "${date.monthValue}/${date.dayOfMonth}"

private fun formatElapsed(duration: Duration): String {
    val safeMinutes = duration.toMinutes().coerceAtLeast(0)
    val hours = safeMinutes / 60
    val minutes = safeMinutes % 60
    return when {
        hours > 0 -> "$hours 小时 $minutes 分钟"
        else -> "$minutes 分钟"
    }
}

private fun Float.formatOneDecimal(): String = String.format("%.1f", this)
