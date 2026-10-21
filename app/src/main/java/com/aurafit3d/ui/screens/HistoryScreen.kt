package com.aurafit3d.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aurafit3d.ui.theme.AuraCard
import com.aurafit3d.ui.theme.AuraTextPrimary
import com.aurafit3d.ui.theme.AuraTextSecondary
import com.aurafit3d.ui.theme.StatusHigh
import com.aurafit3d.ui.viewmodel.AppUiState
import com.aurafit3d.ui.viewmodel.HistoryEntry

/**
 * History screen — per SRS FR-7.
 * Shows past entries with date, body fat %, weight, and +/- change from previous.
 * Delete-all-data control with confirmation dialog (FR-7.2).
 */
@Composable
fun HistoryScreen(
    state: AppUiState,
    onDeleteAllData: () -> Unit,
) {
    val history = state.history
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Result History", color = AuraTextPrimary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("${history.size} entries", color = AuraTextSecondary, style = MaterialTheme.typography.labelSmall)
            }
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                enabled = history.isNotEmpty(),
            ) { Text("Delete all", color = StatusHigh) }
        }

        if (history.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No history yet", color = AuraTextSecondary, style = MaterialTheme.typography.titleMedium)
                    Text("Compute your first result on the Input tab.", color = AuraTextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(history) { entry ->
                    HistoryEntryCard(
                        entry = entry,
                        previous = history.firstOrNull { it.id == entry.id - 1 },
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete all data?") },
            text = { Text("This will permanently remove your profile, history, and cached state. This action is irreversible.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteAllData()
                    showDeleteDialog = false
                }) { Text("Delete", color = StatusHigh) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun HistoryEntryCard(entry: HistoryEntry, previous: HistoryEntry?) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AuraCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(entry.date, color = AuraTextPrimary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("BMI %.2f".format(entry.bmi), color = AuraTextSecondary, style = MaterialTheme.typography.labelSmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("Body Fat", color = AuraTextSecondary, style = MaterialTheme.typography.labelSmall)
                    Text("%.2f%%".format(entry.bodyFat), color = AuraTextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Weight", color = AuraTextSecondary, style = MaterialTheme.typography.labelSmall)
                    Text("%.1f kg".format(entry.weight), color = AuraTextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                if (previous != null) {
                    val delta = entry.bodyFat - previous.bodyFat
                    val sign = if (delta >= 0) "+" else ""
                    Text(
                        "$sign%.2f".format(delta),
                        color = if (delta < 0) StatusHigh else AuraTextSecondary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
