package com.aurafit3d.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// AuraFit3D — DM-1 Deep Cyan palette (per SRS visual design)
// Dark theme by default (SRS FR-8.1)
// ============================================================

// Surface colors
val AuraBackground = Color(0xFF0F1419)
val AuraCard = Color(0xFF1A1F2E)
val AuraSurface = Color(0xFF1F2937)
val AuraSurfaceTint = Color(0xFF243044)
val AuraBorder = Color(0xFF2D3748)

// Accent (deep cyan, SRS DM-1 palette)
val AuraAccent = Color(0xFF37DCF2)
val AuraAccentDark = Color(0xFF1B6B7A)
val AuraAccentContainer = Color(0xFF0E3D45)
val AuraOnAccent = Color(0xFF06222A)

// Text
val AuraTextPrimary = Color(0xFFF5F5F5)
val AuraTextSecondary = Color(0xFF9CA3AF)
val AuraTextTertiary = Color(0xFF6B7280)

// Status colors (paired with text labels — never color alone, per SRS NFR-6)
val StatusHealthy = Color(0xFF10B981)
val StatusElevated = Color(0xFFF59E0B)
val StatusHigh = Color(0xFFEF4444)

// Heatmap gradient stops (SRS FR-3.4 — central fat distribution, never visceral)
val HeatmapLow = Color(0xFF10B981)
val HeatmapMid = Color(0xFFF59E0B)
val HeatmapHigh = Color(0xFFEF4444)
