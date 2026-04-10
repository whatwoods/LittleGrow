package com.littlegrow.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material.icons.rounded.Today
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.littlegrow.app.data.AppTheme
import com.littlegrow.app.data.RecordTab
import com.littlegrow.app.data.ThemeMode
import com.littlegrow.app.ui.screens.BatchRecordScreen
import com.littlegrow.app.ui.screens.GrowthScreen
import com.littlegrow.app.ui.screens.HandoverSummarySheet
import com.littlegrow.app.ui.screens.HomeScreen
import com.littlegrow.app.ui.screens.MedicalSummaryScreen
import com.littlegrow.app.ui.screens.OnboardingScreen
import com.littlegrow.app.ui.screens.QuickRecordSheet
import com.littlegrow.app.ui.screens.RecordsScreen
import com.littlegrow.app.ui.screens.SettingsScreen
import com.littlegrow.app.ui.screens.StageReportSheet
import com.littlegrow.app.ui.screens.TimelineScreen
import com.littlegrow.app.ui.theme.softShadow

private data class TopLevelDestination(
    val destination: AppDestination,
    val label: String,
    val icon: ImageVector,
)

private val topLevelDestinations = listOf(
    TopLevelDestination(AppDestination.HOME, "首页", Icons.Rounded.Home),
    TopLevelDestination(AppDestination.RECORDS, "记录", Icons.Rounded.Today),
    TopLevelDestination(AppDestination.GROWTH, "成长", Icons.Rounded.AutoGraph),
    TopLevelDestination(AppDestination.TIMELINE, "时光", Icons.Rounded.Timeline),
    TopLevelDestination(AppDestination.SETTINGS, "设置", Icons.Rounded.Settings),
)

private const val batchRecordRoutePattern = "batch_records/{tab}"

private fun batchRecordRoute(tab: RecordTab): String = "${AppDestination.BATCH_RECORDS.route}/${tab.name}"

