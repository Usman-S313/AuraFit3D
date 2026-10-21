package com.aurafit3d.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aurafit3d.data.biometric.BiometricEngine
import com.aurafit3d.data.model.SilhouetteViewMode
import com.aurafit3d.ui.components.BodySilhouette
import com.aurafit3d.ui.components.MetricCard
import com.aurafit3d.ui.theme.AuraCard
import com.aurafit3d.ui.theme.AuraTextPrimary
import com.aurafit3d.ui.theme.AuraTextSecondary
import com.aurafit3d.ui.theme.StatusElevated
import com.aurafit3d.ui.theme.StatusHealthy
import com.aurafit3d.ui.theme.StatusHigh
import com.aurafit3d.ui.viewmodel.AppUiState

/**
 * Results dashboard — per SRS FR-2 and FR-3.
 * Two-column layout (stacks on small screens):
 *   LEFT: 2D body silhouette with view mode toggle + zoom controls
 *   RIGHT: metric cards (BMI, BF%, FFMI, WHtR, BMR, TDEE)
 *
 * Below cards: disclaimer strip (per SRS NFR-9)
 */
@Composable
fun ResultsScreen(
    state: AppUiState,
    onSilhouetteViewModeChange: (SilhouetteViewMode) -> Unit,
) {
    val result = state.result
    val scrollState = rememberScrollState()

    if (result == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "No results yet",
                color = AuraTextSecondary,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                "Go to Input tab and click Compute Results.",
                color = AuraTextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        return
    }

    val sex = state.form.sex
    val bmiBand = BiometricEngine.classifyBmi(result.bmi)
    val bfBand = BiometricEngine.classifyBodyFat(sex, result.bodyFat)
    val whtrStatus = BiometricEngine.classifyWhtr(result.whtr)

    val whtrColor = when (whtrStatus) {
        com.aurafit3d.data.model.WhtrStatus.HEALTHY -> StatusHealthy
        com.aurafit3d.data.model.WhtrStatus.ELEVATED -> StatusElevated
        com.aurafit3d.data.model.WhtrStatus.HIGH -> StatusHigh
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Body Composition",
            color = AuraTextPrimary,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        // View mode toggle (FR-3.3)
        Card(
            colors = CardDefaults.cardColors(containerColor = AuraCard),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("View Mode", color = AuraTextSecondary, style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SilhouetteViewMode.entries.forEach { mode ->
                        FilterChip(
                            selected = state.silhouetteViewMode == mode,
                            onClick = { onSilhouetteViewModeChange(mode) },
                            label = { Text(mode.label) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        // 2D body silhouette (FR-3.1)
        val silhouetteLabel = when (state.silhouetteViewMode) {
            SilhouetteViewMode.SKIN -> null
            SilhouetteViewMode.STRUCTURE -> "Stylized structural reference"
            SilhouetteViewMode.HEATMAP -> "Estimated central fat distribution"
        }
        BodySilhouette(
            fatT = result.fatT.toFloat(),
            waistT = result.waistT.toFloat(),
            whtr = result.whtr.toFloat(),
            viewMode = state.silhouetteViewMode,
            label = silhouetteLabel,
        )

        // Metric cards (FR-2.2)
        MetricCard(
            label = "BMI",
            value = "%.2f".format(result.bmi),
            unit = "kg/m²",
            bandLabel = bmiBand.label,
        )
        MetricCard(
            label = "Body Fat",
            value = "%.2f".format(result.bodyFat),
            unit = "%",
            bandLabel = bfBand.label,
            statusColor = whtrColor,
        )
        MetricCard(
            label = "FFMI (normalized)",
            value = "%.2f".format(result.ffmiNormalized),
            unit = "",
            bandLabel = "Informational",
        )
        MetricCard(
            label = "WHtR",
            value = "%.3f".format(result.whtr),
            unit = "",
            bandLabel = whtrStatus.label,
            statusColor = whtrColor,
        )
        MetricCard(
            label = "BMR",
            value = "%.0f".format(result.bmr),
            unit = "kcal",
        )
        MetricCard(
            label = "TDEE",
            value = "%.0f".format(result.tdee),
            unit = "kcal",
            statusColor = AuraTextSecondary,
        )

        // Disclaimer strip (SRS NFR-9)
        Card(
            colors = CardDefaults.cardColors(containerColor = AuraCard),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "Estimates only. Not medical advice.",
                    color = AuraTextSecondary,
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    "Navy method ±3–4 pp error",
                    color = AuraTextSecondary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}
