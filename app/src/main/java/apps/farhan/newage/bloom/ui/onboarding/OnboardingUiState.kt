package apps.farhan.newage.bloom.ui.onboarding

enum class TrackItem(val key: String, val title: String, val subtitle: String) {
    MEALS("meals", "Meals", "3 meals a day"),
    MOVEMENT("movement", "Movement", "30 minutes"),
    WATER("water", "Water", "8 glasses"),
    SLEEP("sleep", "Sleep", "8 hours"),
    RELAXATION("relaxation", "Relaxation", "10 minutes"),
    CYCLE("cycle", "Cycle", "Period tracking"),
    SYMPTOMS("symptoms", "Symptom check-in", "Bloating, mood, pain, energy"),
}

enum class ReminderSlot(val title: String, val subtitle: String, val defaultHour: Int) {
    MORNING("Morning", "A gentle start to your day", 8),
    AFTERNOON("Afternoon", "A midday check-in", 13),
    EVENING("Evening", "Wind down and reflect", 20),
}

data class OnboardingUiState(
    val step: Int = 0,
    val name: String = "",
    val tracked: Set<TrackItem> = TrackItem.entries.toSet(),
    val slot: ReminderSlot = ReminderSlot.MORNING,
    val hour: Int = ReminderSlot.MORNING.defaultHour,
    val minute: Int = 0,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val finished: Boolean = false,
) {
    val isLastStep: Boolean get() = step == STEP_COUNT - 1

    val canContinue: Boolean
        get() = when (step) {
            1 -> name.isNotBlank()
            2 -> tracked.isNotEmpty()
            else -> true
        }

    companion object {
        const val STEP_COUNT = 4
    }
}