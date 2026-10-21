package com.aurafit3d.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aurafit3d.data.biometric.BiometricEngine
import com.aurafit3d.data.biometric.BiometricInput
import com.aurafit3d.data.biometric.BiometricResult
import com.aurafit3d.data.biometric.GoalProjection
import com.aurafit3d.data.biometric.MacroPlan
import com.aurafit3d.data.biometric.MacroPlanner
import com.aurafit3d.data.biometric.Projection
import com.aurafit3d.data.biometric.ValidationError
import com.aurafit3d.data.model.ActivityLevel
import com.aurafit3d.data.model.MacroGoal
import com.aurafit3d.data.model.Sex
import com.aurafit3d.data.model.SilhouetteViewMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Mutable input form state — per SRS FR-1.1.
 * All internal values are metric (cm, kg).
 */
data class InputFormState(
    val sex: Sex = Sex.MALE,
    val age: Int = 25,
    val height: Double = 180.0,
    val weight: Double = 80.0,
    val neck: Double = 38.0,
    val waist: Double = 85.0,
    val hip: Double? = null,
    val activity: ActivityLevel = ActivityLevel.MODERATE,
    val useImperial: Boolean = false,
)

data class HistoryEntry(
    val id: Long,
    val date: String,
    val bodyFat: Double,
    val weight: Double,
    val bmi: Double,
)

data class AppUiState(
    val form: InputFormState = InputFormState(),
    val validationErrors: List<ValidationError> = emptyList(),
    val result: BiometricResult? = null,
    val silhouetteViewMode: SilhouetteViewMode = SilhouetteViewMode.SKIN,
    val macroGoal: MacroGoal = MacroGoal.LOSS,
    val targetBodyFat: Double = 12.0,
    val macroPlan: MacroPlan? = null,
    val projection: Projection? = null,
    val history: List<HistoryEntry> = mockHistory(),
    val lastSelfCheckOk: Boolean = true,
)

private fun mockHistory(): List<HistoryEntry> = listOf(
    HistoryEntry(1, "2026-10-01", bodyFat = 17.5, weight = 80.5, bmi = 24.85),
    HistoryEntry(2, "2026-10-08", bodyFat = 16.8, weight = 80.2, bmi = 24.76),
    HistoryEntry(3, "2026-10-15", bodyFat = 16.3, weight = 80.0, bmi = 24.69),
    HistoryEntry(4, "2026-10-21", bodyFat = 16.11, weight = 80.0, bmi = 24.69),
)

/**
 * AppViewModel — owns all UI state per MVVM pattern (SRS §6).
 * Per SRS FR-3.6: state survives configuration changes via ViewModel.
 */
class AppViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(lastSelfCheckOk = runSelfCheck()) }
    }

    fun onSexChange(sex: Sex) = _uiState.update {
        it.copy(form = it.form.copy(sex = sex, hip = if (sex == Sex.FEMALE) it.form.hip ?: 90.0 else null))
    }
    fun onMetricImperialChange(useImperial: Boolean) = _uiState.update { it.copy(form = it.form.copy(useImperial = useImperial)) }
    fun onAgeChange(age: Int) = _uiState.update { it.copy(form = it.form.copy(age = age)) }
    fun onHeightChange(v: Double) = _uiState.update { it.copy(form = it.form.copy(height = v)) }
    fun onWeightChange(v: Double) = _uiState.update { it.copy(form = it.form.copy(weight = v)) }
    fun onNeckChange(v: Double) = _uiState.update { it.copy(form = it.form.copy(neck = v)) }
    fun onWaistChange(v: Double) = _uiState.update { it.copy(form = it.form.copy(waist = v)) }
    fun onHipChange(v: Double?) = _uiState.update { it.copy(form = it.form.copy(hip = v)) }
    fun onActivityChange(a: ActivityLevel) = _uiState.update { it.copy(form = it.form.copy(activity = a)) }

    fun compute() {
        val state = _uiState.value
        val input = BiometricInput(
            sex = state.form.sex,
            age = state.form.age,
            height = state.form.height,
            weight = state.form.weight,
            neck = state.form.neck,
            waist = state.form.waist,
            hip = state.form.hip,
            activity = state.form.activity,
        )
        val errors = BiometricEngine.validate(input)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(validationErrors = errors) }
            return
        }
        val result = BiometricEngine.compute(input)
        val macroPlan = MacroPlanner.plan(
            tdee = result.tdee,
            weight = state.form.weight,
            sex = state.form.sex,
            goal = state.macroGoal,
        )
        val projection = GoalProjection.project(
            weight = state.form.weight,
            leanMass = result.leanMass,
            currentBodyFat = result.bodyFat,
            targetBodyFat = state.targetBodyFat,
        )
        _uiState.update {
            it.copy(
                validationErrors = emptyList(),
                result = result,
                macroPlan = macroPlan,
                projection = projection,
            )
        }
    }

    fun onSilhouetteViewModeChange(mode: SilhouetteViewMode) =
        _uiState.update { it.copy(silhouetteViewMode = mode) }

    fun onMacroGoalChange(goal: MacroGoal) {
        _uiState.update { it.copy(macroGoal = goal) }
        val state = _uiState.value
        val result = state.result ?: return
        val plan = MacroPlanner.plan(
            tdee = result.tdee,
            weight = state.form.weight,
            sex = state.form.sex,
            goal = goal,
        )
        _uiState.update { it.copy(macroPlan = plan) }
    }

    fun onTargetBodyFatChange(target: Double) {
        _uiState.update { it.copy(targetBodyFat = target) }
        val state = _uiState.value
        val result = state.result ?: return
        val proj = GoalProjection.project(
            weight = state.form.weight,
            leanMass = result.leanMass,
            currentBodyFat = result.bodyFat,
            targetBodyFat = target,
        )
        _uiState.update { it.copy(projection = proj) }
    }

    fun deleteAllData() {
        _uiState.update {
            it.copy(
                history = emptyList(),
                result = null,
                macroPlan = null,
                projection = null,
                form = InputFormState(),
            )
        }
    }

    private fun runSelfCheck(): Boolean {
        return try {
            val r1 = BiometricEngine.compute(
                BiometricInput(
                    sex = Sex.MALE, age = 25, height = 180.0, weight = 80.0,
                    neck = 38.0, waist = 85.0, hip = null, activity = ActivityLevel.MODERATE,
                )
            )
            kotlin.math.abs(r1.bmi - 24.69) < 0.05 &&
                kotlin.math.abs(r1.bodyFat - 16.11) < 0.05 &&
                kotlin.math.abs(r1.bmr - 1805.0) < 0.05
        } catch (e: Exception) {
            false
        }
    }
}

object AppViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AppViewModel() as T
}
