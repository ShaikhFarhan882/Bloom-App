package apps.farhan.newage.bloom.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import apps.farhan.newage.bloom.ui.onboarding.TrackItem
import kotlin.math.roundToInt

val TrackItem.icon: ImageVector
    get() = when (this) {
        TrackItem.MEALS -> Icons.Outlined.Restaurant
        TrackItem.MOVEMENT -> Icons.Outlined.FitnessCenter
        TrackItem.WATER -> Icons.Outlined.WaterDrop
        TrackItem.SLEEP -> Icons.Outlined.Bedtime
        TrackItem.RELAXATION -> Icons.Outlined.SelfImprovement
        TrackItem.CYCLE -> Icons.Outlined.Opacity
        TrackItem.SYMPTOMS -> Icons.Outlined.SentimentSatisfied
    }

val TrackItem.color: Color
    get() = when (this) {
        TrackItem.MEALS -> Color(0xFFB5524B)
        TrackItem.MOVEMENT -> Color(0xFF6B9A6E)
        TrackItem.WATER -> Color(0xFF5B8FC4)
        TrackItem.SLEEP -> Color(0xFF4B4A7C)
        TrackItem.RELAXATION -> Color(0xFF8E7CC3)
        TrackItem.CYCLE -> Color(0xFFC0504D)
        TrackItem.SYMPTOMS -> Color(0xFFD4A24C)
    }

/** Order used by the Settings "What you're tracking" list (matches Figma). */
val TRACKING_DISPLAY_ORDER = listOf(
    TrackItem.MEALS, TrackItem.MOVEMENT, TrackItem.WATER, TrackItem.RELAXATION,
    TrackItem.SLEEP, TrackItem.CYCLE, TrackItem.SYMPTOMS,
)

enum class GoalType(
    val trackItem: TrackItem,
    val unit: String,
    val defaultTarget: Int,
    val step: Int,
    val max: Int,
) {
    MEALS(TrackItem.MEALS, "meals", 3, 1, 3),
    MOVEMENT(TrackItem.MOVEMENT, "min", 30, 5, 300),
    WATER(TrackItem.WATER, "glasses", 8, 1, 20),
    RELAXATION(TrackItem.RELAXATION, "min", 15, 5, 120),
    SLEEP(TrackItem.SLEEP, "hrs", 8, 1, 16);

    val title: String get() = trackItem.title
    val icon: ImageVector get() = trackItem.icon
    val color: Color get() = trackItem.color
}

enum class MealSlot(val label: String) { BREAKFAST("Breakfast"), LUNCH("Lunch"), DINNER("Dinner") }

enum class MealChoice(val label: String) {
    LIGHT("Light"), BALANCED("Balanced"), INDULGENT("Indulgent"), SKIPPED("Skipped")
}

enum class HomeTab(val label: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Outlined.Home),
    INSIGHTS("Insights", Icons.Outlined.BarChart),
    LEARN("Learn", Icons.Outlined.MenuBook),
    SETTINGS("Settings", Icons.Outlined.Settings),
}

enum class JourneyStage(val title: String, val subtitle: String) {
    DIAGNOSED("Diagnosed", "I have a confirmed PCOS diagnosis"),
    SUSPECTED("Suspected", "A doctor mentioned it, not confirmed"),
    EXPLORING("Exploring", "I think I might have symptoms"),
    NOT_SURE("Not sure yet", "I just want to learn more"),
}

enum class SymptomCategory(val label: String, val icon: ImageVector, val options: List<String>) {
    BLOATING("Bloating", Icons.Outlined.Cloud, listOf("None", "Mild", "Moderate", "Severe")),
    SKIN("Skin & acne", Icons.Outlined.AutoAwesome, listOf("Clear", "Mild", "Moderate", "Flare-up")),
    MOOD("Mood", Icons.Outlined.SentimentSatisfied, listOf("Great", "Okay", "Low", "Very low")),
    SLEEP_QUALITY("Sleep quality", Icons.Outlined.Bedtime, listOf("Great", "Okay", "Poor", "Very poor")),
}

data class HomeUiState(
    val userName: String = "",
    val tracked: Set<TrackItem> = TrackItem.entries.toSet(),
    val progress: Map<GoalType, Int> = emptyMap(),
    val targets: Map<GoalType, Int> = GoalType.entries.associateWith { it.defaultTarget },
    val meals: Map<MealSlot, MealChoice> = emptyMap(),
    val notificationsOn: Boolean = true,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val tipVisible: Boolean = true,
    val journeyStage: JourneyStage? = null,
    val lastPeriodDay: Long? = null,
    val symptoms: Map<SymptomCategory, Int> = emptyMap()
) {
    fun current(goal: GoalType): Int =
        if (goal == GoalType.MEALS) meals.size else progress[goal] ?: 0

    fun target(goal: GoalType): Int = targets[goal] ?: goal.defaultTarget

    fun fraction(goal: GoalType): Float =
        (current(goal).toFloat() / target(goal).coerceAtLeast(1)).coerceIn(0f, 1f)

    val activeGoals: List<GoalType> get() = GoalType.entries.filter { it.trackItem in tracked }
    val completedCount: Int get() = activeGoals.count { current(it) >= target(it) }
    val todayPercent: Int
        get() = if (activeGoals.isEmpty()) 0
        else (activeGoals.sumOf { fraction(it).toDouble() } / activeGoals.size * 100).roundToInt()
}