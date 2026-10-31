package apps.farhan.newage.bloom.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apps.farhan.newage.bloom.data.UserPreferencesRepository
import apps.farhan.newage.bloom.ui.onboarding.TrackItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class HomeViewModel(private val repo: UserPreferencesRepository) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val name = repo.userName.first()
            val keys = repo.trackedKeys.first()
            val (hour, minute) = repo.reminderTime.first()
            _state.update {
                it.copy(
                    userName = name.ifBlank { "there" },
                    tracked = TrackItem.entries.filter { t -> t.key in keys }.toSet()
                        .ifEmpty { TrackItem.entries.toSet() },
                    reminderHour = hour,
                    reminderMinute = minute,
                )
            }
        }
    }

    fun setMeal(slot: MealSlot, choice: MealChoice) =
        _state.update { it.copy(meals = it.meals + (slot to choice)) }

    fun setProgress(goal: GoalType, value: Int) =
        _state.update { it.copy(progress = it.progress + (goal to value)) }

    fun changeTarget(goal: GoalType, direction: Int) = _state.update { s ->
        val next = (s.target(goal) + direction * goal.step).coerceIn(goal.step, goal.max)
        s.copy(targets = s.targets + (goal to next))
    }

    fun setTracking(item: TrackItem, enabled: Boolean) {
        val next = if (enabled) _state.value.tracked + item else _state.value.tracked - item
        _state.update { it.copy(tracked = next) }
        persist { repo.setTracked(next.map { it.key }.toSet()) }
    }

    fun setNotifications(on: Boolean) = _state.update { it.copy(notificationsOn = on) }

    fun setReminderTime(hour: Int, minute: Int) {
        _state.update { it.copy(reminderHour = hour, reminderMinute = minute) }
        persist { repo.setReminderTime(hour, minute) }
    }

    fun setName(name: String) {
        val clean = name.trim()
        _state.update { it.copy(userName = clean) }
        persist { repo.setName(clean) }
    }

    fun dismissTip() = _state.update { it.copy(tipVisible = false) }

    /** Clears local data so the next launch behaves like a first launch. */
    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch {
            try { repo.clearAll() } catch (_: IOException) { }
            onDone()
        }
    }

    private fun persist(block: suspend () -> Unit) {
        viewModelScope.launch { try { block() } catch (_: IOException) { } }
    }

    fun saveProfile(name: String, stage: JourneyStage?) {
        setName(name)
        _state.update { it.copy(journeyStage = stage) }
    }

    fun setLastPeriod(day: Long) = _state.update { it.copy(lastPeriodDay = day) }

    fun setSymptom(category: SymptomCategory, index: Int) = _state.update { s ->
        // tapping the selected option again clears it
        val next = if (s.symptoms[category] == index) s.symptoms - category else s.symptoms + (category to index)
        s.copy(symptoms = next)
    }
}