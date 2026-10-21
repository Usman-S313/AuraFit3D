package com.aurafit3d.data.biometric

import com.aurafit3d.data.model.ActivityLevel
import com.aurafit3d.data.model.BodyFatBand
import com.aurafit3d.data.model.BmiBand
import com.aurafit3d.data.model.Sex
import com.aurafit3d.data.model.WhtrStatus
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min

/**
 * Pure-Kotlin biometric engine — per SRS FR-2.4.
 * NO Android framework dependencies, NO rendering dependencies.
 * Runs identically inside JVM unit tests on a dev machine and inside the Android app.
 *
 * Implements SRS §5.1–5.5 formulas exactly:
 *   - U.S. Navy body fat % (male + female), clamped to [2, 60]
 *   - BMI = weight / height_m²
 *   - Lean mass = weight × (1 − BF/100)
 *   - FFMI = leanMass / height_m²
 *   - FFMI normalized = FFMI + 6.1 × (1.8 − height_m)  (the headline value)
 *   - WHtR = waist / height (both cm)
 *   - BMR (Mifflin-St Jeor) = 10×weight + 6.25×height − 5×age + s (s = +5 male, −161 female)
 *   - TDEE = BMR × activity multiplier
 *   - fatT = clamp((BF − 8) / 32, 0, 1)
 *   - waistT = clamp((WHtR − 0.40) / 0.30, 0, 1)
 *
 * Per SRS FR-2.3 — NEVER returns NaN/Infinity or partial results on invalid input.
 * Invalid input produces a list of ValidationError and no result.
 *
 * Reference test fixtures (SRS Appendix A — tolerance ±0.05):
 *   Case 1: M 180/80/38/85, age 25, moderate → BMI 24.69, BF% 16.11, FFMI 20.71, WHtR 0.472, BMR 1805.0
 *   Case 2: F 165/60/32/72/98, age 28, light → BMI 22.04, BF% 27.43, FFMI 16.91, WHtR 0.436, BMR 1330.2
 *   Case 3: M 175/95/40/102, age 40, sedentary → BMI 31.02, BF% 27.27, FFMI 22.87, WHtR 0.583, BMR 1848.8
 *   Case 4: F 170/55/30/66/92, age 22, active → BMI 19.03, BF% 20.93, FFMI 15.66, WHtR 0.388, BMR 1341.5
 */
object BiometricEngine {

    // ---- Validation ranges (SRS Table 4.1) ----
    private const val HEIGHT_MIN = 120.0
    private const val HEIGHT_MAX = 230.0
    private const val WEIGHT_MIN = 30.0
    private const val WEIGHT_MAX = 300.0
    private const val AGE_MIN = 15
    private const val AGE_MAX = 90
    private const val NECK_MIN = 20.0
    private const val NECK_MAX = 60.0
    private const val WAIST_MIN = 40.0
    private const val WAIST_MAX = 200.0
    private const val HIP_MIN = 50.0
    private const val HIP_MAX = 200.0

    // ---- Body fat clamp range (SRS §5.1) ----
    private const val BF_MIN = 2.0
    private const val BF_MAX = 60.0

    // ---- Silhouette mapping constants (SRS §5.5) ----
    private const val FAT_T_OFFSET = 8.0
    private const val FAT_T_RANGE = 32.0
    private const val WAIST_T_OFFSET = 0.40
    private const val WAIST_T_RANGE = 0.30

