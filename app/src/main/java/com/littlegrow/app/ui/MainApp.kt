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
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.littlegrow.app.data.RecordTab
import com.littlegrow.app.ui.screens.GrowthScreen
import com.littlegrow.app.ui.screens.HomeScreen
import com.littlegrow.app.ui.screens.RecordsScreen
import com.littlegrow.app.ui.screens.SettingsScreen
import com.littlegrow.app.ui.screens.TimelineScreen
import com.littlegrow.app.ui.theme.LittleGrowTheme

private enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    Home("home", "首页", Icons.Rounded.Home),
    Records("records", "记录", Icons.Rounded.Today),
    Growth("growth", "成长", Icons.Rounded.AutoGraph),
    Timeline("timeline", "时光", Icons.Rounded.Timeline),
    Settings("settings", "设置", Icons.Rounded.Settings),
}

@Composable
fun LittleGrowApp(
    viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory),
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: TopLevelDestination.Home.route

    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val summary by viewModel.homeSummary.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val feedings by viewModel.feedings.collectAsStateWithLifecycle()
    val sleeps by viewModel.sleeps.collectAsStateWithLifecycle()
    val diapers by viewModel.diapers.collectAsStateWithLifecycle()
    val growthRecords by viewModel.growthRecords.collectAsStateWithLifecycle()
    val milestones by viewModel.milestones.collectAsStateWithLifecycle()
    val vaccines by viewModel.vaccines.collectAsStateWithLifecycle()
    val vaccineRemindersEnabled by viewModel.vaccineRemindersEnabled.collectAsStateWithLifecycle()
    val exportMessage by viewModel.exportMessage.collectAsStateWithLifecycle()
    val isExporting by viewModel.isExporting.collectAsStateWithLifecycle()
    val recordTab by viewModel.currentRecordTab.collectAsStateWithLifecycle()

    LittleGrowTheme(themeMode = themeMode) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(TopLevelDestination.Home.route) { saveState = true }
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
                startDestination = TopLevelDestination.Home.route,
            ) {
                composable(TopLevelDestination.Home.route) {
                    HomeScreen(
                        summary = summary,
                        contentPadding = innerPadding,
                        onOpenRecords = { tab ->
                            viewModel.selectRecordTab(tab)
                            navController.navigate(TopLevelDestination.Records.route) {
                                launchSingleTop = true
                            }
                        },
                        onOpenGrowth = {
                            navController.navigate(TopLevelDestination.Growth.route) {
                                launchSingleTop = true
                            }
                        },
                        onOpenTimeline = {
                            navController.navigate(TopLevelDestination.Timeline.route) {
                                launchSingleTop = true
                            }
                        },
                        onOpenSettings = {
                            navController.navigate(TopLevelDestination.Settings.route) {
                                launchSingleTop = true
                            }
                        },
                    )
                }

                composable(TopLevelDestination.Records.route) {
                    RecordsScreen(
                        selectedTab = recordTab,
                        feedings = feedings,
                        sleeps = sleeps,
                        diapers = diapers,
                        contentPadding = innerPadding,
                        onSelectTab = viewModel::selectRecordTab,
                        onAddFeeding = viewModel::addFeeding,
                        onUpdateFeeding = viewModel::updateFeeding,
                        onDeleteFeeding = viewModel::deleteFeeding,
                        onAddSleep = viewModel::addSleep,
                        onUpdateSleep = viewModel::updateSleep,
                        onDeleteSleep = viewModel::deleteSleep,
                        onAddDiaper = viewModel::addDiaper,
                        onUpdateDiaper = viewModel::updateDiaper,
                        onDeleteDiaper = viewModel::deleteDiaper,
                    )
                }

                composable(TopLevelDestination.Growth.route) {
                    GrowthScreen(
                        growthRecords = growthRecords,
                        vaccines = vaccines,
                        contentPadding = innerPadding,
                        onAddGrowth = viewModel::addGrowth,
                        onUpdateGrowth = viewModel::updateGrowth,
                        onDeleteGrowth = viewModel::deleteGrowth,
                        onToggleVaccineDone = viewModel::setVaccineStatus,
                    )
                }

                composable(TopLevelDestination.Timeline.route) {
                    TimelineScreen(
                        profile = profile,
                        milestones = milestones,
                        contentPadding = innerPadding,
                        onAddMilestone = viewModel::addMilestone,
                        onUpdateMilestone = viewModel::updateMilestone,
                        onDeleteMilestone = viewModel::deleteMilestone,
                    )
                }

                composable(TopLevelDestination.Settings.route) {
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
