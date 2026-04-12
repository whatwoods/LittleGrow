package com.littlegrow.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BabyChangingStation
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material.icons.rounded.Today
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import com.littlegrow.app.ui.components.GlassSurface
import com.littlegrow.app.ui.components.LocalGlassHazeState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailState
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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
import com.littlegrow.app.ui.components.ExpressiveFloatingActionButton as FloatingActionButton
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch

private data class FabMenuItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

private data class TopLevelDestination(
    val destination: AppDestination,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val topLevelDestinations = listOf(
    TopLevelDestination(AppDestination.HOME, "首页", Icons.Rounded.Home, Icons.Outlined.Home),
    TopLevelDestination(AppDestination.RECORDS, "记录", Icons.Rounded.Today, Icons.Outlined.Today),
    TopLevelDestination(AppDestination.GROWTH, "成长", Icons.Rounded.AutoGraph, Icons.Outlined.AutoGraph),
    TopLevelDestination(AppDestination.TIMELINE, "时光", Icons.Rounded.Timeline, Icons.Outlined.Timeline),
)

private val allDestinationsIncludingSettings = topLevelDestinations + listOf(
    TopLevelDestination(AppDestination.SETTINGS, "设置", Icons.Rounded.Settings, Icons.Outlined.Settings),
)

private const val batchRecordRoutePattern = "batch_records/{tab}"
private val mobileBottomBarReservedHeight = 108.dp
private val mobileTopBarReservedHeight = 72.dp
private val mobileFabBottomOffset = 90.dp

private fun batchRecordRoute(tab: RecordTab): String = "${AppDestination.BATCH_RECORDS.route}/${tab.name}"

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LittleGrowApp(
    viewModel: MainViewModel,
    themeMode: ThemeMode,
    appTheme: AppTheme,
) {
    val configuration = LocalConfiguration.current
    val navController = rememberNavController()
    val hazeState = rememberHazeState()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: AppDestination.HOME.route
    val useNavigationRail = configuration.screenWidthDp >= 600
    val useExpandedRail = configuration.screenWidthDp >= 840
    val railState = rememberWideNavigationRailState(
        initialValue = if (useExpandedRail) WideNavigationRailValue.Expanded else WideNavigationRailValue.Collapsed,
    )

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
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
    var showQuickRecordSheet by rememberSaveable { mutableStateOf(false) }
    var quickRecordInitialTab by rememberSaveable { mutableStateOf<RecordTab?>(null) }
    var showAddGrowthDialog by rememberSaveable { mutableStateOf(false) }
    var showAddMilestoneDialog by rememberSaveable { mutableStateOf(false) }
    var showHandoverSheet by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val showQuickRecordAction = currentRoute == AppDestination.HOME.route || currentRoute == AppDestination.RECORDS.route

    fun onQuickRecord(tab: RecordTab) {
        quickRecordInitialTab = tab
        showQuickRecordSheet = true
    }

    fun navigateToTopLevel(destination: AppDestination) {
        navController.navigate(destination.route) {
            popUpTo(AppDestination.HOME.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

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

    LaunchedEffect(useNavigationRail, useExpandedRail) {
        if (!useNavigationRail) return@LaunchedEffect
        railState.snapTo(
            if (useExpandedRail) WideNavigationRailValue.Expanded else WideNavigationRailValue.Collapsed,
        )
    }

    when (launchState) {
        AppLaunchState.LOADING -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                LoadingIndicator()
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
            initialSelectedTab = quickRecordInitialTab,
            onDismiss = {
                showQuickRecordSheet = false
                quickRecordInitialTab = null
            },
        )
    }

    if (showAddGrowthDialog) {
        com.littlegrow.app.ui.screens.AddGrowthDialog(
            initial = null,
            onDismiss = { showAddGrowthDialog = false },
            onSubmit = { draft ->
                viewModel.addGrowth(draft)
                showAddGrowthDialog = false
            },
        )
    }

    if (showAddMilestoneDialog) {
        com.littlegrow.app.ui.screens.AddMilestoneDialog(
            initial = null,
            onDismiss = { showAddMilestoneDialog = false },
            onSubmit = { draft ->
                viewModel.addMilestone(draft)
                showAddMilestoneDialog = false
            },
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

    CompositionLocalProvider(LocalGlassHazeState provides hazeState) {
        Box(modifier = Modifier.fillMaxSize()) {
            AppBackdrop(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(hazeState),
            )

            Scaffold(
                containerColor = Color.Transparent,
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState)
                },
                floatingActionButton = {
                    if (!useNavigationRail) {
                        var fabExpanded by rememberSaveable { mutableStateOf(false) }

                        val menuItems: List<FabMenuItem> = when (currentRoute) {
                            AppDestination.HOME.route,
                            AppDestination.RECORDS.route -> listOf(
                                FabMenuItem("记录喂奶", Icons.Rounded.LocalDrink) { onQuickRecord(RecordTab.FEEDING) },
                                FabMenuItem("记录睡眠", Icons.Rounded.Bedtime) { onQuickRecord(RecordTab.SLEEP) },
                                FabMenuItem("记录尿布", Icons.Rounded.BabyChangingStation) { onQuickRecord(RecordTab.DIAPER) },
                                FabMenuItem("健康记录", Icons.Rounded.MedicalServices) { onQuickRecord(RecordTab.MEDICAL) },
                                FabMenuItem("记录活动", Icons.Rounded.DirectionsRun) { onQuickRecord(RecordTab.ACTIVITY) },
                            )
                            AppDestination.GROWTH.route -> listOf(
                                FabMenuItem("添加生长记录", Icons.Rounded.Straighten) { showAddGrowthDialog = true },
                            )
                            AppDestination.TIMELINE.route -> listOf(
                                FabMenuItem("添加里程碑", Icons.Rounded.EmojiEvents) { showAddMilestoneDialog = true },
                            )
                            else -> emptyList()
                        }

                        val fabVisible = menuItems.isNotEmpty()

                        LaunchedEffect(currentRoute) { fabExpanded = false }

                        FloatingActionButtonMenu(
                            modifier = Modifier.animateFloatingActionButton(
                                visible = fabVisible,
                                alignment = Alignment.BottomEnd,
                            )
                                .padding(bottom = mobileFabBottomOffset),
                            expanded = fabExpanded,
                            button = {
                                ToggleFloatingActionButton(
                                    checked = fabExpanded,
                                    onCheckedChange = { fabExpanded = !fabExpanded },
                                ) {
                                    val iconRotation by animateFloatAsState(
                                        targetValue = if (fabExpanded) 45f else 0f,
                                        label = "fab_rotation"
                                    )
                                    Icon(
                                        imageVector = if (fabExpanded) Icons.Rounded.Add else Icons.Rounded.Add,
                                        contentDescription = if (fabExpanded) "收起" else "添加",
                                        modifier = Modifier.graphicsLayer(rotationZ = iconRotation)
                                    )
                                }
                            }
                        ) {
                            menuItems.forEach { item ->
                                FloatingActionButtonMenuItem(
                                    onClick = {
                                        fabExpanded = false
                                        item.onClick()
                                    },
                                    icon = { Icon(item.icon, contentDescription = null) },
                                    text = { Text(item.label) }
                                )
                            }
                        }
                    }
                },
            ) { innerPadding ->
                val layoutDirection = LocalLayoutDirection.current
                val contentPadding = PaddingValues(
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    top = innerPadding.calculateTopPadding() +
                        if (useNavigationRail) 0.dp else mobileTopBarReservedHeight,
                    end = innerPadding.calculateEndPadding(layoutDirection),
                    bottom = innerPadding.calculateBottomPadding() +
                        if (useNavigationRail) 0.dp else mobileBottomBarReservedHeight,
                )
                Row(modifier = Modifier.fillMaxSize()) {
                    if (useNavigationRail) {
                        TopLevelWideNavigationRail(
                            currentRoute = currentRoute,
                            railState = railState,
                            showQuickRecordAction = showQuickRecordAction,
                            onQuickRecord = { showQuickRecordSheet = true },
                            onNavigate = ::navigateToTopLevel,
                        )
                    }

                    Box(modifier = Modifier.weight(1f).hazeSource(hazeState)) {
                        val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<androidx.compose.ui.unit.IntOffset>()
                        NavHost(
                            navController = navController,
                            startDestination = AppDestination.HOME.route,
                            enterTransition = { slideIntoContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = spatialSpec) },
                            exitTransition = { slideOutOfContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = spatialSpec) },
                            popEnterTransition = { slideIntoContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = spatialSpec) },
                            popExitTransition = { slideOutOfContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = spatialSpec) },
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
                    refreshing = refreshing,
                    contentPadding = contentPadding,
                    onRefresh = viewModel::refresh,
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
                    refreshing = refreshing,
                    contentPadding = contentPadding,
                    onRefresh = viewModel::refresh,
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
                    refreshing = refreshing,
                    contentPadding = contentPadding,
                    onRefresh = viewModel::refresh,
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
                    refreshing = refreshing,
                    contentPadding = contentPadding,
                    onRefresh = viewModel::refresh,
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
                    contentPadding = contentPadding,
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
                    contentPadding = contentPadding,
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
                    contentPadding = contentPadding,
                    onGenerate = viewModel::buildMedicalSummary,
                )
            }
                        }
                    }
                }
            }

            if (!useNavigationRail) {
                GlassTopAppBar(
                    modifier = Modifier.align(Alignment.TopCenter),
                    onSettingsClick = { navigateToTopLevel(AppDestination.SETTINGS) },
                )
                TopLevelShortNavigationBar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    currentRoute = currentRoute,
                    onNavigate = ::navigateToTopLevel,
                )
            }
        }
    }
}

