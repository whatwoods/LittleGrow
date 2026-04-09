package com.littlegrow.app.data

import java.time.LocalDate

data class VaccineTemplate(
    val scheduleKey: String,
    val vaccineName: String,
    val doseNumber: Int,
    val monthsAfterBirth: Long = 0,
    val daysAfterBirth: Long = 0,
)

private val nationalVaccineTemplates = listOf(
    VaccineTemplate("hepb_1", "乙肝疫苗", 1),
    VaccineTemplate("bcg_1", "卡介苗", 1),
    VaccineTemplate("hepb_2", "乙肝疫苗", 2, monthsAfterBirth = 1),
    VaccineTemplate("polio_1", "脊灰疫苗", 1, monthsAfterBirth = 2),
    VaccineTemplate("dtap_1", "百白破疫苗", 1, monthsAfterBirth = 3),
    VaccineTemplate("polio_2", "脊灰疫苗", 2, monthsAfterBirth = 3),
    VaccineTemplate("dtap_2", "百白破疫苗", 2, monthsAfterBirth = 4),
    VaccineTemplate("polio_3", "脊灰疫苗", 3, monthsAfterBirth = 4),
    VaccineTemplate("dtap_3", "百白破疫苗", 3, monthsAfterBirth = 5),
    VaccineTemplate("hepb_3", "乙肝疫苗", 3, monthsAfterBirth = 6),
    VaccineTemplate("mena_1", "A群流脑疫苗", 1, monthsAfterBirth = 6),
    VaccineTemplate("mmr_1", "麻腮风疫苗", 1, monthsAfterBirth = 8),
    VaccineTemplate("je_1", "乙脑减毒活疫苗", 1, monthsAfterBirth = 8),
    VaccineTemplate("mena_2", "A群流脑疫苗", 2, monthsAfterBirth = 9),
    VaccineTemplate("dtap_4", "百白破疫苗", 4, monthsAfterBirth = 18),
    VaccineTemplate("mmr_2", "麻腮风疫苗", 2, monthsAfterBirth = 18),
    VaccineTemplate("hepa_1", "甲肝减毒活疫苗", 1, monthsAfterBirth = 18),
)

fun buildNationalVaccineSchedule(
    birthday: LocalDate,
    existing: List<VaccineEntity>,
): List<VaccineEntity> {
    val existingByKey = existing.associateBy { it.scheduleKey }
    return nationalVaccineTemplates.map { template ->
        val scheduledDate = birthday
            .plusMonths(template.monthsAfterBirth)
            .plusDays(template.daysAfterBirth)
        val current = existingByKey[template.scheduleKey]
        VaccineEntity(
            scheduleKey = template.scheduleKey,
            vaccineName = template.vaccineName,
            doseNumber = template.doseNumber,
            scheduledDate = scheduledDate,
            actualDate = current?.actualDate,
            isDone = current?.isDone ?: false,
        )
    }.sortedBy { it.scheduledDate }
}
