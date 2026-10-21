package com.aurafit3d.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aurafit3d.data.model.ActivityLevel
import com.aurafit3d.data.model.MacroGoal
import com.aurafit3d.data.model.Sex
import com.aurafit3d.data.model.SilhouetteViewMode
import com.aurafit3d.ui.screens.HistoryScreen
import com.aurafit3d.ui.screens.InputScreen
import com.aurafit3d.ui.screens.PlannerScreen
import com.aurafit3d.ui.screens.ResultsScreen
import com.aurafit3d.ui.screens.SettingsScreen
import com.aurafit3d.ui.theme.AuraAccent
import com.aurafit3d.ui.theme.AuraBackground
import com.aurafit3d.ui.theme.AuraTextPrimary
import com.aurafit3d.ui.theme.AuraTextSecondary
import com.aurafit3d.ui.viewmodel.AppViewModel
import com.aurafit3d.ui.viewmodel.AppViewModelFactory

/**
 * Root app composable — holds the Scaffold (top app bar + bottom nav + NavHost).
 * Per SRS FR-8.1: Material 3, edge-to-edge, dark theme.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuraFitApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AuraFitDestination.Input.route

    val viewModel: AppViewModel = viewModel(factory = AppViewModelFactory)
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = AuraBackground,
        contentColor = AuraTextPrimary,
        topBar = {
            AuraFitTopAppBar(
                title = "AuraFit 3D",
                subtitle = "v3.0 · 2D-First",
                selfCheckOk = uiState.lastSelfCheckOk,
            )
        },
        bottomBar = {
            AuraFitBottomBar(
                currentRoute = currentRoute,
                onNavigate = { dest ->
                    if (dest.route != currentRoute) {
                        navController.navigate(dest.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AuraFitDestination.Input.route,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            auraFitGraph(
                state = uiState,
                onSexChange = viewModel::onSexChange,
                onMetricImperialChange = viewModel::onMetricImperialChange,
                onAgeChange = viewModel::onAgeChange,
                onHeightChange = viewModel::onHeightChange,
                onWeightChange = viewModel::onWeightChange,
                onNeckChange = viewModel::onNeckChange,
                onWaistChange = viewModel::onWaistChange,
                onHipChange = viewModel::onHipChange,
                onActivityChange = viewModel::onActivityChange,
                onCompute = viewModel::compute,
                onNavigateToResults = { navController.navigate(AuraFitDestination.Results.route) },
                onSilhouetteViewModeChange = viewModel::onSilhouetteViewModeChange,
                onMacroGoalChange = viewModel::onMacroGoalChange,
                onTargetBodyFatChange = viewModel::onTargetBodyFatChange,
                onDeleteAllData = viewModel::deleteAllData,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuraFitTopAppBar(title: String, subtitle: String, selfCheckOk: Boolean) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = AuraTextPrimary,
                )
                Text(
                    text = subtitle,
                    color = AuraTextSecondary,
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                )
            }
        },
        actions = {
            if (selfCheckOk) {
                Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = "Self-check passed",
                    tint = AuraAccent,
                    modifier = Modifier.padding(end = 12.dp),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AuraBackground,
            titleContentColor = AuraTextPrimary,
            actionIconContentColor = AuraAccent,
        ),
    )
}

@Composable
private fun AuraFitBottomBar(
    currentRoute: String,
    onNavigate: (AuraFitDestination) -> Unit,
) {
    NavigationBar(
        containerColor = AuraBackground,
        contentColor = AuraTextPrimary,
        tonalElevation = 0.dp,
    ) {
        AURA_FIT_DESTINATIONS.forEach { dest ->
            val selected = currentRoute == dest.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(dest) },
                icon = {
                    Icon(
                        imageVector = if (selected) dest.iconFilled else dest.iconOutlined,
                        contentDescription = dest.label,
                    )
                },
                label = { Text(dest.label) },
                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                    selectedIconColor = AuraBackground,
                    selectedTextColor = AuraAccent,
                    indicatorColor = AuraAccent,
                    unselectedIconColor = AuraTextSecondary,
                    unselectedTextColor = AuraTextSecondary,
                ),
            )
        }
    }
}

// ---- NavGraph builder ----

private fun NavGraphBuilder.auraFitGraph(
    state: com.aurafit3d.ui.viewmodel.AppUiState,
    onSexChange: (Sex) -> Unit,
    onMetricImperialChange: (Boolean) -> Unit,
    onAgeChange: (Int) -> Unit,
    onHeightChange: (Double) -> Unit,
    onWeightChange: (Double) -> Unit,
    onNeckChange: (Double) -> Unit,
    onWaistChange: (Double) -> Unit,
    onHipChange: (Double?) -> Unit,
    onActivityChange: (ActivityLevel) -> Unit,
    onCompute: () -> Unit,
    onNavigateToResults: () -> Unit,
    onSilhouetteViewModeChange: (SilhouetteViewMode) -> Unit,
    onMacroGoalChange: (MacroGoal) -> Unit,
    onTargetBodyFatChange: (Double) -> Unit,
    onDeleteAllData: () -> Unit,
) {
    composable(AuraFitDestination.Input.route) {
        InputScreen(
            state = state,
            onSexChange = onSexChange,
            onMetricImperialChange = onMetricImperialChange,
            onAgeChange = onAgeChange,
            onHeightChange = onHeightChange,
            onWeightChange = onWeightChange,
            onNeckChange = onNeckChange,
            onWaistChange = onWaistChange,
            onHipChange = onHipChange,
            onActivityChange = onActivityChange,
            onCompute = {
                onCompute()
                onNavigateToResults()
            },
        )
    }
    composable(AuraFitDestination.Results.route) {
        ResultsScreen(
            state = state,
            onSilhouetteViewModeChange = onSilhouetteViewModeChange,
        )
    }
    composable(AuraFitDestination.Planner.route) {
        PlannerScreen(
            state = state,
            onMacroGoalChange = onMacroGoalChange,
            onTargetBodyFatChange = onTargetBodyFatChange,
        )
    }
    composable(AuraFitDestination.History.route) {
        HistoryScreen(
            state = state,
            onDeleteAllData = onDeleteAllData,
        )
    }
    composable(AuraFitDestination.Settings.route) {
        SettingsScreen(state = state)
    }
}
