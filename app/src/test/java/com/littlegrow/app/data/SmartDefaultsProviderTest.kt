package com.littlegrow.app.data

import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartDefaultsProviderTest {
    private val provider = SmartDefaultsProvider()

    @Test
    fun switchesToOppositeBreastSideAndShowsHint() {
        val now = LocalDateTime.of(2026, 4, 9, 10, 0)

        val defaults = provider.buildFeedingDefaults(
            profile = BabyProfile(
                name = "小芽",
                birthday = LocalDate.of(2026, 1, 1),
                gender = Gender.GIRL,
            ),
            feedings = listOf(
                FeedingEntity(
                    id = 1,
                    type = FeedingType.BREAST_LEFT,
                    happenedAt = now.minusHours(2),
                    durationMinutes = 18,
                    amountMl = null,
                    foodName = null,
                    photoPath = null,
                    note = null,
                ),
            ),
            now = now,
        )

        assertEquals(FeedingType.BREAST_RIGHT, defaults.defaultType)
        assertEquals(18, defaults.defaultDurationMinutes)
        assertEquals("上次喂的左侧，这次可先喂右侧。", defaults.breastSideHint)
    }

    @Test
    fun hidesSolidFoodBeforeSixMonthsWithoutRecentSolidRecords() {
        val now = LocalDateTime.of(2026, 4, 9, 9, 30)

        val defaults = provider.buildFeedingDefaults(
            profile = BabyProfile(
                name = "小芽",
                birthday = LocalDate.of(2025, 12, 15),
                gender = Gender.GIRL,
            ),
            feedings = listOf(
                FeedingEntity(
                    id = 1,
                    type = FeedingType.BOTTLE_FORMULA,
                    happenedAt = now.minusHours(3),
                    durationMinutes = null,
                    amountMl = 120,
                    foodName = null,
                    photoPath = null,
                    note = null,
                ),
            ),
            now = now,
        )

        assertEquals(FeedingType.BOTTLE_FORMULA, defaults.defaultType)
        assertEquals(120, defaults.defaultBottleAmountMl)
        assertTrue(FeedingType.SOLID_FOOD in defaults.hiddenTypes)
        assertTrue(defaults.hiddenTypesHint?.contains("已先隐藏辅食") == true)
    }

    @Test
    fun prioritizesSolidFoodAfterSixMonthsWhenRecentlyRecorded() {
        val now = LocalDateTime.of(2026, 9, 9, 11, 0)

        val defaults = provider.buildFeedingDefaults(
            profile = BabyProfile(
                name = "小芽",
                birthday = LocalDate.of(2025, 12, 1),
                gender = Gender.BOY,
            ),
            feedings = listOf(
                FeedingEntity(
                    id = 1,
                    type = FeedingType.SOLID_FOOD,
                    happenedAt = now.minusDays(1),
                    durationMinutes = null,
                    amountMl = null,
                    foodName = "南瓜泥",
                    photoPath = null,
                    note = null,
                ),
            ),
            now = now,
        )

        assertEquals(FeedingType.SOLID_FOOD, defaults.orderedTypes.first())
        assertFalse(FeedingType.SOLID_FOOD in defaults.hiddenTypes)
    }
}
