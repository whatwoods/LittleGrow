package com.littlegrow.app.data

import java.time.Duration
import java.time.LocalDate

object MedicalSummaryGenerator {
    fun build(
        profile: BabyProfile?,
        days: Long,
        now: LocalDate,
        feedings: List<FeedingEntity>,
        sleeps: List<SleepEntity>,
        diapers: List<DiaperEntity>,
        medicalRecords: List<MedicalEntity>,
        growthRecords: List<GrowthEntity>,
    ): MedicalSummary {
        val fromDate = now.minusDays(days - 1)
        val filteredFeedings = feedings.filter { it.happenedAt.toLocalDate() in fromDate..now }
        val filteredSleeps = sleeps.filter { it.startTime.toLocalDate() in fromDate..now }
        val filteredDiapers = diapers.filter { it.happenedAt.toLocalDate() in fromDate..now }
        val filteredMedical = medicalRecords.filter { it.happenedAt.toLocalDate() in fromDate..now }

        return MedicalSummary(
            title = "最近 $days 天就医摘要",
            lines = buildList {
                profile?.let {
                    add("${it.name} · ${it.gender.label} · 出生于 ${it.birthday}")
                }
                growthRecords.maxByOrNull { it.date }?.let { latest ->
                    add("最近生长：体重 ${latest.weightKg ?: "-"} kg，身高 ${latest.heightCm ?: "-"} cm。")
                }
                add("喂养：共 ${filteredFeedings.size} 次，平均每天 ${String.format("%.1f", filteredFeedings.size / days.toDouble())} 次。")
                add("睡眠：共 ${Duration.ofMinutes(filteredSleeps.sumOf { Duration.between(it.startTime, it.endTime).toMinutes() }).toHours()} 小时。")
                add("排泄：共 ${filteredDiapers.size} 次，其中大便 ${filteredDiapers.count { it.type == DiaperType.POOP }} 次。")
                if (filteredMedical.isEmpty()) {
                    add("近期无额外疾病/用药/过敏记录。")
                } else {
                    add("健康记录：")
                    filteredMedical.sortedBy { it.happenedAt }.forEach { record ->
                        add("- ${record.happenedAt} ${record.type.label} ${record.title}${record.temperatureC?.let { " ${it}℃" }.orEmpty()}")
                    }
                }
            },
        )
    }
}
