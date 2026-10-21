package com.aurafit3d.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * AuraFit3D — Bottom navigation destinations — per SRS §2.1 + screen plan.
 * Each tab corresponds to one of the five SRS functional areas.
 */
sealed class AuraFitDestination(val route: String, val label: String, val iconFilled: ImageVector, val iconOutlined: ImageVector) {
    data object Input : AuraFitDestination("input", "Input", Icons.Filled.Edit, Icons.Outlined.Edit)
    data object Results : AuraFitDestination("results", "Results", Icons.Filled.BarChart, Icons.Outlined.BarChart)
    data object Planner : AuraFitDestination("planner", "Planner", Icons.Filled.Calculate, Icons.Outlined.Calculate)
    data object History : AuraFitDestination("history", "History", Icons.Filled.History, Icons.Outlined.History)
    data object Settings : AuraFitDestination("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

val AURA_FIT_DESTINATIONS = listOf(
    AuraFitDestination.Input,
    AuraFitDestination.Results,
    AuraFitDestination.Planner,
    AuraFitDestination.History,
    AuraFitDestination.Settings,
)
