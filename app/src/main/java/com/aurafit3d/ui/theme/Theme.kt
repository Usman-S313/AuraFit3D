package com.aurafit3d.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.toArgb

/**
 * AuraFit3D — Material 3 dark color scheme.
 * Per SRS FR-8.1: dark theme by default.
 * The system light/dark detection is intentionally ignored — AuraFit is dark-only in v1.0.
 */
private val AuraDarkColorScheme = darkColorScheme(
    primary = AuraAccent,
    onPrimary = AuraOnAccent,
    primaryContainer = AuraAccentContainer,
    onPrimaryContainer = AuraAccent,
    secondary = AuraAccentDark,
    onSecondary = AuraTextPrimary,
    secondaryContainer = AuraSurfaceTint,
    onSecondaryContainer = AuraTextPrimary,
    tertiary = AuraAccent,
    onTertiary = AuraOnAccent,
    background = AuraBackground,
    onBackground = AuraTextPrimary,
    surface = AuraBackground,
    onSurface = AuraTextPrimary,
    surfaceVariant = AuraSurface,
    onSurfaceVariant = AuraTextSecondary,
    surfaceTint = AuraSurfaceTint,
    outline = AuraBorder,
    outlineVariant = AuraBorder,
    error = StatusHigh,
    onError = AuraTextPrimary,
    errorContainer = StatusHigh,
    onErrorContainer = AuraTextPrimary,
)

/**
 * App theme — always dark, always edge-to-edge (per SRS FR-8.1).
 * The `dynamicColor` parameter is intentionally not supported — AuraFit uses a fixed brand palette.
 */
@Composable
fun AuraFitTheme(
    content: @Composable () -> Unit,
) {
    // Always dark — ignore system theme
    val colorScheme = AuraDarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AuraTypography,
        content = content,
    )
}
