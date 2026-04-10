package com.littlegrow.app.data

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class FeedingFormDefaults(
    val defaultType: FeedingType = FeedingType.BREAST_LEFT,
    val defaultHappenedAt: LocalDateTime = LocalDateTime.now(),
    val defaultDurationMinutes: Int = 15,
    val defaultBottleAmountMl: Int = 90,
    val orderedTypes: List<FeedingType> = FeedingType.entries,
    val hiddenTypes: Set<FeedingType> = emptySet(),
    val breastSideHint: String? = null,
    val hiddenTypesHint: String? = null,
)

class SmartDefaultsProvider {
    fun buildFeedingDefaults(
        profile: BabyProfile?,
        feedings: List<FeedingEntity>,
        now: LocalDateTime = LocalDateTime.now(),
    ): FeedingFormDefaults {
        val sortedFeedings = feedings.sortedByDescending { it.happenedAt }
        val latestFeeding = sortedFeedings.firstOrNull()
        val latestBreastfeeding = sortedFeedings.firstOrNull { it.type.isBreastfeeding }
        val latestBottleFeeding = sortedFeedings.firstOrNull { it.type.isBottleFeeding }
        val recentSolidFoodCount = sortedFeedings.count {
            it.type == FeedingType.SOLID_FOOD &&
                ChronoUnit.DAYS.between(it.happenedAt.toLocalDate(), now.toLocalDate()) in 0..6
        }
        val ageMonths = profile?.birthday?.let {
            ChronoUnit.MONTHS.between(it, now.toLocalDate()).toInt()
        } ?: 0

        val suggestedBreastType = latestBreastfeeding?.type?.oppositeBreastSide
        val defaultType = when {
            now.hour in 0..5 -> suggestedBreastType ?: FeedingType.BREAST_LEFT
            latestFeeding?.type?.isBreastfeeding == true -> suggestedBreastType ?: FeedingType.BREAST_LEFT
            latestFeeding != null -> latestFeeding.type
            else -> FeedingType.BREAST_LEFT
        }

        val hideSolidFoodByDefault = ageMonths < 6 && recentSolidFoodCount == 0
        val prioritizedTypes = if (ageMonths >= 6 && recentSolidFoodCount > 0) {
            listOf(FeedingType.SOLID_FOOD) + FeedingType.entries.filterNot { it == FeedingType.SOLID_FOOD }
        } else {
            FeedingType.entries
        }
        val fallbackType = prioritizedTypes.firstOrNull { it != FeedingType.SOLID_FOOD } ?: FeedingType.BREAST_LEFT
        val safeDefaultType = if (hideSolidFoodByDefault && defaultType == FeedingType.SOLID_FOOD) {
            fallbackType
        } else {
            defaultType
        }

        return FeedingFormDefaults(
            defaultType = safeDefaultType,
            defaultHappenedAt = now,
            defaultDurationMinutes = latestBreastfeeding?.durationMinutes?.takeIf { it > 0 } ?: 15,
            defaultBottleAmountMl = latestBottleFeeding?.amountMl?.takeIf { it > 0 } ?: 90,
            orderedTypes = prioritizedTypes,
            hiddenTypes = if (hideSolidFoodByDefault) setOf(FeedingType.SOLID_FOOD) else emptySet(),
            breastSideHint = suggestedBreastType?.let { suggested ->
                val lastSide = latestBreastfeeding.type.sideLabel ?: return@let null
                val nextSide = suggested.sideLabel ?: return@let null
                "上次喂的$lastSide，这次可先喂$nextSide。"
            },
            hiddenTypesHint = if (hideSolidFoodByDefault) {
                "宝宝未满 6 个月，且最近 7 天没有辅食记录，已先隐藏辅食。"
            } else {
                null
            },
        )
    }
}

private val FeedingType.isBreastfeeding: Boolean
    get() = this == FeedingType.BREAST_LEFT || this == FeedingType.BREAST_RIGHT

private val FeedingType.isBottleFeeding: Boolean
    get() = this == FeedingType.BOTTLE_BREAST_MILK || this == FeedingType.BOTTLE_FORMULA

private val FeedingType.oppositeBreastSide: FeedingType?
    get() = when (this) {
        FeedingType.BREAST_LEFT -> FeedingType.BREAST_RIGHT
        FeedingType.BREAST_RIGHT -> FeedingType.BREAST_LEFT
        else -> null
    }

private val FeedingType.sideLabel: String?
    get() = when (this) {
        FeedingType.BREAST_LEFT -> "左侧"
        FeedingType.BREAST_RIGHT -> "右侧"
        else -> null
    }