@Composable
fun LittleGrowApp(
    viewModel: MainViewModel,
    themeMode: ThemeMode,
    appTheme: AppTheme,
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: AppDestination.HOME.route

    val summary by viewModel.homeSummary.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val feedings by viewModel.feedings.collectAsStateWithLifecycle()
    val sleeps by viewModel.sleeps.collectAsStateWithLifecycle()
    val diapers by viewModel.diapers.collectAsStateWithLifecycle()
    val growthRecords by viewModel.growthRecords.collectAsStateWithLifecycle()
    val milestones by viewModel.milestones.collectAsStateWithLifecycle()
    val medicalRecords by viewModel.medicalRecords.collectAsStateWithLifecycle()
    val activityRecords by viewModel.activityRecords.collectAsStateWithLifecycle()
    val vaccines by viewModel.vaccines.collectAsStateWithLifecycle()
    val vaccineRemindersEnabled by viewModel.vaccineRemindersEnabled.collectAsStateWithLifecycle()
    val anomalyRemindersEnabled by viewModel.anomalyRemindersEnabled.collectAsStateWithLifecycle()
    val diaperRemindersEnabled by viewModel.diaperRemindersEnabled.collectAsStateWithLifecycle()
    val largeTextModeEnabled by viewModel.largeTextModeEnabled.collectAsStateWithLifecycle()
    val darkModeScheduleEnabled by viewModel.darkModeScheduleEnabled.collectAsStateWithLifecycle()
    val darkModeStartHour by viewModel.darkModeStartHour.collectAsStateWithLifecycle()
    val darkModeEndHour by viewModel.darkModeEndHour.collectAsStateWithLifecycle()
    val homeModules by viewModel.homeModules.collectAsStateWithLifecycle()
    val caregivers by viewModel.caregivers.collectAsStateWithLifecycle()
    val currentCaregiver by viewModel.currentCaregiver.collectAsStateWithLifecycle()
    val autoBackupFrequency by viewModel.autoBackupFrequency.collectAsStateWithLifecycle()
    val exportMessage by viewModel.exportMessage.collectAsStateWithLifecycle()
    val isExporting by viewModel.isExporting.collectAsStateWithLifecycle()
    val recordTab by viewModel.currentRecordTab.collectAsStateWithLifecycle()
    val orderedRecordTabs by viewModel.orderedRecordTabs.collectAsStateWithLifecycle()
    val activeHomeModules by viewModel.activeHomeModules.collectAsStateWithLifecycle()
    val breastfeedingTimer by viewModel.breastfeedingTimer.collectAsStateWithLifecycle()
    val pendingDestination by viewModel.pendingDestination.collectAsStateWithLifecycle()
    val pendingQuickAction by viewModel.pendingRecordQuickAction.collectAsStateWithLifecycle()
    val feedingFormDefaults by viewModel.feedingFormDefaults.collectAsStateWithLifecycle()
    val quickActionNotificationsEnabled by viewModel.quickActionNotificationsEnabled.collectAsStateWithLifecycle()
    val weeklyTrends by viewModel.weeklyTrends.collectAsStateWithLifecycle()
    val routineInsights by viewModel.routineInsights.collectAsStateWithLifecycle()
    val encouragementText by viewModel.encouragementText.collectAsStateWithLifecycle()
    val monthlyGuide by viewModel.monthlyGuide.collectAsStateWithLifecycle()
    val pendingStageReport by viewModel.pendingStageReport.collectAsStateWithLifecycle()
    val memoryOfTheDay by viewModel.memoryOfTheDay.collectAsStateWithLifecycle()
    val nightWakeCount by viewModel.nightWakeCount.collectAsStateWithLifecycle()
    val medicalSummary by viewModel.medicalSummary.collectAsStateWithLifecycle()
    val handoverSummary by viewModel.handoverSummary.collectAsStateWithLifecycle()
    val homeCaregiverFilter by viewModel.homeCaregiverFilter.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val launchState by viewModel.launchState.collectAsStateWithLifecycle()
    var showQuickRecordSheet by rememberSaveable { mutableStateOf(false) }
    var showHandoverSheet by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(pendingDestination) {
        val destination = pendingDestination ?: return@LaunchedEffect
        navController.navigate(destination.route) {
            launchSingleTop = true
            restoreState = true
        }
        viewModel.consumePendingDestination()
    }

    LaunchedEffect(userMessage) {
        val message = userMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.consumeUserMessage()
    }

    when (launchState) {
        AppLaunchState.LOADING -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return
        }

        AppLaunchState.ONBOARDING -> {
            OnboardingScreen(onComplete = viewModel::completeOnboarding)
            return
        }

        AppLaunchState.READY -> Unit
    }

    if (showQuickRecordSheet) {
        QuickRecordSheet(
            viewModel = viewModel,
            feedingFormDefaults = feedingFormDefaults,
            caregivers = caregivers,
            currentCaregiver = currentCaregiver,
            onDismiss = { showQuickRecordSheet = false },
        )
    }

    pendingStageReport?.let { report ->
        StageReportSheet(
            entry = report,
            onDismiss = { viewModel.dismissStageReport(report.day) },
        )
    }

    if (showHandoverSheet) {
        HandoverSummarySheet(
            summary = handoverSummary,
            onGenerate = viewModel::buildHandoverSummary,
            onClear = viewModel::clearHandoverSummary,
            onDismiss = { showHandoverSheet = false },
        )
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            if (currentRoute == AppDestination.HOME.route || currentRoute == AppDestination.RECORDS.route) {
                FloatingActionButton(
                    onClick = { showQuickRecordSheet = true },
                    // removed softShadow
                    shape = androidx.compose.material3.MaterialTheme.shapes.large,
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "添加记录")
                }
            }
        },
        bottomBar = {
            NavigationBar {
                topLevelDestinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.destination.route,
                        onClick = {
                            navController.navigate(destination.destination.route) {
                                popUpTo(AppDestination.HOME.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { androidx.compose.material3.Icon(destination.icon, destination.label) },
                        label = { androidx.compose.material3.Text(destination.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.HOME.route,
            enterTransition = { slideIntoContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = androidx.compose.animation.core.spring()) },
            exitTransition = { slideOutOfContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = androidx.compose.animation.core.spring()) },
            popEnterTransition = { slideIntoContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = androidx.compose.animation.core.spring()) },
            popExitTransition = { slideOutOfContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = androidx.compose.animation.core.spring()) },
        ) {
            composable(AppDestination.HOME.route) {
                HomeScreen(
                    summary = summary,
                    activeModules = activeHomeModules,
                    weeklyTrends = weeklyTrends,
                    routineInsights = routineInsights,
                    encouragementText = encouragementText,
                    monthlyGuide = monthlyGuide,
                    memoryOfTheDay = memoryOfTheDay,
                    vaccines = vaccines,
                    caregivers = caregivers,
                    caregiverFilter = homeCaregiverFilter,
                    contentPadding = innerPadding,
                    onCaregiverFilterChange = viewModel::setHomeCaregiverFilter,
                    onDismissGuide = viewModel::dismissMonthlyGuide,
                    onOpenRecords = { tab ->
                        viewModel.selectRecordTab(tab)
                        navController.navigate(AppDestination.RECORDS.route) {
                            launchSingleTop = true
                        }
                    },
                    onOpenGrowth = {
                        navController.navigate(AppDestination.GROWTH.route) {
                            launchSingleTop = true
                        }
                    },
                    onOpenTimeline = {
                        navController.navigate(AppDestination.TIMELINE.route) {
                            launchSingleTop = true
                        }
                    },
                    onOpenMedicalSummary = {
                        navController.navigate(AppDestination.MEDICAL_SUMMARY.route) {
                            launchSingleTop = true
                        }
                    },
                    onOpenSettings = {
                        navController.navigate(AppDestination.SETTINGS.route) {
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable(AppDestination.RECORDS.route) {
                RecordsScreen(
                    selectedTab = recordTab,
                    orderedTabs = orderedRecordTabs,
                    feedings = feedings,
                    sleeps = sleeps,
                    diapers = diapers,
                    medicalRecords = medicalRecords,
                    activityRecords = activityRecords,
                    caregivers = caregivers,
                    currentCaregiver = currentCaregiver,
                    nightWakeCount = nightWakeCount,
                    breastfeedingTimer = breastfeedingTimer,
                    pendingQuickAction = pendingQuickAction,
                    feedingFormDefaults = feedingFormDefaults,
                    contentPadding = innerPadding,
                    onSelectTab = viewModel::selectRecordTab,
                    onConsumeQuickAction = viewModel::consumePendingRecordQuickAction,
                    onStartBreastfeedingTimer = viewModel::startBreastfeedingTimer,
                    onCancelBreastfeedingTimer = viewModel::cancelBreastfeedingTimer,
                    onSaveBreastfeedingTimer = viewModel::saveBreastfeedingTimer,
                    onAddFeeding = viewModel::addFeeding,
                    onUpdateFeeding = viewModel::updateFeeding,
                    onDeleteFeeding = viewModel::deleteFeeding,
                    onAddSleep = viewModel::addSleep,
                    onUpdateSleep = viewModel::updateSleep,
                    onDeleteSleep = viewModel::deleteSleep,
                    onAddDiaper = viewModel::addDiaper,
                    onUpdateDiaper = viewModel::updateDiaper,
                    onDeleteDiaper = viewModel::deleteDiaper,
                    onAddMedical = viewModel::addMedical,
                    onUpdateMedical = viewModel::updateMedical,
                    onDeleteMedical = viewModel::deleteMedical,
                    onAddActivity = viewModel::addActivity,
                    onUpdateActivity = viewModel::updateActivity,
                    onDeleteActivity = viewModel::deleteActivity,
                    onOpenBatchRecord = { tab ->
                        navController.navigate(batchRecordRoute(tab)) {
                            launchSingleTop = true
                        }
                    },
                    onOpenHandoverSummary = { showHandoverSheet = true },
                )
            }

            composable(AppDestination.GROWTH.route) {
                GrowthScreen(
                    profile = profile,
                    growthRecords = growthRecords,
                    vaccines = vaccines,
                    contentPadding = innerPadding,
                    onAddGrowth = viewModel::addGrowth,
                    onUpdateGrowth = viewModel::updateGrowth,
                    onDeleteGrowth = viewModel::deleteGrowth,
                    onToggleVaccineDone = viewModel::setVaccineStatus,
                    onUpdateVaccineReaction = viewModel::updateVaccineReaction,
                )
            }

            composable(AppDestination.TIMELINE.route) {
                TimelineScreen(
                    profile = profile,
                    feedings = feedings,
                    sleeps = sleeps,
                    diapers = diapers,
                    growthRecords = growthRecords,
                    milestones = milestones,
                    contentPadding = innerPadding,
                    onAddMilestone = viewModel::addMilestone,
                    onUpdateMilestone = viewModel::updateMilestone,
                    onDeleteMilestone = viewModel::deleteMilestone,
                )
            }

            composable(AppDestination.SETTINGS.route) {
                SettingsScreen(
                    profile = profile,
                    themeMode = themeMode,
                    appTheme = appTheme,
                    vaccineRemindersEnabled = vaccineRemindersEnabled,
                    quickActionNotificationsEnabled = quickActionNotificationsEnabled,
                    anomalyRemindersEnabled = anomalyRemindersEnabled,
                    diaperRemindersEnabled = diaperRemindersEnabled,
                    largeTextModeEnabled = largeTextModeEnabled,
                    darkModeScheduleEnabled = darkModeScheduleEnabled,
                    darkModeStartHour = darkModeStartHour,
                    darkModeEndHour = darkModeEndHour,
                    homeModules = homeModules,
                    caregivers = caregivers,
                    currentCaregiver = currentCaregiver,
                    autoBackupFrequency = autoBackupFrequency,
                    exportMessage = exportMessage,
                    isExporting = isExporting,
                    contentPadding = innerPadding,
                    onSaveProfile = viewModel::saveProfile,
                    onThemeModeChange = viewModel::setThemeMode,
                    onAppThemeChange = viewModel::setAppTheme,
                    onVaccineRemindersChange = viewModel::setVaccineRemindersEnabled,
                    onQuickActionNotificationsChange = viewModel::setQuickActionNotificationsEnabled,
                    onAnomalyRemindersChange = viewModel::setAnomalyRemindersEnabled,
                    onDiaperRemindersChange = viewModel::setDiaperRemindersEnabled,
                    onLargeTextModeChange = viewModel::setLargeTextModeEnabled,
                    onDarkModeScheduleChange = viewModel::setDarkModeSchedule,
                    onHomeModulesChange = viewModel::setHomeModules,
                    onCaregiversChange = viewModel::setCaregivers,
                    onCurrentCaregiverChange = viewModel::setCurrentCaregiver,
                    onAutoBackupFrequencyChange = viewModel::setAutoBackupFrequency,
                    onExportCsv = viewModel::exportCsv,
                    onExportPdf = viewModel::exportPdf,
                    onExportBackup = viewModel::exportBackup,
                    onRestoreBackup = viewModel::restoreBackup,
                    onImportCsv = viewModel::importCsv,
                    onOpenMedicalSummary = {
                        navController.navigate(AppDestination.MEDICAL_SUMMARY.route) {
                            launchSingleTop = true
                        }
                    },
                    onClearExportMessage = viewModel::clearExportMessage,
                )
            }

            composable(batchRecordRoutePattern) { backStackEntry ->
                val tab = backStackEntry.arguments?.getString("tab")
                    ?.let { runCatching { RecordTab.valueOf(it) }.getOrNull() }
                    ?: recordTab
                BatchRecordScreen(
                    recordTab = tab,
                    contentPadding = innerPadding,
                    onAddFeeding = viewModel::addFeeding,
                    onAddSleep = viewModel::addSleep,
                    onAddDiaper = viewModel::addDiaper,
                    onAddMedical = viewModel::addMedical,
                    onAddActivity = viewModel::addActivity,
                    onDone = { navController.popBackStack() },
                )
            }

            composable(AppDestination.MEDICAL_SUMMARY.route) {
                MedicalSummaryScreen(
                    summary = medicalSummary,
                    contentPadding = innerPadding,
                    onGenerate = viewModel::buildMedicalSummary,
                )
            }
        }
    }
}

