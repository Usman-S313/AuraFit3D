package com.aurafit3d.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aurafit3d.data.model.MacroGoal
import com.aurafit3d.data.model.SilhouetteViewMode
import com.aurafit3d.ui.components.BodySilhouette
import com.aurafit3d.ui.components.MetricCard
import com.aurafit3d.ui.theme.AuraAccent
import com.aurafit3d.ui.theme.AuraCard
import com.aurafit3d.ui.theme.AuraTextPrimary
import com.aurafit3d.ui.theme.AuraTextSecondary
import com.aurafit3d.ui.theme.StatusElevated
import com.aurafit3d.ui.viewmodel.AppUiState

/**
 * Planner screen — per SRS FR-4 (calorie/macro planner) and FR-5 (goal projection).
 * Three sections stacked:
 *   A) Calorie & Macro Planner — goal selector + 2x2 metric grid
 *   B) Goal Projection — target BF% slider + projected weeks + assumptions
 *   C) Side-by-side 2D before/after silhouettes (FR-5.2)
 */
@Composable
fun PlannerScreen(
    state: AppUiState,
    onMacroGoalChange: (MacroGoal) -> Unit,
    onTargetBodyFatChange: (Double) -> Unit,
) {
    val result = state.result
    val macroPlan = state.macroPlan
    val projection = state.projection
    val scrollState = rememberScrollState()

    if (result == null || macroPlan == null || projection == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("No data yet", color = AuraTextSecondary, style = MaterialTheme.typography.titleMedium)
            Text("Compute results on the Input tab first.", color = AuraTextSecondary, style = MaterialTheme.typography.bodySmall)
        }
        return
    }

    // Per SRS FR-5.1 — target must be above Essential-band floor for sex
    val targetMin = if (state.form.sex == com.aurafit3d.data.model.Sex.MALE) 6.0 else 14.0
    val targetMax = maxOf(result.bodyFat, targetMin + 1.0)

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Calorie & Goal Planner",
            color = AuraTextPrimary,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        // ===== Section A: Calorie & Macro Planner (FR-4) =====
        Card(
            colors = CardDefaults.cardColors(containerColor = AuraCard),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Goal", color = AuraTextSecondary, style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MacroGoal.entries.forEach { goal ->
                        FilterChip(
                            selected = state.macroGoal == goal,
                            onClick = { onMacroGoalChange(goal) },
                            label = { Text(goal.label, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // Floor notice (FR-4.3)
                if (macroPlan.floorApplied) {
                    Text(
                        "⚠ Calorie floor applied: minimum safe intake",
                        color = StatusElevated,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                if (macroPlan.carbsClamped) {
                    Text(
                        "⚠ Carbs set to 0; protein reduced to fit calorie budget",
                        color = StatusElevated,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }

                // 2x2 grid of macros
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MacroCell(
                        label = "Daily Calories",
                        value = macroPlan.calories.toString(),
                        unit = "kcal",
                        modifier = Modifier.weight(1f),
                        highlight = true,
                    )
                    MacroCell(
                        label = "Protein",
                        value = macroPlan.protein.toString(),
                        unit = "g",
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MacroCell(
                        label = "Fat",
                        value = macroPlan.fat.toString(),
                        unit = "g",
                        modifier = Modifier.weight(1f),
                    )
                    MacroCell(
                        label = "Carbs",
                        value = macroPlan.carbs.toString(),
                        unit = "g",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // ===== Section B: Goal Projection (FR-5) =====
        Card(
            colors = CardDefaults.cardColors(containerColor = AuraCard),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Goal Projection", color = AuraTextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                Text("Target Body Fat %: %.1f%%".format(state.targetBodyFat), color = AuraAccent, style = MaterialTheme.typography.titleSmall)
                Slider(
                    value = state.targetBodyFat.toFloat(),
                    onValueChange = { onTargetBodyFatChange(it.toDouble()) },
                    valueRange = targetMin.toFloat()..targetMax.toFloat(),
                )

                if (projection.alreadyAtOrBelowTarget) {
                    Text(
                        "Already at or below target",
                        color = StatusElevated,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                } else {
                    Text(
                        "Projected weeks to goal: %.1f weeks".format(projection.weeks),
                        color = AuraTextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Target weight: %.2f kg  ·  Fat to lose: %.2f kg".format(projection.targetWeight, projection.fatToLose),
                        color = AuraTextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                // Assumptions (FR-5.3)
                Text("Assumptions", color = AuraTextSecondary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 4.dp))
                val assumptions = listOf(
                    "Constant lean mass; all change is fat mass",
                    "7700 kcal per kg of fat",
                    "500 kcal/day deficit",
                    "Navy method ±3–4 pp error",
                )
                assumptions.forEach { a ->
                    Text("• $a", color = AuraTextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // ===== Section C: Side-by-side 2D before/after (FR-5.2) =====
        Card(
            colors = CardDefaults.cardColors(containerColor = AuraCard),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Current vs Target", color = AuraTextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Current silhouette
                    Box(modifier = Modifier.weight(1f)) {
                        Column {
                            Text("Now", color = AuraTextSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 4.dp))
                            BodySilhouette(
                                fatT = result.fatT.toFloat(),
                                waistT = result.waistT.toFloat(),
                                whtr = result.whtr.toFloat(),
                                viewMode = SilhouetteViewMode.SKIN,
                            )
                        }
                    }
                    // Target silhouette — recompute target fatT
                    val targetFatT = ((state.targetBodyFat - 8.0) / 32.0).coerceIn(0.0, 1.0)
                    Box(modifier = Modifier.weight(1f)) {
                        Column {
                            Text("Goal (%.1f weeks)".format(projection.weeks), color = AuraTextSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 4.dp))
                            BodySilhouette(
                                fatT = targetFatT.toFloat(),
                                waistT = result.waistT.toFloat(),   // waist roughly same (lean mass preserved per assumption)
                                whtr = result.whtr.toFloat(),
                                viewMode = SilhouetteViewMode.SKIN,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroCell(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (highlight) AuraAccent.copy(alpha = 0.15f) else AuraCard),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(label, color = AuraTextSecondary, style = MaterialTheme.typography.labelSmall)
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 2.dp)) {
                Text(
                    value,
                    color = if (highlight) AuraAccent else AuraTextPrimary,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                if (unit.isNotEmpty()) Text(" $unit", color = AuraTextSecondary, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

// Import required for Card alpha
private val AuraAccent = com.aurafit3d.ui.theme.AuraAccent
