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

enum class AppTheme(val label: String) {
    EARTHY("大地自然"),
    PEACH("奶油蜜桃"),
    MINT("清新薄荷"),
    LAVENDER("薰衣紫雾"),
}

enum class FeedingType(val label: String) {
    BREAST_LEFT("母乳左侧"),
    BREAST_RIGHT("母乳右侧"),
    BOTTLE_BREAST_MILK("瓶喂母乳"),
    BOTTLE_FORMULA("瓶喂配方"),
    SOLID_FOOD("辅食"),
}

enum class AllergyStatus(val label: String) {
    NONE("无"),
    OBSERVING("观察中"),
    SAFE("可继续"),
    ALLERGIC("疑似过敏"),
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

enum class MedicalRecordType(val label: String) {
    ILLNESS("疾病"),
    MEDICATION("用药"),
    ALLERGY("过敏"),
}

enum class SleepType(val label: String) {
    NAP("小睡"),
    NIGHT_SLEEP("夜间睡眠"),
}

enum class FallingAsleepMethod(val label: String) {
    NURSING("奶睡"),
    ROCKING("抱睡"),
    SELF_SOOTHING("自主入睡"),
    OTHER("其他"),
}

enum class ReactionSeverity(val label: String) {
    MILD("轻微"),
    MODERATE("中度"),
    SEVERE("严重"),
}

enum class VaccineCategory(val label: String) {
    NATIONAL("国家免疫规划"),
    RECOMMENDED("推荐自费疫苗"),
}

enum class ActivityType(val label: String) {
    OUTDOOR("户外"),
    BATH("洗澡"),
    EARLY_EDUCATION("早教"),
    TUMMY_TIME("趴玩"),
    PLAY("玩耍"),
    OTHER("其他"),
}

enum class RecordTab(val label: String) {
    FEEDING("喂养"),
    SLEEP("睡眠"),
    DIAPER("排泄"),
    MEDICAL("健康"),
    ACTIVITY("活动"),
}

enum class GrowthMetric(val label: String) {
    WEIGHT("体重"),
    HEIGHT("身高"),
    HEAD("头围"),
    BMI("BMI"),
}

data class BabyProfile(
    val name: String,
    val birthday: LocalDate,
    val gender: Gender,
    val avatarPath: String? = null,
)

enum class HomeModule(val label: String) {
    TODAY_SUMMARY("今日摘要"),
    RECENT_FEEDINGS("最近喂奶"),
    RECENT_SLEEP("最近睡眠"),
    LATEST_GROWTH("最近体重"),
    MILESTONE("里程碑"),
    VACCINE("疫苗提醒"),
    TREND("本周趋势"),
    ROUTINE("作息规律"),
    MEMORY("成长回忆"),
    GUIDE("月龄指南"),
}

enum class BackupFrequency(val label: String, val days: Int?) {
    OFF("关闭", null),
    DAILY("每天", 1),
    EVERY_THREE_DAYS("每 3 天", 3),
    WEEKLY("每周", 7),
}

data class TrendInsight(
    val category: String,
    val description: String,
    val direction: TrendDirection,
)

enum class TrendDirection {
    UP,
    DOWN,
    STABLE,
}

data class RoutineInsight(
    val title: String,
    val description: String,
)

data class StageReport(
    val title: String,
    val summary: List<String>,
)

data class StageReportEntry(
    val day: Int,
    val report: StageReport,
)

data class MedicalSummary(
    val title: String,
    val lines: List<String>,
)

data class HandoverSummary(
    val title: String,
    val lines: List<String>,
)

data class MemorySnapshot(
    val title: String,
    val lines: List<String>,
    val photoPaths: List<String>,
)

data class MonthlyGuideEntry(
    val month: Int,
    val title: String,
    val developmentHighlights: List<String>,
    val feedingTips: List<String>,
    val sleepTips: List<String>,
    val careTips: List<String>,
)

data class HomeSummary(
    val profile: BabyProfile?,
    val ageText: String,
    val todayFeedings: Int,
    val todayDiapers: Int,
    val todaySleepMinutes: Long,
    val todaySleepReference: String? = null,
    val todayFeedingReference: String? = null,
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
    val photoPath: String?,
    val note: String?,
    val allergyObservation: AllergyStatus = AllergyStatus.NONE,
    val observationEndDate: LocalDate? = null,
    val caregiver: String? = null,
)

data class SleepDraft(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val note: String?,
    val sleepType: SleepType? = null,
    val fallingAsleepMethod: FallingAsleepMethod? = null,
    val caregiver: String? = null,
)

data class DiaperDraft(
    val happenedAt: LocalDateTime,
    val type: DiaperType,
    val poopColor: PoopColor?,
    val poopTexture: PoopTexture?,
    val note: String?,
    val photoPath: String? = null,
    val caregiver: String? = null,
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
    val photoPath: String?,
    val note: String?,
)

data class MedicalDraft(
    val happenedAt: LocalDateTime,
    val type: MedicalRecordType,
    val title: String,
    val temperatureC: Float?,
    val dosage: String?,
    val note: String?,
    val caregiver: String? = null,
)

data class ActivityDraft(
    val happenedAt: LocalDateTime,
    val type: ActivityType,
    val durationMinutes: Int?,
    val note: String?,
    val caregiver: String? = null,
)

data class VaccineReactionDraft(
    val reactionNote: String?,
    val hadFever: Boolean,
    val reactionSeverity: ReactionSeverity?,
)
