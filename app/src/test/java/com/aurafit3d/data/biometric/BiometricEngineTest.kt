package com.aurafit3d.data.biometric

import com.aurafit3d.data.model.ActivityLevel
import com.aurafit3d.data.model.MacroGoal
import com.aurafit3d.data.model.Sex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Self-check fixture tests — verifies the engine matches SRS Appendix A reference values.
 * Tolerance: ±0.05 for biometric metrics, ±1 kcal/gram for macros.
 *
 * These tests run on the JVM (no Android device required) per SRS FR-2.4
 * (engine must be pure Kotlin with no Android dependencies).
 *
 * Run from CLI:    ./gradlew test
 * Run in AS:       right-click this file → Run 'BiometricEngineTest'
 */
class BiometricEngineTest {

    private val BIO_TOL = 0.05
    private val KCAL_TOL = 1
    private val GRAM_TOL = 1

    // ===== SRS Appendix A — Biometric fixtures =====

    @Test
    fun case1_male_moderate_matchesAppendixA() {
        val input = BiometricInput(
            sex = Sex.MALE, age = 25, height = 180.0, weight = 80.0,
            neck = 38.0, waist = 85.0, hip = null, activity = ActivityLevel.MODERATE,
        )
        val r = BiometricEngine.compute(input)
        assertEquals(24.69, r.bmi, BIO_TOL)
        assertEquals(16.11, r.bodyFat, BIO_TOL)
        assertEquals(20.71, r.ffmi, BIO_TOL)
        assertEquals(20.71, r.ffmiNormalized, BIO_TOL)
        assertEquals(0.472, r.whtr, BIO_TOL)
        assertEquals(1805.0, r.bmr, BIO_TOL)
    }

    @Test
    fun case2_female_light_matchesAppendixA() {
        val input = BiometricInput(
            sex = Sex.FEMALE, age = 28, height = 165.0, weight = 60.0,
            neck = 32.0, waist = 72.0, hip = 98.0, activity = ActivityLevel.LIGHT,
        )
        val r = BiometricEngine.compute(input)
        assertEquals(22.04, r.bmi, BIO_TOL)
        assertEquals(27.43, r.bodyFat, BIO_TOL)
        assertEquals(15.99, r.ffmi, BIO_TOL)
        assertEquals(16.91, r.ffmiNormalized, BIO_TOL)
        assertEquals(0.436, r.whtr, BIO_TOL)
        assertEquals(1330.2, r.bmr, BIO_TOL)
    }

    @Test
    fun case3_male_sedentary_matchesAppendixA() {
        val input = BiometricInput(
            sex = Sex.MALE, age = 40, height = 175.0, weight = 95.0,
            neck = 40.0, waist = 102.0, hip = null, activity = ActivityLevel.SEDENTARY,
        )
        val r = BiometricEngine.compute(input)
        assertEquals(31.02, r.bmi, BIO_TOL)
        assertEquals(27.27, r.bodyFat, BIO_TOL)
        assertEquals(22.56, r.ffmi, BIO_TOL)
        assertEquals(22.87, r.ffmiNormalized, BIO_TOL)
        assertEquals(0.583, r.whtr, BIO_TOL)
        assertEquals(1848.8, r.bmr, BIO_TOL)
    }

    @Test
    fun case4_female_active_matchesAppendixA() {
        val input = BiometricInput(
            sex = Sex.FEMALE, age = 22, height = 170.0, weight = 55.0,
            neck = 30.0, waist = 66.0, hip = 92.0, activity = ActivityLevel.ACTIVE,
        )
        val r = BiometricEngine.compute(input)
        assertEquals(19.03, r.bmi, BIO_TOL)
        assertEquals(20.93, r.bodyFat, BIO_TOL)
        assertEquals(15.05, r.ffmi, BIO_TOL)
        assertEquals(15.66, r.ffmiNormalized, BIO_TOL)
        assertEquals(0.388, r.whtr, BIO_TOL)
        assertEquals(1341.5, r.bmr, BIO_TOL)
    }

    // ===== Macro fixtures (SRS Appendix A.2) =====

    @Test
    fun macro_case1_loss() {
        // Case 1 Loss: 2298 kcal, 144P, 64F, 287C
        val plan = MacroPlanner.plan(tdee = 2798.0, weight = 80.0, sex = Sex.MALE, goal = MacroGoal.LOSS)
        assertEquals(2298.0, plan.calories.toDouble(), KCAL_TOL.toDouble())
        assertEquals(144.0, plan.protein.toDouble(), GRAM_TOL.toDouble())
        assertEquals(64.0, plan.fat.toDouble(), GRAM_TOL.toDouble())
        assertEquals(287.0, plan.carbs.toDouble(), GRAM_TOL.toDouble())
        assertTrue("Floor should not be applied", !plan.floorApplied)
    }

