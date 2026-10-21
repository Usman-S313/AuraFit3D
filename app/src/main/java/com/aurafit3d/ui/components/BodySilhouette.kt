package com.aurafit3d.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aurafit3d.data.model.SilhouetteViewMode
import com.aurafit3d.ui.theme.AuraAccent
import com.aurafit3d.ui.theme.AuraAccentDark
import com.aurafit3d.ui.theme.AuraBackground
import com.aurafit3d.ui.theme.AuraBorder
import com.aurafit3d.ui.theme.AuraCard
import com.aurafit3d.ui.theme.AuraTextSecondary
import com.aurafit3d.ui.theme.HeatmapHigh
import com.aurafit3d.ui.theme.HeatmapLow
import com.aurafit3d.ui.theme.HeatmapMid
import com.aurafit3d.ui.theme.StatusElevated
import com.aurafit3d.ui.theme.StatusHealthy
import com.aurafit3d.ui.theme.StatusHigh
import kotlin.math.cos
import kotlin.math.sin

/**
 * Parametric 2D body silhouette — per SRS FR-3.
 *
 * Drawn on Android Canvas via Compose. Deforms in real time as fatT and waistT change,
 * per SRS §5.5 mapping.
 *
 * Three view modes (FR-3.3):
 *   - SKIN: filled silhouette in accent color with subtle gradient
 *   - STRUCTURE: wireframe outline + stylized skeletal reference points
 *   - HEATMAP: radial gradient overlay on waist region, colored by WHtR band
 *
 * Gestures (FR-3.2):
 *   - Pinch zoom (0.5x to 3x)
 *   - One-finger drag pan (clamped so silhouette stays visible)
 *   - Reset button restores defaults
 *
 * Silhouette math (SRS §5.5):
 *   chestHalfWidth  = baseChest  + 0.20 × fatT × baseChest
 *   waistHalfWidth  = baseWaist + 0.55 × waistT × baseWaist + 0.10 × fatT × baseWaist
 *   hipHalfWidth    = baseHip   + 0.35 × fatT × baseHip + 0.05 × waistT × baseHip
 */
@Composable
fun BodySilhouette(
    fatT: Float,
    waistT: Float,
    whtr: Float,
    viewMode: SilhouetteViewMode,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    // Zoom + pan state (FR-3.2)
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AuraCard)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(0.5f, 3f)
                        scale = newScale
                        // Clamp pan so silhouette stays within view
                        offsetX = (offsetX + pan.x).coerceIn(-150f, 150f)
                        offsetY = (offsetY + pan.y).coerceIn(-150f, 150f)
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height

                translate(left = canvasW / 2 + offsetX, top = 80f + offsetY) {
                    scale(scale, pivot = Offset(0f, 0f)) {
                        drawSilhouette(
                            fatT = fatT,
                            waistT = waistT,
                            viewMode = viewMode,
                            whtr = whtr,
                        )
                    }
                }
            }
        }

        // Zoom controls (FR-3.2 reset)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            listOf(0.5f to "0.5×", 1f to "1×", 2f to "2×").forEach { (s, label) ->
                OutlinedButton(onClick = { scale = s; offsetX = 0f; offsetY = 0f }) {
                    Text(label)
                }
            }
            OutlinedButton(onClick = { scale = 1f; offsetX = 0f; offsetY = 0f }) {
                Text("Reset")
            }
        }

        // Mode-specific label (FR-3.4)
        if (label != null) {
            Text(
                text = label,
                color = AuraTextSecondary,
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp).align(Alignment.CenterHorizontally),
            )
        }
    }
}

// ---- Silhouette drawing (SRS §5.5) ----

