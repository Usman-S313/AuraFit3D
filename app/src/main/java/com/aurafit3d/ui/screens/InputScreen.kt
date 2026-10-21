package com.aurafit3d.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aurafit3d.data.model.ActivityLevel
import com.aurafit3d.data.model.Sex
import com.aurafit3d.ui.components.MeasurementSlider
import com.aurafit3d.ui.theme.AuraAccent
import com.aurafit3d.ui.theme.AuraBackground
import com.aurafit3d.ui.theme.AuraCard
import com.aurafit3d.ui.theme.AuraTextPrimary
import com.aurafit3d.ui.theme.AuraTextSecondary
import com.aurafit3d.ui.viewmodel.AppUiState

/**
 * Input screen — per SRS FR-1.
 * Captures measurements via sliders + numeric entry, with metric/imperial toggle.
 * All inputs validated per Table 4.1 ranges.
 */
@Composable
fun InputScreen(
    state: AppUiState,
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
) {
    val form = state.form
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Title
        Text(
            text = "Capture Measurements",
            color = AuraTextPrimary,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Enter measurements to compute body composition.",
            color = AuraTextSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )

        // Metric / Imperial toggle (FR-1.2)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = !form.useImperial,
                onClick = { onMetricImperialChange(false) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            ) { Text("Metric") }
            SegmentedButton(
                selected = form.useImperial,
                onClick = { onMetricImperialChange(true) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            ) { Text("Imperial") }
        }

        // Sex selector (FR-1.1) — drives Navy formula selection
        Card(
            colors = CardDefaults.cardColors(containerColor = AuraCard),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Biological Sex", color = AuraTextSecondary, style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(Sex.MALE to "Male", Sex.FEMALE to "Female").forEach { (sex, label) ->
                        FilterChip(
                            selected = form.sex == sex,
                            onClick = { onSexChange(sex) },
                            label = { Text(label) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        // Age (15–90, FR-1.3)
        MeasurementSlider(
            label = "Age",
            value = form.age.toDouble(),
            range = 15.0..90.0,
            unit = "yrs",
            onValueChange = { onAgeChange(it.toInt()) },
            errorMessage = state.validationErrors.firstOrNull { it.field == "age" }?.message,
        )

        // Height (120–230 cm)
        MeasurementSlider(
            label = "Height",
            value = form.height,
            range = 120.0..230.0,
            unit = "cm",
            onValueChange = onHeightChange,
            errorMessage = state.validationErrors.firstOrNull { it.field == "height" }?.message,
        )

        // Weight (30–300 kg)
        MeasurementSlider(
            label = "Weight",
            value = form.weight,
            range = 30.0..300.0,
            unit = "kg",
            onValueChange = onWeightChange,
            errorMessage = state.validationErrors.firstOrNull { it.field == "weight" }?.message,
        )

        // Activity level (FR-1.1) — chips for 5 levels
        Card(
            colors = CardDefaults.cardColors(containerColor = AuraCard),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Activity Level", color = AuraTextSecondary, style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                ) {
                    ActivityLevel.entries.forEach { level ->
                        FilterChip(
                            selected = form.activity == level,
                            onClick = { onActivityChange(level) },
                            label = { Text(level.displayLabel, style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }
            }
        }

        // Neck (20–60 cm)
        MeasurementSlider(
            label = "Neck Circumference",
            value = form.neck,
            range = 20.0..60.0,
            unit = "cm",
            onValueChange = onNeckChange,
            errorMessage = state.validationErrors.firstOrNull { it.field == "neck" }?.message,
        )

        // Waist (40–200 cm; must exceed neck for males — FR-1.3)
        MeasurementSlider(
            label = "Waist Circumference",
            value = form.waist,
            range = 40.0..200.0,
            unit = "cm",
            onValueChange = onWaistChange,
            errorMessage = state.validationErrors.firstOrNull { it.field == "waist" }?.message,
        )

        // Hip (50–200 cm; required for female only)
        if (form.sex == Sex.FEMALE) {
            MeasurementSlider(
                label = "Hip Circumference",
                value = form.hip ?: 90.0,
                range = 50.0..200.0,
                unit = "cm",
                onValueChange = { onHipChange(it) },
                errorMessage = state.validationErrors.firstOrNull { it.field == "hip" }?.message,
            )
        }

        // Compute button (sticky at bottom of scroll)
        Button(
            onClick = onCompute,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AuraAccent),
        ) {
            Text("Compute Results", color = AuraBackground, fontWeight = FontWeight.Bold)
        }
    }
}