@Composable
private fun AppBackdrop(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier.drawWithCache {
            val stripeWidth = 1.5.dp.toPx()
            val stripeGap = 72.dp.toPx()
            val stripeTilt = size.height * 0.34f
            val dotRadius = size.minDimension * 0.018f
            val baseGradient = Brush.verticalGradient(
                colors = listOf(
                    colorScheme.background,
                    colorScheme.surface.copy(alpha = 0.9f),
                    colorScheme.surfaceContainerLowest.copy(alpha = 0.84f),
                ),
            )
            val prismWash = Brush.linearGradient(
                colors = listOf(
                    colorScheme.tertiary.copy(alpha = 0.12f),
                    Color.Transparent,
                    colorScheme.primary.copy(alpha = 0.14f),
                ),
                start = Offset(0f, size.height),
                end = Offset(size.width, 0f),
            )
            val topHalo = Brush.radialGradient(
                colors = listOf(
                    colorScheme.primary.copy(alpha = 0.28f),
                    Color.Transparent,
                ),
                center = Offset(size.width * 0.15f, size.height * 0.1f),
                radius = size.minDimension * 0.46f,
            )
            val middleGlow = Brush.radialGradient(
                colors = listOf(
                    colorScheme.secondary.copy(alpha = 0.18f),
                    Color.Transparent,
                ),
                center = Offset(size.width * 0.82f, size.height * 0.42f),
                radius = size.minDimension * 0.4f,
            )
            val bottomGlow = Brush.radialGradient(
                colors = listOf(
                    colorScheme.tertiary.copy(alpha = 0.22f),
                    Color.Transparent,
                ),
                center = Offset(size.width * 0.45f, size.height * 0.92f),
                radius = size.minDimension * 0.42f,
            )
            val lightBand = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.18f),
                    Color.Transparent,
                ),
                start = Offset(size.width * 0.02f, size.height * 0.08f),
                end = Offset(size.width * 0.9f, size.height * 0.64f),
            )
            onDrawBehind {
                drawRect(brush = baseGradient)
                drawRect(brush = prismWash)
                drawRect(brush = topHalo)
                drawRect(brush = middleGlow)
                drawRect(brush = bottomGlow)
                var stripeX = -stripeTilt
                while (stripeX < size.width + stripeTilt) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(stripeX, 0f),
                        end = Offset(stripeX + stripeTilt, size.height),
                        strokeWidth = stripeWidth,
                    )
                    stripeX += stripeGap
                }
                drawCircle(
                    color = colorScheme.primary.copy(alpha = 0.14f),
                    radius = dotRadius * 2.8f,
                    center = Offset(size.width * 0.18f, size.height * 0.3f),
                )
                drawCircle(
                    color = colorScheme.secondary.copy(alpha = 0.12f),
                    radius = dotRadius * 2.2f,
                    center = Offset(size.width * 0.74f, size.height * 0.26f),
                )
                drawCircle(
                    color = colorScheme.tertiary.copy(alpha = 0.14f),
                    radius = dotRadius * 3.1f,
                    center = Offset(size.width * 0.58f, size.height * 0.76f),
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.1f),
                    radius = dotRadius * 1.6f,
                    center = Offset(size.width * 0.84f, size.height * 0.68f),
                )
                drawRect(brush = lightBand)
            }
        },
    )
}

