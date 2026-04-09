package com.littlegrow.app.ui

import com.littlegrow.app.data.GrowthMetric
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

fun LocalDate.formatDate(): String = format(dateFormatter)

fun LocalDateTime.formatDateTime(): String = format(dateTimeFormatter)

fun Duration.formatDuration(): String {
    val hours = toHours()
    val minutes = toMinutes() % 60
    return if (hours > 0) {
        "${hours} 小时 ${minutes} 分钟"
    } else {
        "${minutes} 分钟"
    }
}

fun Long.formatMinutes(): String {
    val duration = Duration.ofMinutes(this)
    return duration.formatDuration()
}

fun Float?.formatMetric(metric: GrowthMetric): String {
    if (this == null) return "未记录"
    return when (metric) {
        GrowthMetric.WEIGHT -> String.format("%.1f kg", this)
        GrowthMetric.HEIGHT -> String.format("%.1f cm", this)
        GrowthMetric.HEAD -> String.format("%.1f cm", this)
    }
}
