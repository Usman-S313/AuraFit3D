package com.aurafit3d

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aurafit3d.ui.navigation.AuraFitApp
import com.aurafit3d.ui.theme.AuraFitTheme

/**
 * Single-Activity entry point — per SRS §2.1.
 *
 * All UI is rendered through Jetpack Compose; the activity itself is a thin shell
 * that hosts the Compose tree. Navigation between screens is handled by
 * Compose Navigation inside AuraFitApp (see ui/navigation/AppNavigation.kt).
 *
 * Per SRS FR-8.1: edge-to-edge layout, dark theme by default.
 * Per SRS FR-8.2: portrait is primary; landscape is supported (no orientation lock).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AuraFitTheme {
                AuraFitHost()
            }
        }
    }
}

@Composable
private fun AuraFitHost() {
    Surface(modifier = Modifier.fillMaxSize()) {
        AuraFitApp()
    }
}
