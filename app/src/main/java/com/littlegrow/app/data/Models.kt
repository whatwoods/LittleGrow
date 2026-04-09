package com.littlegrow.app.data

import java.time.LocalDate
import java.time.LocalDateTime

enum class Gender(val label: String) {
    BOY("男宝宝"),
    GIRL("女宝宝"),
}

enum class ThemeMode(val label: String) {
    SYSTEM("跟随系统"),
    LIGHT("浅色"),
    DARK("深色"),
}

enum class FeedingType(val label: String) {
    BREAST_LEFT("母乳左侧"),
    BREAST_RIGHT("母乳右侧"),
    BOTTLE_BREAST_MILK("瓶喂母乳"),
    BOTTLE_FORMULA("瓶喂配方"),
    SOLID_FOOD("辅食"),
}

enum class DiaperType(val label: String) {
    PEE("小便"),
    POOP("大便"),
}

enum class PoopColor(val label: String) {
    YELLOW("黄色"),
    GREEN("绿色"),
    BROWN("棕色"),
    RED("红色"),
    WHITE("白色"),
    BLACK("黑色"),
}

enum class PoopTexture(val label: String) {
    LIQUID("稀"),
    SOFT("软"),
    NORMAL("正常"),
    HARD("偏硬"),
}

enum class MilestoneCategory(val label: String) {
    GROSS_MOTOR("大动作"),
    FINE_MOTOR("精细动作"),
    LANGUAGE("语言"),
    SOCIAL("社交"),
    COGNITIVE("认知"),
}

enum class RecordTab(val label: String) {
    FEEDING("喂养"),
    SLEEP("睡眠"),
    DIAPER("排泄"),
}

enum class GrowthMetric(val label: String) {
    WEIGHT("体重"),
    HEIGHT("身高"),
    HEAD("头围"),
}

data class BabyProfile(
    val name: String,
    val birthday: LocalDate,
    val gender: Gender,
)

data class HomeSummary(
    val profile: BabyProfile?,
    val ageText: String,
    val todayFeedings: Int,
    val todayDiapers: Int,
    val todaySleepMinutes: Long,
    val latestGrowth: GrowthEntity?,
    val latestMilestone: MilestoneEntity?,
    val recentFeedings: List<FeedingEntity>,
    val recentSleeps: List<SleepEntity>,
    val recentDiapers: List<DiaperEntity>,
)

data class FeedingDraft(
    val type: FeedingType,
    val happenedAt: LocalDateTime,
    val durationMinutes: Int?,
    val amountMl: Int?,
    val foodName: String?,
    val note: String?,
)

data class SleepDraft(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val note: String?,
)

data class DiaperDraft(
    val happenedAt: LocalDateTime,
    val type: DiaperType,
    val poopColor: PoopColor?,
    val poopTexture: PoopTexture?,
    val note: String?,
)

data class GrowthDraft(
    val date: LocalDate,
    val weightKg: Float?,
    val heightCm: Float?,
    val headCircCm: Float?,
)

data class MilestoneDraft(
    val title: String,
    val category: MilestoneCategory,
    val achievedDate: LocalDate,
    val note: String?,
)