    @Test
    fun macro_case1_gain() {
        val plan = MacroPlanner.plan(tdee = 2798.0, weight = 80.0, sex = Sex.MALE, goal = MacroGoal.GAIN)
        assertEquals(3298.0, plan.calories.toDouble(), KCAL_TOL.toDouble())
        assertEquals(144.0, plan.protein.toDouble(), GRAM_TOL.toDouble())
        assertEquals(92.0, plan.fat.toDouble(), GRAM_TOL.toDouble())
        assertEquals(474.0, plan.carbs.toDouble(), GRAM_TOL.toDouble())
    }

    @Test
    fun macro_case2_loss() {
        val plan = MacroPlanner.plan(tdee = 1829.0, weight = 60.0, sex = Sex.FEMALE, goal = MacroGoal.LOSS)
        assertEquals(1329.0, plan.calories.toDouble(), KCAL_TOL.toDouble())
        assertEquals(108.0, plan.protein.toDouble(), GRAM_TOL.toDouble())
        assertEquals(37.0, plan.fat.toDouble(), GRAM_TOL.toDouble())
        assertEquals(141.0, plan.carbs.toDouble(), GRAM_TOL.toDouble())
    }

    @Test
    fun macro_case3_loss() {
        // Tolerance is ±1, so accept either 1718 or 1719 (rounding diff across platforms)
        val plan = MacroPlanner.plan(tdee = 2218.0, weight = 95.0, sex = Sex.MALE, goal = MacroGoal.LOSS)
        val acceptable = setOf(1718, 1719)
        assertTrue("Calories ${plan.calories} not in acceptable set", plan.calories in acceptable)
        assertEquals(171.0, plan.protein.toDouble(), GRAM_TOL.toDouble())
        assertEquals(48.0, plan.fat.toDouble(), GRAM_TOL.toDouble())
        assertEquals(151.0, plan.carbs.toDouble(), GRAM_TOL.toDouble())
    }

    @Test
    fun macro_case4_gain() {
        val plan = MacroPlanner.plan(tdee = 2314.0, weight = 55.0, sex = Sex.FEMALE, goal = MacroGoal.GAIN)
        assertEquals(2814.0, plan.calories.toDouble(), KCAL_TOL.toDouble())
        assertEquals(99.0, plan.protein.toDouble(), GRAM_TOL.toDouble())
        assertEquals(78.0, plan.fat.toDouble(), GRAM_TOL.toDouble())
        assertEquals(429.0, plan.carbs.toDouble(), GRAM_TOL.toDouble())
    }

    // ===== Projection fixture (SRS Appendix A.3) =====

    @Test
    fun projection_case1_target12pct() {
        // Case 1 with 12% target → target weight ≈ 76.27 kg, fat to lose ≈ 3.73 kg, ≈ 8.2 weeks
        val weight = 80.0
        val leanMass = 80.0 * (1.0 - 16.11 / 100.0)  // = 67.112
        val projection = GoalProjection.project(
            weight = weight,
            leanMass = leanMass,
            currentBodyFat = 16.11,
            targetBodyFat = 12.0,
        )
        assertEquals(76.27, projection.targetWeight, 0.05)
        assertEquals(3.73, projection.fatToLose, 0.05)
        assertEquals(8.2, projection.weeks, 0.05)
        assertTrue("Should not be at target", !projection.alreadyAtOrBelowTarget)
    }

    // ===== Validation tests (SRS FR-1.3) =====

    @Test
    fun validation_rejectsWaistNotExceedingNeckForMale() {
        val input = BiometricInput(
            sex = Sex.MALE, age = 25, height = 180.0, weight = 80.0,
            neck = 38.0, waist = 38.0, hip = null, activity = ActivityLevel.MODERATE,
        )
        val errors = BiometricEngine.validate(input)
        assertTrue("Should reject waist not exceeding neck", errors.any { it.field == "waist" })
    }

    @Test
    fun validation_rejectsMissingHipForFemale() {
        val input = BiometricInput(
            sex = Sex.FEMALE, age = 28, height = 165.0, weight = 60.0,
            neck = 32.0, waist = 72.0, hip = null, activity = ActivityLevel.LIGHT,
        )
        val errors = BiometricEngine.validate(input)
        assertTrue("Should reject missing hip for female", errors.any { it.field == "hip" })
    }

    @Test
    fun validation_acceptsValidInput() {
        val input = BiometricInput(
            sex = Sex.MALE, age = 25, height = 180.0, weight = 80.0,
            neck = 38.0, waist = 85.0, hip = null, activity = ActivityLevel.MODERATE,
        )
        val errors = BiometricEngine.validate(input)
        assertTrue("Valid input should have no errors", errors.isEmpty())
    }
}
