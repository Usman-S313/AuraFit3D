package com.aurafit3d.data.model

/**
 * Biological sex — drives Navy body fat formula selection and ACE classification bands.
 * Per SRS §5.1 and Table 5.4.
 */
enum class Sex(val label: String) {
    MALE("Male"),
    FEMALE("Female"),
}

/**
 * Activity level — drives TDEE multiplier.
 * Per SRS Table 5.2.
 */
enum class ActivityLevel(val multiplier: Double, val displayLabel: String) {
    SEDENTARY(1.2, "Sedentary"),
    LIGHT(1.375, "Light"),
    MODERATE(1.55, "Moderate"),
    ACTIVE(1.725, "Active"),
    VERY_ACTIVE(1.9, "Very Active"),
}

/**
 * Silhouette view modes — per SRS FR-3.3.
 * Implemented as layered VectorDrawable/Canvas groups toggled by Compose state.
 */
enum class SilhouetteViewMode(val label: String) {
    SKIN("Skin"),
    STRUCTURE("Structure"),
    HEATMAP("Heatmap"),
}

/**
 * ACE body fat classification bands — per SRS Table 5.4.
 * Lower bound inclusive; upper bound exclusive.
 * FFMI is informational only and carries no pass/fail label (FR-2.2).
 */
enum class BodyFatBand(val label: String) {
    ESSENTIAL("Essential"),
    ATHLETE("Athlete"),
    FITNESS("Fitness"),
    AVERAGE("Average"),
    HIGH_RISK("High Risk"),
    ;

    companion object {
        /**
         * Classify body fat percentage by sex.
         * Male: Essential <6, Athlete 6–13.9, Fitness 14–17.9, Average 18–24.9, High Risk ≥25
         * Female: Essential <14, Athlete 14–20.9, Fitness 21–24.9, Average 25–31.9, High Risk ≥32
         */
        fun classify(sex: Sex, bodyFat: Double): BodyFatBand {
            return when (sex) {
                Sex.MALE -> when {
                    bodyFat < 6.0 -> ESSENTIAL
                    bodyFat < 14.0 -> ATHLETE
                    bodyFat < 18.0 -> FITNESS
                    bodyFat < 25.0 -> AVERAGE
                    else -> HIGH_RISK
                }
                Sex.FEMALE -> when {
                    bodyFat < 14.0 -> ESSENTIAL
                    bodyFat < 21.0 -> ATHLETE
                    bodyFat < 25.0 -> FITNESS
                    bodyFat < 32.0 -> AVERAGE
                    else -> HIGH_RISK
                }
            }
        }
    }
}

/**
 * WHtR status — per SRS §5.2.
 * < 0.5 Healthy, 0.5–0.6 Elevated, ≥ 0.6 High.
 */
enum class WhtrStatus(val label: String) {
    HEALTHY("Healthy"),
    ELEVATED("Elevated"),
    HIGH("High"),
    ;

    companion object {
        fun classify(whtr: Double): WhtrStatus = when {
            whtr < 0.5 -> HEALTHY
            whtr < 0.6 -> ELEVATED
            else -> HIGH
        }
    }
}

/**
 * BMI band (informational only — not in SRS bands but displayed).
 */
enum class BmiBand(val label: String) {
    UNDERWEIGHT("Underweight"),
    NORMAL("Normal"),
    OVERWEIGHT("Overweight"),
    OBESE("Obese"),
    ;

    companion object {
        fun classify(bmi: Double): BmiBand = when {
            bmi < 18.5 -> UNDERWEIGHT
            bmi < 25.0 -> NORMAL
            bmi < 30.0 -> OVERWEIGHT
            else -> OBESE
        }
    }
}

/**
 * Macro planner goal — per SRS FR-4.1.
 * Loss: TDEE − 500 kcal, Maintenance: TDEE, Gain: TDEE + 500 kcal.
 */
enum class MacroGoal(val label: String, val calorieDelta: Int) {
    LOSS("Weight Loss", -500),
    MAINTENANCE("Maintenance", 0),
    GAIN("Weight Gain", +500),
}
