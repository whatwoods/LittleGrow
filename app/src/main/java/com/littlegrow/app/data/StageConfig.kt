package com.littlegrow.app.data

data class StageConfig(
    val prioritizedTabs: List<RecordTab>,
    val prioritizedModules: List<HomeModule>,
)

object StageConfigProvider {
    fun forAgeMonths(ageMonths: Int): StageConfig {
        return when {
            ageMonths < 4 -> StageConfig(
                prioritizedTabs = listOf(RecordTab.FEEDING, RecordTab.SLEEP, RecordTab.DIAPER, RecordTab.MEDICAL, RecordTab.ACTIVITY),
                prioritizedModules = listOf(HomeModule.TODAY_SUMMARY, HomeModule.RECENT_FEEDINGS, HomeModule.RECENT_SLEEP, HomeModule.VACCINE, HomeModule.GUIDE),
            )
            ageMonths < 7 -> StageConfig(
                prioritizedTabs = listOf(RecordTab.FEEDING, RecordTab.SLEEP, RecordTab.ACTIVITY, RecordTab.DIAPER, RecordTab.MEDICAL),
                prioritizedModules = listOf(HomeModule.TODAY_SUMMARY, HomeModule.TREND, HomeModule.RECENT_FEEDINGS, HomeModule.MILESTONE, HomeModule.GUIDE),
            )
            ageMonths < 13 -> StageConfig(
                prioritizedTabs = listOf(RecordTab.ACTIVITY, RecordTab.FEEDING, RecordTab.SLEEP, RecordTab.DIAPER, RecordTab.MEDICAL),
                prioritizedModules = listOf(HomeModule.TODAY_SUMMARY, HomeModule.TREND, HomeModule.ROUTINE, HomeModule.MILESTONE, HomeModule.LATEST_GROWTH),
            )
            else -> StageConfig(
                prioritizedTabs = listOf(RecordTab.ACTIVITY, RecordTab.FEEDING, RecordTab.SLEEP, RecordTab.MEDICAL, RecordTab.DIAPER),
                prioritizedModules = listOf(HomeModule.TODAY_SUMMARY, HomeModule.ROUTINE, HomeModule.TREND, HomeModule.MEMORY, HomeModule.GUIDE),
            )
        }
    }
}
