package apps.farhan.newage.bloom.ui.onboarding


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apps.farhan.newage.bloom.data.OnboardingResult
import apps.farhan.newage.bloom.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class OnboardingViewModel(
    private val repository: UserPreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value.take(40)) }
    }

    fun onToggle(item: TrackItem, enabled: Boolean) {
        _uiState.update { s ->
            s.copy(tracked = if (enabled) s.tracked + item else s.tracked - item)
        }
    }

    fun onSlotSelected(slot: ReminderSlot) {
        _uiState.update { it.copy(slot = slot, hour = slot.defaultHour, minute = 0) }
    }

    fun onTimeSelected(hour: Int, minute: Int) {
        _uiState.update { it.copy(hour = hour, minute = minute) }
    }

    fun onNext() {
        val s = _uiState.value
        if (!s.canContinue || s.isSaving) return
        if (s.isLastStep) save() else _uiState.update { it.copy(step = it.step + 1) }
    }

    fun onBack() {
        _uiState.update { s ->
            if (s.step == 0 || s.isSaving) s else s.copy(step = s.step - 1, errorMessage = null)
        }
    }

    /** "Set up later" on the last step: finish onboarding with the default reminder. */
    fun onSkip() {
        if (!_uiState.value.isSaving) save()
    }

    private fun save() {
        val s = _uiState.value
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                repository.completeOnboarding(
                    OnboardingResult(
                        name = s.name.trim(),
                        trackedKeys = s.tracked.map { it.key }.toSet(),
                        reminderSlot = s.slot.name,
                        reminderHour = s.hour,
                        reminderMinute = s.minute,
                    ),
                )
                _uiState.update { it.copy(isSaving = false, finished = true) }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Couldn't save your preferences. Please try again.",
                    )
                }
            }
        }
    }
}