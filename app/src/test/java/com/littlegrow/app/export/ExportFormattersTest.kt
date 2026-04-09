package com.littlegrow.app.export

import com.littlegrow.app.data.BabyProfile
import com.littlegrow.app.data.DiaperEntity
import com.littlegrow.app.data.DiaperType
import com.littlegrow.app.data.ExportSnapshot
import com.littlegrow.app.data.FeedingEntity
import com.littlegrow.app.data.FeedingType
import com.littlegrow.app.data.Gender
import com.littlegrow.app.data.GrowthEntity
import com.littlegrow.app.data.MilestoneCategory
import com.littlegrow.app.data.MilestoneEntity
import com.littlegrow.app.data.PoopColor
import com.littlegrow.app.data.PoopTexture
import com.littlegrow.app.data.SleepEntity
import com.littlegrow.app.data.VaccineEntity
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportFormattersTest {
    @Test
    fun buildCsvIncludesSectionsAndEscapesQuotes() {
        val snapshot = ExportSnapshot(
            generatedAt = LocalDateTime.of(2026, 4, 9, 9, 30),
            profile = BabyProfile(
                name = "小小芽",
                birthday = LocalDate.of(2025, 12, 1),
                gender = Gender.GIRL,
            ),
            feedings = listOf(
                FeedingEntity(
                    id = 1,
                    type = FeedingType.SOLID_FOOD,
                    happenedAt = LocalDateTime.of(2026, 4, 9, 8, 15),
                    durationMinutes = null,
                    amountMl = null,
                    foodName = "南瓜泥",
                    note = "说了\"还要\"",
                ),
            ),
            sleeps = listOf(
                SleepEntity(
                    id = 1,
                    startTime = LocalDateTime.of(2026, 4, 8, 22, 0),
                    endTime = LocalDateTime.of(2026, 4, 9, 6, 30),
                    note = "夜醒一次",
                ),
            ),
            diapers = listOf(
                DiaperEntity(
                    id = 1,
                    happenedAt = LocalDateTime.of(2026, 4, 9, 7, 40),
                    type = DiaperType.POOP,
                    poopColor = PoopColor.YELLOW,
                    poopTexture = PoopTexture.SOFT,
                    note = null,
                ),
            ),
            growthRecords = listOf(
                GrowthEntity(
                    id = 1,
                    date = LocalDate.of(2026, 4, 1),
                    weightKg = 6.8f,
                    heightCm = 64.2f,
                    headCircCm = 41.1f,
                ),
            ),
            milestones = listOf(
                MilestoneEntity(
                    id = 1,
                    title = "会翻身",
                    category = MilestoneCategory.GROSS_MOTOR,
                    achievedDate = LocalDate.of(2026, 4, 5),
                    note = "从俯卧翻到仰卧",
                ),
            ),
            vaccines = listOf(
                VaccineEntity(
                    scheduleKey = "hepb_2",
                    vaccineName = "乙肝疫苗",
                    doseNumber = 2,
                    scheduledDate = LocalDate.of(2026, 1, 1),
                    actualDate = LocalDate.of(2026, 1, 2),
                    isDone = true,
                ),
            ),
        )

        val csv = buildCsv(snapshot)

        assertTrue(csv.contains("\"宝宝资料\""))
        assertTrue(csv.contains("\"喂养记录\""))
        assertTrue(csv.contains("\"说了\"\"还要\"\"\""))
        assertTrue(csv.contains("\"乙肝疫苗\""))
    }
}
