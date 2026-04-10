package com.littlegrow.app.data

data class ReferenceRange(
    val label: String,
    val min: Double,
    val max: Double,
    val unit: String,
)

object AgeBasedReference {
    fun feedingTimesPerDay(ageMonths: Int): ReferenceRange {
        return when {
            ageMonths < 1 -> ReferenceRange("0-1 月龄", 8.0, 12.0, "次/天")
            ageMonths < 3 -> ReferenceRange("1-3 月龄", 7.0, 10.0, "次/天")
            ageMonths < 6 -> ReferenceRange("3-6 月龄", 6.0, 8.0, "次/天")
            ageMonths < 12 -> ReferenceRange("6-12 月龄", 5.0, 7.0, "次/天")
            else -> ReferenceRange("1 岁+", 3.0, 5.0, "次/天")
        }
    }

    fun sleepHoursPerDay(ageMonths: Int): ReferenceRange {
        return when {
            ageMonths < 1 -> ReferenceRange("0-1 月龄", 14.0, 17.0, "小时/天")
            ageMonths < 3 -> ReferenceRange("1-3 月龄", 14.0, 16.0, "小时/天")
            ageMonths < 6 -> ReferenceRange("3-6 月龄", 12.0, 15.0, "小时/天")
            ageMonths < 12 -> ReferenceRange("6-12 月龄", 12.0, 14.0, "小时/天")
            ageMonths < 24 -> ReferenceRange("1-2 岁", 11.0, 14.0, "小时/天")
            else -> ReferenceRange("2 岁+", 10.0, 13.0, "小时/天")
        }
    }

    fun bottleIntervalHours(ageMonths: Int): ReferenceRange {
        return when {
            ageMonths < 1 -> ReferenceRange("0-1 月龄", 2.0, 3.0, "小时")
            ageMonths < 3 -> ReferenceRange("1-3 月龄", 2.0, 4.0, "小时")
            ageMonths < 6 -> ReferenceRange("3-6 月龄", 3.0, 4.0, "小时")
            ageMonths < 12 -> ReferenceRange("6-12 月龄", 3.0, 5.0, "小时")
            else -> ReferenceRange("1 岁+", 4.0, 6.0, "小时")
        }
    }
}
