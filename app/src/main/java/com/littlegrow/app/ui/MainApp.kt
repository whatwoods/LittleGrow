package com.littlegrow.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.littlegrow.app.ui.screens.GrowthScreen
import com.littlegrow.app.ui.screens.HomeScreen
import com.littlegrow.app.ui.screens.OnboardingScreen
import com.littlegrow.app.ui.screens.RecordsScreen
import com.littlegrow.app.ui.screens.SettingsScreen
import com.littlegrow.app.ui.screens.TimelineScreen
import com.littlegrow.app.ui.theme.LittleGrowTheme

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

@Composable
fun LittleGrowApp(
    viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory),
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: AppDestination.HOME.route

    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
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
    val exportMessage by viewModel.exportMessage.collectAsStateWithLifecycle()
    val isExporting by viewModel.isExporting.collectAsStateWithLifecycle()
    val recordTab by viewModel.currentRecordTab.collectAsStateWithLifecycle()
    val breastfeedingTimer by viewModel.breastfeedingTimer.collectAsStateWithLifecycle()
    val pendingDestination by viewModel.pendingDestination.collectAsStateWithLifecycle()
    val pendingQuickAction by viewModel.pendingRecordQuickAction.collectAsStateWithLifecycle()
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsStateWithLifecycle()

    LaunchedEffect(pendingDestination) {
        val destination = pendingDestination ?: return@LaunchedEffect
        navController.navigate(destination.route) {
            launchSingleTop = true
            restoreState = true
        }
        viewModel.consumePendingDestination()
    }

    LittleGrowTheme(themeMode = themeMode) {
        if (!onboardingCompleted) {
            OnboardingScreen(
                onComplete = { profile -> viewModel.completeOnboarding(profile) },
            )
            return@LittleGrowTheme
        }

        Scaffold(
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
            ) {
                composable(AppDestination.HOME.route) {
                    HomeScreen(
                        summary = summary,
                        contentPadding = innerPadding,
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
                        feedings = feedings,
                        sleeps = sleeps,
                        diapers = diapers,
                        medicalRecords = medicalRecords,
                        activityRecords = activityRecords,
                        breastfeedingTimer = breastfeedingTimer,
                        pendingQuickAction = pendingQuickAction,
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
                    )
                }

                composable(AppDestination.TIMELINE.route) {
                    TimelineScreen(
                        profile = profile,
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
                        vaccineRemindersEnabled = vaccineRemindersEnabled,
                        exportMessage = exportMessage,
                        isExporting = isExporting,
                        contentPadding = innerPadding,
                        onSaveProfile = viewModel::saveProfile,
                        onThemeModeChange = viewModel::setThemeMode,
                        onVaccineRemindersChange = viewModel::setVaccineRemindersEnabled,
                        onExportCsv = viewModel::exportCsv,
                        onExportPdf = viewModel::exportPdf,
                        onClearExportMessage = viewModel::clearExportMessage,
                    )
                }
            }
        }
    }
}
