package com.littlegrow.app.data

import java.time.LocalDateTime

data class ExportSnapshot(
    val generatedAt: LocalDateTime,
    val profile: BabyProfile?,
    val feedings: List<FeedingEntity>,
    val sleeps: List<SleepEntity>,
    val diapers: List<DiaperEntity>,
    val growthRecords: List<GrowthEntity>,
    val milestones: List<MilestoneEntity>,
    val medicalRecords: List<MedicalEntity>,
    val activityRecords: List<ActivityEntity>,
    val vaccines: List<VaccineEntity>,
)

fun BabyEntity.toProfile(): BabyProfile {
    return BabyProfile(
        name = name,
        birthday = birthday,
        gender = gender,
    )
}