    /**
     * Validate input per SRS FR-1.3 and Table 4.1.
     * Returns a list of validation errors. Empty list = valid input.
     * The engine NEVER returns a partial result on invalid input.
     */
    fun validate(input: BiometricInput): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        // Height
        if (input.height !in HEIGHT_MIN..HEIGHT_MAX) {
            errors += ValidationError(
                field = "height",
                message = "Height must be in range $HEIGHT_MIN–$HEIGHT_MAX cm",
            )
        }
        // Weight
        if (input.weight !in WEIGHT_MIN..WEIGHT_MAX) {
            errors += ValidationError(
                field = "weight",
                message = "Weight must be in range $WEIGHT_MIN–$WEIGHT_MAX kg",
            )
        }
        // Age
        if (input.age !in AGE_MIN..AGE_MAX) {
            errors += ValidationError(
                field = "age",
                message = "Age must be in range $AGE_MIN–$AGE_MAX years",
            )
        }
        // Neck
        if (input.neck !in NECK_MIN..NECK_MAX) {
            errors += ValidationError(
                field = "neck",
                message = "Neck must be in range $NECK_MIN–$NECK_MAX cm",
            )
        }
        // Waist
        if (input.waist !in WAIST_MIN..WAIST_MAX) {
            errors += ValidationError(
                field = "waist",
                message = "Waist must be in range $WAIST_MIN–$WAIST_MAX cm",
            )
        }
        // For males, waist must exceed neck (Navy method requires positive (waist − neck))
        if (input.sex == Sex.MALE && input.waist <= input.neck) {
            errors += ValidationError(
                field = "waist",
                message = "Waist must exceed neck for males (Navy method constraint)",
            )
        }
        // Hip — required for females, optional for males
        if (input.sex == Sex.FEMALE) {
            val hip = input.hip
            if (hip == null) {
                errors += ValidationError(
                    field = "hip",
                    message = "Hip circumference is required for female users",
                )
            } else if (hip !in HIP_MIN..HIP_MAX) {
                errors += ValidationError(
                    field = "hip",
                    message = "Hip must be in range $HIP_MIN–$HIP_MAX cm",
                )
            }
        }
        return errors
    }

    /**
     * Compute all biometric metrics per SRS §5.1–5.5.
     * Per FR-2.3, NEVER returns NaN/Infinity — invalid input throws IllegalArgumentException.
     * Caller MUST validate(input) first; on failure, do not call this function.
     */
    fun compute(input: BiometricInput): BiometricResult {
        val errors = validate(input)
        require(errors.isEmpty()) {
            "Cannot compute biometric on invalid input: ${errors.joinToString { it.message }}"
        }

        val heightM = input.height / 100.0

        // ---- Body fat % (U.S. Navy method, SRS §5.1) ----
        val bodyFatRaw = when (input.sex) {
            Sex.MALE -> {
                // BF% = 495 / (1.0324 − 0.19077 × log10(waist − neck) + 0.15456 × log10(height)) − 450
                val waistMinusNeck = input.waist - input.neck   // guaranteed > 0 by validation
                495.0 / (1.0324 - 0.19077 * log10(waistMinusNeck) + 0.15456 * log10(input.height)) - 450.0
            }
            Sex.FEMALE -> {
                // BF% = 495 / (1.29579 − 0.35004 × log10(waist + hip − neck) + 0.22100 × log10(height)) − 450
                val hip = input.hip ?: throw IllegalArgumentException("Hip required for female")
                val sum = input.waist + hip - input.neck       // guaranteed > 0 by ranges
                495.0 / (1.29579 - 0.35004 * log10(sum) + 0.22100 * log10(input.height)) - 450.0
            }
        }
        // Clamp to [2, 60] per SRS §5.1
        val bodyFat = bodyFatRaw.coerceIn(BF_MIN, BF_MAX)

        // ---- Other metrics (SRS §5.2) ----
        val bmi = input.weight / (heightM * heightM)
        val leanMass = input.weight * (1.0 - bodyFat / 100.0)
        val ffmi = leanMass / (heightM * heightM)
        val ffmiNormalized = ffmi + 6.1 * (1.8 - heightM)
        val whtr = input.waist / input.height

        // BMR (Mifflin-St Jeor): s = +5 male, −161 female
        val s = if (input.sex == Sex.MALE) 5.0 else -161.0
        val bmr = 10.0 * input.weight + 6.25 * input.height - 5.0 * input.age + s
        val tdee = bmr * input.activity.multiplier

        // Silhouette mapping (SRS §5.5)
        val fatT = ((bodyFat - FAT_T_OFFSET) / FAT_T_RANGE).coerceIn(0.0, 1.0)
        val waistT = ((whtr - WAIST_T_OFFSET) / WAIST_T_RANGE).coerceIn(0.0, 1.0)

        // ---- Sanity: guarantee no NaN/Infinity (FR-2.3) ----
        fun safe(v: Double): Double = if (v.isNaN() || v.isInfinite()) 0.0 else v
        return BiometricResult(
            bmi = safe(bmi),
            bodyFat = safe(bodyFat),
            leanMass = safe(leanMass),
            ffmi = safe(ffmi),
            ffmiNormalized = safe(ffmiNormalized),
            whtr = safe(whtr),
            bmr = safe(bmr),
            tdee = safe(tdee),
            fatT = safe(fatT),
            waistT = safe(waistT),
        )
    }

    /**
     * Convenience classifier helpers (informational — engine itself doesn't classify,
     * but UI uses these for band labels).
     */
    fun classifyBodyFat(sex: Sex, bodyFat: Double): BodyFatBand = BodyFatBand.classify(sex, bodyFat)
    fun classifyWhtr(whtr: Double): WhtrStatus = WhtrStatus.classify(whtr)
    fun classifyBmi(bmi: Double): BmiBand = BmiBand.classify(bmi)

    /**
     * Helper to clamp a value to [min, max].
     * Visible for testing — also used by MacroPlanner.
     */
    internal fun clamp(v: Double, min: Double, max: Double): Double =
        max(min, min(max, v))
}
