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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aurafit3d.ui.theme.AuraAccent
import com.aurafit3d.ui.theme.AuraCard
import com.aurafit3d.ui.theme.AuraTextPrimary
import com.aurafit3d.ui.theme.AuraTextSecondary
import com.aurafit3d.ui.viewmodel.AppUiState

/**
 * Settings screen — per SRS FR-7 (data controls) and FR-8 (Android platform behavior).
 * Includes:
 *   - Dark theme (locked ON per FR-8.1)
 *   - Backup toggle (default OFF per FR-7.3)
 *   - Units toggle (drives the Input screen metric/imperial switch)
 *   - Accessibility note (informational)
 *   - About section with app version + SRS reference
 */
@Composable
fun SettingsScreen(state: AppUiState) {
    val scrollState = rememberScrollState()
    val form = state.form

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Settings", color = AuraTextPrimary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        // Theme
        SettingsCard(title = "Theme") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Dark (locked)", color = AuraTextSecondary, style = MaterialTheme.typography.bodyMedium)
                Switch(checked = true, onCheckedChange = {}, enabled = false)
            }
        }

        // Units
        SettingsCard(title = "Units") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(if (form.useImperial) "Imperial" else "Metric", color = AuraTextSecondary, style = MaterialTheme.typography.bodyMedium)
                Text("(Toggle on Input screen)", color = AuraTextSecondary, style = MaterialTheme.typography.labelSmall)
            }
        }

        // Backup
        SettingsCard(title = "Backup") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Include in cloud backup", color = AuraTextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text("Default OFF per privacy policy", color = AuraTextSecondary, style = MaterialTheme.typography.labelSmall)
                }
                Switch(checked = false, onCheckedChange = {}, enabled = false)  // FR-7.3 — default OFF, toggle in v2
            }
        }

        // Accessibility
        SettingsCard(title = "Accessibility") {
            Text(
                "TalkBack labels enabled on all interactive elements. Per SRS NFR-6 — content scaling, color contrast, no color-only indicators.",
                color = AuraTextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        // About
        SettingsCard(title = "About") {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Version", color = AuraTextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text("v3.0.0 (2D-First)", color = AuraAccent, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("SRS Reference", color = AuraTextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text("v3.0.0 (2D-First Edition)", color = AuraTextPrimary, style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    "Estimates only. Not medical advice.",
                    color = AuraTextSecondary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AuraCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = AuraTextPrimary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}
