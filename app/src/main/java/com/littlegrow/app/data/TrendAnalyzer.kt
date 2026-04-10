package com.littlegrow.app.data

import java.time.Duration
import java.time.LocalDate

object TrendAnalyzer {
    fun weeklyTrends(
        today: LocalDate,
        feedings: List<FeedingEntity>,
        sleeps: List<SleepEntity>,
    ): List<TrendInsight> {
        val thisWeekStart = today.minusDays(6)
        val lastWeekStart = today.minusDays(13)
        val lastWeekEnd = today.minusDays(7)

        val thisWeekFeedings = feedings.count { it.happenedAt.toLocalDate() in thisWeekStart..today }
        val lastWeekFeedings = feedings.count { it.happenedAt.toLocalDate() in lastWeekStart..lastWeekEnd }
        val thisWeekSleepMinutes = sleeps
            .filter { it.startTime.toLocalDate() in thisWeekStart..today }
            .sumOf { Duration.between(it.startTime, it.endTime).toMinutes() }
        val lastWeekSleepMinutes = sleeps
            .filter { it.startTime.toLocalDate() in lastWeekStart..lastWeekEnd }
            .sumOf { Duration.between(it.startTime, it.endTime).toMinutes() }

        val thisWeekBottleIntervals = feedings
            .sortedBy { it.happenedAt }
            .zipWithNext()
            .filter { (current, next) -> current.happenedAt.toLocalDate() in thisWeekStart..today && next.happenedAt.toLocalDate() in thisWeekStart..today }
            .map { (current, next) -> Duration.between(current.happenedAt, next.happenedAt).toMinutes().toDouble() / 60.0 }
        val lastWeekBottleIntervals = feedings
            .sortedBy { it.happenedAt }
            .zipWithNext()
            .filter { (current, next) -> current.happenedAt.toLocalDate() in lastWeekStart..lastWeekEnd && next.happenedAt.toLocalDate() in lastWeekStart..lastWeekEnd }
            .map { (current, next) -> Duration.between(current.happenedAt, next.happenedAt).toMinutes().toDouble() / 60.0 }

        return buildList {
            add(
                compareMetric(
                    category = "喂养",
                    current = thisWeekFeedings.toDouble(),
                    previous = lastWeekFeedings.toDouble(),
                    formatter = { diff ->
                        if (diff == 0.0) {
                            "本周喂养次数与上周基本持平。"
                        } else {
                            "本周比上周${if (diff > 0) "多" else "少"}喂了 ${kotlin.math.abs(diff).toInt()} 次。"
                        }
                    },
                ),
            )
            add(
                compareMetric(
                    category = "睡眠",
                    current = thisWeekSleepMinutes / 7.0,
                    previous = lastWeekSleepMinutes / 7.0,
                    formatter = { diff ->
                        if (diff == 0.0) {
                            "最近两周的日均睡眠差异不大。"
                        } else {
                            "本周日均睡眠比上周${if (diff > 0) "多" else "少"} ${kotlin.math.abs(diff).toInt()} 分钟。"
                        }
                    },
                ),
            )
            if (thisWeekBottleIntervals.isNotEmpty() && lastWeekBottleIntervals.isNotEmpty()) {
                val current = thisWeekBottleIntervals.average()
                val previous = lastWeekBottleIntervals.average()
                add(
                    compareMetric(
                        category = "间隔",
                        current = current,
                        previous = previous,
                        formatter = { diff ->
                            if (diff == 0.0) {
                                "喂奶间隔和上周基本一致。"
                            } else {
                                "喂奶间隔从 ${String.format("%.1f", previous)} 小时变为 ${String.format("%.1f", current)} 小时。"
                            }
                        },
                    ),
                )
            }
        }.sortedByDescending { insight ->
            when (insight.direction) {
                TrendDirection.UP -> 2
                TrendDirection.DOWN -> 1
                TrendDirection.STABLE -> 0
            }
        }
    }

    fun routineInsights(
        feedings: List<FeedingEntity>,
        sleeps: List<SleepEntity>,
    ): List<RoutineInsight> {
        val sleepHour = sleeps
            .groupingBy { it.startTime.hour }
            .eachCount()
            .maxByOrNull { it.value }
        val feedingHour = feedings
            .groupingBy { it.happenedAt.hour }
            .eachCount()
            .maxByOrNull { it.value }
        return buildList {
            sleepHour?.let {
                add(RoutineInsight("午睡规律", "最近更常在 ${it.key}:00 左右开始睡觉。"))
            }
            feedingHour?.let {
                add(RoutineInsight("喂养高峰", "最近 ${it.key}:00 左右更容易进入喂养节奏。"))
            }
        }
    }

    private fun compareMetric(
        category: String,
        current: Double,
        previous: Double,
        formatter: (Double) -> String,
    ): TrendInsight {
        val diff = current - previous
        val direction = when {
            diff > 0.1 -> TrendDirection.UP
            diff < -0.1 -> TrendDirection.DOWN
            else -> TrendDirection.STABLE
        }
        return TrendInsight(
            category = category,
            description = formatter(diff),
            direction = direction,
        )
    }
}
