package com.littlegrow.app.data

import java.time.Duration
import java.time.LocalDateTime

object HandoverSummaryGenerator {
    fun build(
        from: LocalDateTime,
        to: LocalDateTime,
        feedings: List<FeedingEntity>,
        sleeps: List<SleepEntity>,
        diapers: List<DiaperEntity>,
        medicalRecords: List<MedicalEntity>,
        activityRecords: List<ActivityEntity>,
    ): HandoverSummary {
        val filteredFeedings = feedings.filter { it.happenedAt in from..to }
        val filteredSleeps = sleeps.filter { it.startTime in from..to || it.endTime in from..to }
        val filteredDiapers = diapers.filter { it.happenedAt in from..to }
        val filteredMedical = medicalRecords.filter { it.happenedAt in from..to }
        val filteredActivities = activityRecords.filter { it.happenedAt in from..to }
        return HandoverSummary(
            title = "交接摘要",
            lines = buildList {
                add("从 $from 到 $to")
                add("喂养 ${filteredFeedings.size} 次，最近一次 ${filteredFeedings.maxByOrNull { it.happenedAt }?.happenedAt ?: "无"}。")
                add("排泄 ${filteredDiapers.size} 次，其中大便 ${filteredDiapers.count { it.type == DiaperType.POOP }} 次。")
                add("睡眠 ${filteredSleeps.size} 段，共 ${Duration.ofMinutes(filteredSleeps.sumOf { Duration.between(it.startTime, it.endTime).toMinutes() }).toHours()} 小时。")
                if (filteredActivities.isNotEmpty()) add("活动记录 ${filteredActivities.size} 条。")
                if (filteredMedical.isNotEmpty()) add("健康相关 ${filteredMedical.size} 条，请优先查看。")
            },
        )
    }
}
