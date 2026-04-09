package com.littlegrow.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class VaccineScheduleTest {
    @Test
    fun buildSchedule_generatesNationalPlanFromBirthday() {
        val birthday = LocalDate.of(2026, 1, 15)
        val vaccines = buildNationalVaccineSchedule(birthday, emptyList())

        assertEquals(birthday, vaccines.first { it.scheduleKey == "hepb_1" }.scheduledDate)
        assertEquals(
            LocalDate.of(2026, 9, 15),
            vaccines.first { it.scheduleKey == "mmr_1" }.scheduledDate,
        )
        assertTrue(vaccines.any { it.scheduleKey == "dtap_4" })
    }

    @Test
    fun buildSchedule_preservesExistingCompletionState() {
        val birthday = LocalDate.of(2026, 1, 15)
        val existing = listOf(
            VaccineEntity(
                scheduleKey = "hepb_1",
                vaccineName = "乙肝疫苗",
                doseNumber = 1,
                scheduledDate = birthday,
                actualDate = birthday,
                isDone = true,
            ),
        )

        val vaccines = buildNationalVaccineSchedule(birthday, existing)

        assertTrue(vaccines.first { it.scheduleKey == "hepb_1" }.isDone)
        assertEquals(birthday, vaccines.first { it.scheduleKey == "hepb_1" }.actualDate)
    }
}
