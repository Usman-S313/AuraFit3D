package com.aurafit3d.data.biometric

import com.aurafit3d.data.model.MacroGoal
import com.aurafit3d.data.model.Sex
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Macro planner result — per SRS FR-4.1–4.3.
 *
 * @property goal selected goal (loss/maintenance/gain)
 * @property calories daily calorie target (kcal)
 * @property protein grams per day
 * @property fat grams per day
 * @property carbs grams per day
 * @property floorApplied true if calorie floor (1200 female / 1500 male) was applied (FR-4.3)
 * @property carbsClamped true if carbs were clamped to 0 and protein reduced (FR-4.3)
 */
data class MacroPlan(
    val goal: MacroGoal,
    val calories: Int,
    val protein: Int,
    val fat: Int,
    val carbs: Int,
    val floorApplied: Boolean,
    val carbsClamped: Boolean,
)

/**
 * Macro planner — per SRS FR-4.
 * Pure Kotlin, no Android dependencies.
 *
 * Formulas:
 *   calories = TDEE + goal.calorieDelta (loss=−500, maintenance=0, gain=+500)
 *   protein = 1.8 g × weight (kg)
 *   fat = 25% × calories / 9
 *   carbs = (calories − protein×4 − fat×9) / 4
 *
 * Calorie floor (FR-4.3):
 *   female: 1200 kcal, male: 1500 kcal
 *   If planned calories < floor, clamp to floor, set floorApplied=true
 *
 * Carb clamp (FR-4.3):
 *   If carbs would be negative, clamp carbs to 0, reduce protein to fit, set carbsClamped=true
 */
object MacroPlanner {

    private const val PROTEIN_PER_KG = 1.8        // g/kg body weight (SRS FR-4.2)
    private const val FAT_PERCENT_OF_CALORIES = 0.25  // 25% (SRS FR-4.2)
    private const val KCAL_PER_G_PROTEIN = 4.0
    private const val KCAL_PER_G_FAT = 9.0
    private const val KCAL_PER_G_CARB = 4.0
    private const val FLOOR_FEMALE = 1200
    private const val FLOOR_MALE = 1500

    fun plan(
        tdee: Double,
        weight: Double,
        sex: Sex,
        goal: MacroGoal,
    ): MacroPlan {
        // Step 1: raw calorie target
        var calories = (tdee + goal.calorieDelta).roundToInt()

        // Step 2: apply calorie floor (FR-4.3)
        val floor = if (sex == Sex.FEMALE) FLOOR_FEMALE else FLOOR_MALE
        var floorApplied = false
        if (calories < floor) {
            calories = floor
            floorApplied = true
        }

        // Step 3: compute protein and fat
        var protein = (PROTEIN_PER_KG * weight).roundToInt()
        val fat = (FAT_PERCENT_OF_CALORIES * calories / KCAL_PER_G_FAT).roundToInt()

        // Step 4: compute carbs from remaining calories
        // carbs = (calories − protein×4 − fat×9) / 4
        val remainingKcal = calories - protein * KCAL_PER_G_PROTEIN - fat * KCAL_PER_G_FAT
        var carbs = (remainingKcal / KCAL_PER_G_CARB).roundToInt()

        // Step 5: carb clamp (FR-4.3) — if carbs < 0, set to 0, reduce protein to fit
        var carbsClamped = false
        if (carbs < 0) {
            carbs = 0
            // Reduce protein so that protein×4 + fat×9 = calories
            val proteinKcal = calories - fat * KCAL_PER_G_FAT
            protein = max(0, (proteinKcal / KCAL_PER_G_PROTEIN).roundToInt())
            carbsClamped = true
        }

        return MacroPlan(
            goal = goal,
            calories = calories,
            protein = protein,
            fat = fat,
            carbs = carbs,
            floorApplied = floorApplied,
            carbsClamped = carbsClamped,
        )
    }
}

/**
 * Goal projection — per SRS FR-5.
 *
 * Assumptions (per SRS FR-5.3):
 *   - constant lean mass (all mass change is fat mass)
 *   - 7700 kcal per kg of fat
 *   - 500 kcal per day deficit
 *   - weekly fat loss = 3500 / 7700 ≈ 0.4545 kg per week
 *
 * Formulas (SRS §5.3):
 *   targetWeight = leanMass / (1 − targetBF/100)
 *   fatToLose = weight − targetWeight
 *   weeks = fatToLose / 0.4545
 *
 * If targetBF >= currentBF, set alreadyAtOrBelowTarget = true and return zeros.
 */
data class Projection(
    val targetWeight: Double,
    val fatToLose: Double,
    val weeks: Double,
    val alreadyAtOrBelowTarget: Boolean,
)

object GoalProjection {

    private const val KCAL_PER_KG_FAT = 7700.0
    private const val KCAL_PER_DAY_DEFICIT = 500.0
    private const val WEEKLY_FAT_LOSS_KG = 3500.0 / KCAL_PER_KG_FAT  // ≈ 0.4545

    fun project(
        weight: Double,
        leanMass: Double,
        currentBodyFat: Double,
        targetBodyFat: Double,
    ): Projection {
        // Per SRS FR-5.1 — target must be above Essential-band floor for sex
        // (validation should be done by caller; we just check the no-op case here)
        if (targetBodyFat >= currentBodyFat) {
            return Projection(
                targetWeight = weight,
                fatToLose = 0.0,
                weeks = 0.0,
                alreadyAtOrBelowTarget = true,
            )
        }

        val targetWeight = leanMass / (1.0 - targetBodyFat / 100.0)
        val fatToLose = weight - targetWeight
        val weeks = fatToLose / WEEKLY_FAT_LOSS_KG

        return Projection(
            targetWeight = targetWeight,
            fatToLose = fatToLose,
            weeks = weeks,
            alreadyAtOrBelowTarget = false,
        )
    }
}