private fun DrawScope.drawSilhouette(
    fatT: Float,
    waistT: Float,
    viewMode: SilhouetteViewMode,
    whtr: Float,
) {
    // Base half-widths (canvas pixel units) — per SRS §5.5 starting values
    val baseChest = 35f
    val baseWaist = 28f
    val baseHip = 38f

    // Per SRS §5.5 — exact coefficient mapping
    val chestHalf = baseChest + 0.20f * fatT * baseChest
    val waistHalf = baseWaist + 0.55f * waistT * baseWaist + 0.10f * fatT * baseWaist
    val hipHalf = baseHip + 0.35f * fatT * baseHip + 0.05f * waistT * baseHip

    // Vertical positions (fixed — silhouette doesn't grow taller/shorter)
    val yHead = -50f
    val yNeck = 0f
    val yChest = 60f
    val yWaist = 140f
    val yHip = 220f
    val yLeg = 320f

    // Build symmetric body path (left = mirror of right around x=0)
    val bodyPath = Path().apply {
        // Start at top of neck (center, yNeck)
        moveTo(0f, yNeck)

        // Right shoulder + chest (Bezier for organic curve)
        cubicTo(
            x1 = chestHalf * 0.6f, y1 = yNeck + 10f,
            x2 = chestHalf, y2 = yChest - 20f,
            x3 = chestHalf, y3 = yChest,
        )
        // Chest → Waist (right side)
        cubicTo(
            x1 = chestHalf, y1 = yChest + 40f,
            x2 = waistHalf, y2 = yWaist - 30f,
            x3 = waistHalf, y3 = yWaist,
        )
        // Waist → Hip (right side)
        cubicTo(
            x1 = waistHalf, y1 = yWaist + 30f,
            x2 = hipHalf, y2 = yHip - 40f,
            x3 = hipHalf, y3 = yHip,
        )
        // Hip → leg (right side)
        cubicTo(
            x1 = hipHalf * 0.7f, y1 = yHip + 40f,
            x2 = 18f, y2 = yLeg - 20f,
            x3 = 18f, y3 = yLeg,
        )
        // Right leg down to bottom
        lineTo(15f, yLeg + 60f)
        // Bottom of right leg
        lineTo(-15f, yLeg + 60f)
        // Left leg back up
        lineTo(-15f, yLeg)
        // Leg → Hip (left side, mirror)
        cubicTo(
            x1 = -hipHalf * 0.7f, y1 = yHip + 40f,
            x2 = -hipHalf, y2 = yHip - 40f,
            x3 = -hipHalf, y3 = yHip,
        )
        // Hip → Waist (left side, mirror)
        cubicTo(
            x1 = -waistHalf, y1 = yWaist + 30f,
            x2 = -chestHalf, y2 = yChest + 40f,
            x3 = -chestHalf, y3 = yChest,
        )
        // Chest → Neck (left side, mirror)
        cubicTo(
            x1 = -chestHalf, y1 = yChest - 20f,
            x2 = -chestHalf * 0.6f, y2 = yNeck + 10f,
            x3 = 0f, y3 = yNeck,
        )
        close()
    }

    // Head (simple circle)
    val headRadius = 22f
    val headPath = Path().apply {
        addOval(
            androidx.compose.ui.geometry.Rect(
                left = -headRadius,
                top = yHead - headRadius,
                right = headRadius,
                bottom = yHead + headRadius,
            )
        )
    }

    // ---- Render per view mode ----
    when (viewMode) {
        SilhouetteViewMode.SKIN -> {
            // Filled silhouette in accent cyan with subtle vertical gradient
            val skinBrush = Brush.verticalGradient(
                colors = listOf(AuraAccent, AuraAccentDark),
                startY = yHead - headRadius,
                endY = yLeg + 60f,
            )
            drawPath(path = bodyPath, brush = skinBrush)
            drawPath(path = headPath, brush = skinBrush)
        }

        SilhouetteViewMode.STRUCTURE -> {
            // Wireframe — silhouette outline only, dashed
            val wireframeStyle = Stroke(
                width = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f)),
                cap = StrokeCap.Round,
            )
            drawPath(
                path = bodyPath,
                color = AuraAccent,
                style = wireframeStyle,
            )
            drawPath(
                path = headPath,
                color = AuraAccent,
                style = wireframeStyle,
            )

            // Stylized skeletal reference points (FR-3.4 — labeled "Stylized structural reference")
            val refPoints = listOf(
                Offset(0f, yNeck + 8f) to "clavicle",
                Offset(0f, yChest + 40f) to "sternum",
                Offset(-waistHalf, yWaist) to "iliac L",
                Offset(waistHalf, yWaist) to "iliac R",
            )
            refPoints.forEach { (pos, _) ->
                drawCircle(
                    color = AuraAccent,
                    radius = 4f,
                    center = pos,
                )
                drawCircle(
                    color = AuraAccent,
                    radius = 8f,
                    center = pos,
                    style = Stroke(width = 1.5f),
                )
            }
        }

        SilhouetteViewMode.HEATMAP -> {
            // Skin-tone base
            val skinBrush = Brush.verticalGradient(
                colors = listOf(AuraAccent.copy(alpha = 0.4f), AuraAccentDark.copy(alpha = 0.4f)),
                startY = yHead,
                endY = yLeg,
            )
            drawPath(path = bodyPath, brush = skinBrush)
            drawPath(path = headPath, brush = skinBrush)

            // Heatmap overlay on waist region — color chosen by WHtR band (FR-3.4)
            val heatmapColor = when {
                whtr < 0.5f -> StatusHealthy
                whtr < 0.6f -> StatusElevated
                else -> StatusHigh
            }
            val heatmapBrush = Brush.radialGradient(
                colors = listOf(heatmapColor.copy(alpha = 0.7f), heatmapColor.copy(alpha = 0.0f)),
                center = Offset(0f, yWaist),
                radius = hipHalf * 1.5f,
            )
            // Clip the heatmap to the body path by drawing it slightly inset
            val heatmapPath = Path().apply {
                // Inset waist region for the heatmap
                moveTo(-waistHalf, yWaist - 40f)
                cubicTo(-waistHalf, yWaist, -waistHalf, yWaist + 40f, -hipHalf, yHip)
                cubicTo(0f, yHip + 20f, hipHalf, yHip, hipHalf, yHip)
                cubicTo(waistHalf, yWaist + 40f, waistHalf, yWaist, waistHalf, yWaist - 40f)
                close()
            }
            drawPath(path = heatmapPath, brush = heatmapBrush)
        }
    }
}
