package com.aurafit3d.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aurafit3d.ui.theme.AuraAccent
import com.aurafit3d.ui.theme.AuraTextPrimary
import com.aurafit3d.ui.theme.AuraTextSecondary

/**
 * Measurement slider with numeric input — per SRS FR-1.2.
 * Each input is settable by slider AND numeric keyboard entry.
 * The slider shows the current value badge above it.
 *
 * @param label field label (e.g. "Height", "Weight")
 * @param value current metric value
 * @param range valid range (per SRS Table 4.1)
 * @param unit display unit (e.g. "cm", "kg")
 * @param onValueChange callback when value changes
 * @param errorMessage optional inline error message (FR-1.3)
 */
@Composable
fun MeasurementSlider(
    label: String,
    value: Double,
    range: ClosedFloatingPointRange<Double>,
    unit: String,
    onValueChange: (Double) -> Unit,
    errorMessage: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                color = AuraTextPrimary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "%.1f %s".format(value, unit),
                color = AuraAccent,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toDouble()) },
            valueRange = range.start.toFloat()..range.endInclusive.toFloat(),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        )
        OutlinedTextField(
            value = "%.1f".format(value),
            onValueChange = { s ->
                s.toDoubleOrNull()?.let(onValueChange)
            },
            label = { Text("$label numeric input") },
            trailingIcon = { Text(unit, color = AuraTextSecondary) },
            singleLine = true,
            isError = errorMessage != null,
            supportingText = if (errorMessage != null) {
                { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
