package com.aurafit3d.data.biometric

import com.aurafit3d.data.model.ActivityLevel
import com.aurafit3d.data.model.Sex

/**
 * Biometric input — per SRS FR-1.1.
 * All measurements are in metric units (cm, kg) internally.
 * Imperial inputs are converted at the UI layer before reaching this object.
 *
 * Per SRS Table 4.1 — validation ranges:
 *   height 120–230 cm
 *   weight 30–300 kg
 *   age 15–90 years (warn if <18, but accept)
 *   neck 20–60 cm
 *   waist 40–200 cm (must exceed neck for males)
 *   hip 50–200 cm (required for female users only)
 */
data class BiometricInput(
    val sex: Sex,
    val age: Int,
    val height: Double,         // cm
    val weight: Double,        // kg
    val neck: Double,           // cm
    val waist: Double,          // cm
    val hip: Double?,           // cm — null for males; required for females
    val activity: ActivityLevel,
)

/**
 * Validation error — per SRS FR-1.3.
 * The engine never returns NaN/Infinity or partial results on invalid input.
 * Errors carry a field name and a human-readable message.
 */
data class ValidationError(
    val field: String,
    val message: String,
)

/**
 * Result of biometric computation — per SRS FR-2.1.
 * All values are finite — the engine guarantees no NaN/Infinity (FR-2.3).
 *
 * @property bmi weight / height_m²
 * @property bodyFat U.S. Navy body fat %, clamped to [2, 60]
 * @property leanMass weight × (1 − BF/100), in kg
 * @property ffmi lean mass / height_m²
 * @property ffmiNormalized FFMI + 6.1 × (1.8 − height_m); headline value
 * @property whtr waist / height (both cm); < 0.5 healthy, 0.5–0.6 elevated, ≥ 0.6 high
 * @property bmr Mifflin-St Jeor: 10×weight + 6.25×height − 5×age + s (s=+5 male, −161 female)
 * @property tdee BMR × activity multiplier
 * @property fatT normalized fat parameter for silhouette morphing: clamp((BF − 8) / 32, 0, 1)
 * @property waistT normalized waist parameter for silhouette morphing: clamp((WHtR − 0.40) / 0.30, 0, 1)
 */
data class BiometricResult(
    val bmi: Double,
    val bodyFat: Double,
    val leanMass: Double,
    val ffmi: Double,
    val ffmiNormalized: Double,
    val whtr: Double,
    val bmr: Double,
    val tdee: Double,
    val fatT: Double,
    val waistT: Double,
)
