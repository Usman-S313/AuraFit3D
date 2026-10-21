package com.aurafit3d.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aurafit3d.ui.theme.AuraBackground
import com.aurafit3d.ui.theme.AuraCard
import com.aurafit3d.ui.theme.AuraSurface
import com.aurafit3d.ui.theme.AuraTextPrimary
import com.aurafit3d.ui.theme.AuraTextSecondary

/**
 * Metric card — displays one biometric metric (label + big number + band label).
 * Per SRS NFR-9: status colors paired with text labels, never color alone.
 *
 * @param label metric name (e.g. "BMI", "Body Fat")
 * @param value formatted value (e.g. "24.69")
 * @param unit unit suffix (e.g. "kg/m²", "%", "kcal")
 * @param bandLabel status band (e.g. "Average", "Healthy")
 * @param statusColor optional status color (paired with bandLabel — never alone)
 */
@Composable
fun MetricCard(
    label: String,
    value: String,
    unit: String,
    bandLabel: String? = null,
    statusColor: Color? = null,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AuraCard),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = AuraTextSecondary,
                    style = MaterialTheme.typography.labelMedium,
                )
                if (bandLabel != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        if (statusColor != null) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(statusColor)
                                    .padding(2.dp)
                            )
                        }
                        Text(
                            text = bandLabel,
                            color = statusColor ?: AuraTextSecondary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        color = AuraTextPrimary,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    if (unit.isNotEmpty()) {
                        Text(
                            text = " $unit",
                            color = AuraTextSecondary,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                        )
                    }
                }
            }
        }
    }
}
