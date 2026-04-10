package com.littlegrow.app.data

object EncouragementProvider {
    fun build(
        todayFeedings: Int,
        todaySleepMinutes: Long,
        consecutiveDays: Int,
    ): String {
        return when {
            consecutiveDays >= 30 -> "已经连续记录 $consecutiveDays 天了，照护节奏很稳。"
            todayFeedings >= 6 -> "今天已经喂了 $todayFeedings 次奶，辛苦了。"
            todaySleepMinutes >= 12 * 60 -> "今天睡眠记录看起来很扎实，继续保持。"
            else -> "今天也在认真记录，后面会更容易看出规律。"
        }
    }
}