@Composable
private fun TopLevelShortNavigationBar(
    modifier: Modifier = Modifier,
    currentRoute: String,
    onNavigate: (AppDestination) -> Unit,
) {
    val shape = RoundedCornerShape(30.dp)
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .softShadow(
                elevation = 20.dp,
                shape = shape,
                color = Color.Black.copy(alpha = 0.06f),
            ),
        shape = shape,
        color = colorScheme.surface.copy(alpha = 0.94f),
        border = BorderStroke(0.8.dp, colorScheme.outlineVariant.copy(alpha = 0.22f)),
        tonalElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.drawWithCache {
                val softTint = Brush.linearGradient(
                    colors = listOf(
                        colorScheme.primary.copy(alpha = 0.05f),
                        colorScheme.surface.copy(alpha = 0.02f),
                        colorScheme.secondary.copy(alpha = 0.04f),
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height),
                )
                val topSheen = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.16f),
                        Color.Transparent,
                    ),
                    startY = 0f,
                    endY = size.height * 0.45f,
                )
                onDrawBehind {
                    drawRect(brush = softTint)
                    drawRect(brush = topSheen)
                }
            },
        ) {
            NavigationBar(
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets(0, 0, 0, 0),
            ) {
                topLevelDestinations.forEach { destination ->
                    val selected = currentRoute == destination.destination.route
                    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            onNavigate(destination.destination)
                        },
                        icon = {
                            androidx.compose.animation.Crossfade(
                                targetState = selected,
                                label = "Icon Crossfade"
                            ) { isSelected ->
                                Icon(
                                    if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = null
                                )
                            }
                        },
                        label = { Text(destination.label) },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = colorScheme.primary.copy(alpha = 0.14f),
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun TopLevelWideNavigationRail(
    currentRoute: String,
    railState: WideNavigationRailState,
    showQuickRecordAction: Boolean,
    onQuickRecord: () -> Unit,
    onNavigate: (AppDestination) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val railExpanded = railState.targetValue == WideNavigationRailValue.Expanded

    WideNavigationRail(
        modifier = Modifier.fillMaxHeight(),
        state = railState,
        header = {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                IconButton(
                    onClick = { scope.launch { railState.toggle() } },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Menu,
                        contentDescription = if (railExpanded) "收起导航" else "展开导航",
                    )
                }

                if (showQuickRecordAction) {
                    if (railExpanded) {
                        ExtendedFloatingActionButton(
                            text = { Text("添加记录") },
                            icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                            onClick = onQuickRecord,
                        )
                    } else {
                        FloatingActionButton(onClick = onQuickRecord) {
                            Icon(Icons.Rounded.Add, contentDescription = "添加记录")
                        }
                    }
                }
            }
        },
    ) {
        allDestinationsIncludingSettings.forEach { destination ->
            val selected = currentRoute == destination.destination.route
            val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
            WideNavigationRailItem(
                railExpanded = railExpanded,
                selected = selected,
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    onNavigate(destination.destination)
                },
                icon = {
                    androidx.compose.animation.Crossfade(
                        targetState = selected,
                        label = "Icon Crossfade"
                    ) { isSelected ->
                        Icon(
                            if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                            contentDescription = null
                        )
                    }
                },
                label = { Text(destination.label) },
            )
        }
    }
}

@Composable
private fun GlassTopAppBar(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit,
) {
    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp),
        alpha = 0.88f,
        shape = RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp),
        accentColor = MaterialTheme.colorScheme.primary,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 12.dp,
                    top = 40.dp,
                    bottom = 12.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "长呀长",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "设置",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
