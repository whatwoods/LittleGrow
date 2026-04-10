package com.littlegrow.app.data

import java.time.Duration
import java.time.LocalDate

object StageReportGenerator {
    private val stageDays = listOf(30, 100, 180, 365)

    fun reachedReports(
        birthday: LocalDate,
        ageDays: Int,
        feedings: List<FeedingEntity>,
        sleeps: List<SleepEntity>,
        diapers: List<DiaperEntity>,
        growthRecords: List<GrowthEntity>,
        milestones: List<MilestoneEntity>,
    ): List<StageReportEntry> {
        return stageDays
            .filter { ageDays >= it }
            .map { day ->
                buildReport(
                    birthday = birthday,
                    targetDay = day,
                    feedings = feedings,
                    sleeps = sleeps,
                    diapers = diapers,
                    growthRecords = growthRecords,
                    milestones = milestones,
                )
            }
    }

    fun pendingReport(
        birthday: LocalDate,
        ageDays: Int,
        shownKeys: Set<String>,
        feedings: List<FeedingEntity>,
        sleeps: List<SleepEntity>,
        diapers: List<DiaperEntity>,
        growthRecords: List<GrowthEntity>,
        milestones: List<MilestoneEntity>,
    ): StageReportEntry? {
        val targetDay = stageDays.firstOrNull { kotlin.math.abs(ageDays - it) <= 1 && it.toString() !in shownKeys } ?: return null
        return buildReport(
            birthday = birthday,
            targetDay = targetDay,
            feedings = feedings,
            sleeps = sleeps,
            diapers = diapers,
            growthRecords = growthRecords,
            milestones = milestones,
        )
    }

    private fun buildReport(
        birthday: LocalDate,
        targetDay: Int,
        feedings: List<FeedingEntity>,
        sleeps: List<SleepEntity>,
        diapers: List<DiaperEntity>,
        growthRecords: List<GrowthEntity>,
        milestones: List<MilestoneEntity>,
    ): StageReportEntry {
        val endDate = birthday.plusDays((targetDay - 1).toLong())
        val filteredFeedings = feedings.filter { it.happenedAt.toLocalDate() <= endDate }
        val filteredSleeps = sleeps.filter { it.startTime.toLocalDate() <= endDate }
        val filteredDiapers = diapers.filter { it.happenedAt.toLocalDate() <= endDate }
        val filteredGrowths = growthRecords.filter { it.date <= endDate }.sortedBy { it.date }
        val filteredMilestones = milestones.filter { it.achievedDate <= endDate }
        val stageTitle = when (targetDay) {
            30 -> "满月小结"
            100 -> "百天小结"
            180 -> "半岁小结"
            else -> "一岁小结"
        }

        val firstWeight = filteredGrowths.firstOrNull()?.weightKg
        val latestGrowth = filteredGrowths.lastOrNull()
        val totalSleepHours = Duration.ofMinutes(
            filteredSleeps.sumOf { Duration.between(it.startTime, it.endTime).toMinutes() },
        ).toHours()

        val summary = buildList {
            add("截至第 $targetDay 天，共记录喂养 ${filteredFeedings.size} 次。")
            add("累计睡眠 $totalSleepHours 小时，换尿布 ${filteredDiapers.size} 次。")
            latestGrowth?.let { latest ->
                add(
                    "最近生长：体重 ${latest.weightKg?.let { String.format("%.1f kg", it) } ?: "-"}，" +
                        "身高 ${latest.heightCm?.let { String.format("%.1f cm", it) } ?: "-"}。",
                )
            }
            if (firstWeight != null && latestGrowth?.weightKg != null) {
                add("阶段体重变化 ${String.format("%.1f kg", latestGrowth.weightKg - firstWeight)}。")
            }
            if (filteredMilestones.isNotEmpty()) {
                add("已达成 ${filteredMilestones.size} 个里程碑：${filteredMilestones.joinToString { it.title }}。")
            }
        }

        return StageReportEntry(
            day = targetDay,
            report = StageReport(
                title = stageTitle,
                summary = summary,
            ),
        )
    }
}
