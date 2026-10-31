package apps.farhan.newage.bloom.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apps.farhan.newage.bloom.ui.components.*
import apps.farhan.newage.bloom.ui.onboarding.TrackItem
import java.util.Calendar
import kotlin.math.roundToInt

private enum class InsightPeriod(val label: String, val unit: String) {
    WEEK("Week", "day"),
    MONTH("Month", "week"),
}

private val DAY_LABELS = listOf("M", "T", "W", "T", "F", "S", "S")
private val DAY_NAMES = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

// The app does not store history yet, so earlier days use demo values.
// Today's bar and score are always computed from the real goal progress.
private val DEMO_WEEK = listOf(0.55f, 0.85f, 0.65f, 0.75f, 0.80f, 0.85f, 0.70f)
private val DEMO_MONTH = listOf(0.50f, 0.65f, 0.70f)
private val DEMO_SYMPTOM_HISTORY = mapOf(
    SymptomCategory.BLOATING to listOf(1, 0, 3, 3, 2, 3),
    SymptomCategory.SKIN to listOf(0, 1, 2, 0, 1, 2),
    SymptomCategory.MOOD to listOf(1, 0, 2, 1, 0, 2),
    SymptomCategory.SLEEP_QUALITY to listOf(0, 2, 1, 0, 2, 1),
)

/** Monday = 0 ... Sunday = 6 */
private fun mondayFirstIndex(): Int = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7

@Composable
fun InsightsScreen(
    state: HomeUiState,
    onOpenCycle: () -> Unit,
    onOpenSymptoms: () -> Unit,
    onOpenGoal: (GoalType) -> Unit,
) {
    var period by rememberSaveable { mutableStateOf(InsightPeriod.WEEK) }
    var selected by rememberSaveable { mutableIntStateOf(-1) }
    val primary = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    val todayIdx = remember { mondayFirstIndex() }
    val weekValues = remember(state.todayPercent, todayIdx) {
        List(7) { i ->
            when {
                i < todayIdx -> DEMO_WEEK[i]
                i == todayIdx -> state.todayPercent / 100f
                else -> 0f
            }
        }
    }
    val weekAverage = weekValues.take(todayIdx + 1).average().toFloat()
    val monthValues = DEMO_MONTH + weekAverage

    val isWeek = period == InsightPeriod.WEEK
    val values = if (isWeek) weekValues else monthValues
    val labels = if (isWeek) DAY_LABELS else listOf("W1", "W2", "W3", "W4")
    val futureFrom = if (isWeek) todayIdx + 1 else values.size
    val score = ((if (isWeek) weekAverage else monthValues.average().toFloat()) * 100).roundToInt()

    val hint = when {
        selected < 0 -> "Tap a ${period.unit} to see its summary"
        isWeek && selected == todayIdx ->
            "Today · ${state.completedCount} of ${state.activeGoals.size} goals complete"
        isWeek -> "${DAY_NAMES[selected]} · ${(values[selected] * 100).roundToInt()}%"
        else -> "Week ${selected + 1} · ${(values[selected] * 100).roundToInt()}%"
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ScreenHeader("Insights", "Your week at a glance, and what to try next")

        SectionTitle("Your score")
        BloomCard {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text("$score%", style = BloomSerifItalic.copy(fontSize = 32.sp), color = primary)
                        Text("${period.label.lowercase()} average", fontSize = 12.sp, color = muted)
                    }
                    PeriodToggle(period) { period = it; selected = -1 }
                }
                Spacer(Modifier.height(16.dp))
                BarChart(labels, values, selected, futureFrom) { selected = it }
                Spacer(Modifier.height(10.dp))
                Text(
                    hint, fontSize = 11.sp, color = muted,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }

        SectionTitle("Cycle")
        CycleCard(state, onOpenCycle)

        SectionTitle("Daily summary")
        DailySummaryCard(state, onOpenGoal)

        val next = state.activeGoals
            .filter { state.current(it) < state.target(it) }
            .minByOrNull { state.fraction(it) }
        if (next != null) {
            SectionTitle("Try next")
            BloomRow(
                title = "Log ${next.title.lowercase()}",
                subtitle = "${state.target(next) - state.current(next)} ${next.unit} to reach today's goal",
                leading = { IconBadge(next.icon, next.color) },
                onClick = { onOpenGoal(next) },
            )
        }

        SectionTitle("Symptom patterns")
        SymptomCategory.entries.forEach { cat ->
            val days = DEMO_SYMPTOM_HISTORY.getValue(cat) + (state.symptoms[cat] ?: -1)
            val n = days.count { it >= 2 }
            BloomRow(
                title = cat.label,
                subtitle = "Moderate or higher on $n of 7 days",
                leading = { IconBadge(cat.icon, primary) },
                trailing = { Sparkline(days) },
                onClick = onOpenSymptoms,
            )
        }
        Text(
            "Patterns are for your own tracking and are not medical advice. Share them with your doctor if you're concerned.",
            fontSize = 11.sp, lineHeight = 15.sp, color = muted, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        )
    }
}

/* ----------------------------------- Cycle card ----------------------------------- */

@Composable
private fun CycleCard(state: HomeUiState, onClick: () -> Unit) {
    val last = state.lastPeriodDay
    if (last == null) {
        BloomRow(
            title = "No cycle logged yet",
            subtitle = "Log your last period date",
            leading = { IconBadge(TrackItem.CYCLE.icon, TrackItem.CYCLE.color) },
            onClick = onClick,
        )
        return
    }

    val today = remember { CycleDates.today() }
    val length = CycleDates.DEFAULT_LENGTH
    val cycleDay = (today - last + 1).toInt().coerceAtLeast(1)
    val progress = (cycleDay.toFloat() / length).coerceIn(0f, 1f)
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    BloomCard(onClick = onClick) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(TrackItem.CYCLE.icon, TrackItem.CYCLE.color)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Cycle day $cycleDay", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Next period around ${CycleDates.format(last + length)}",
                        fontSize = 12.sp, color = muted,
                    )
                }
                ChevronIcon()
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = TrackItem.CYCLE.color,
                trackColor = MaterialTheme.colorScheme.outline,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (cycleDay > length) "Running ${cycleDay - length} day(s) past the typical $length-day estimate"
                else "Based on a typical $length-day cycle",
                fontSize = 11.sp, color = muted,
            )
        }
    }
}

/* -------------------------------- Daily summary card -------------------------------- */

@Composable
private fun DailySummaryCard(state: HomeUiState, onOpenGoal: (GoalType) -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    BloomCard {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("${state.todayPercent}%", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Today", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(
                        if (state.todayPercent == 0) "Nothing logged yet today. No rush, tap a goal whenever you're ready."
                        else "${state.completedCount} of ${state.activeGoals.size} goals complete so far today.",
                        fontSize = 12.sp, color = muted,
                    )
                }
            }
            if (state.activeGoals.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                state.activeGoals.forEach { goal -> GoalMiniRow(goal, state) { onOpenGoal(goal) } }
            }
        }
    }
}

@Composable
private fun GoalMiniRow(goal: GoalType, state: HomeUiState, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(goal.icon, contentDescription = null, tint = goal.color, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row {
                Text(goal.title, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text(
                    "${state.current(goal)}/${state.target(goal)} ${goal.unit}",
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { state.fraction(goal) },
                modifier = Modifier.fillMaxWidth().height(3.dp).clip(CircleShape),
                color = goal.color,
                trackColor = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

/* -------------------------------- Chart & small parts -------------------------------- */

@Composable
private fun PeriodToggle(selected: InsightPeriod, onSelect: (InsightPeriod) -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant).padding(3.dp),
    ) {
        InsightPeriod.entries.forEach { p ->
            val isSelected = p == selected
            Text(
                p.label, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.White else muted,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) primary else Color.Transparent)
                    .clickable { onSelect(p) }
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            )
        }
    }
}

@Composable
private fun BarChart(
    labels: List<String>,
    values: List<Float>,
    selected: Int,
    futureFrom: Int,
    onSelect: (Int) -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        values.forEachIndexed { i, v ->
            val isFuture = i >= futureFrom
            Column(
                Modifier.weight(1f).clickable(enabled = !isFuture) { onSelect(i) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(Modifier.height(110.dp).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                    Box(
                        Modifier
                            .fillMaxWidth(0.7f)
                            .fillMaxHeight(if (isFuture) 0.03f else v.coerceAtLeast(0.03f))
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                when {
                                    i == selected -> primary
                                    isFuture -> primary.copy(alpha = 0.15f)
                                    else -> primary.copy(alpha = 0.35f)
                                },
                            ),
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(labels[i], fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** One bar per day. A negative value means "not logged" and is drawn faint. */
@Composable
private fun Sparkline(days: List<Int>) {
    val primary = MaterialTheme.colorScheme.primary
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.Bottom) {
        days.forEach { v ->
            val logged = v >= 0
            Box(
                Modifier
                    .width(4.dp)
                    .height(if (logged) (8 + v * 5).dp else 6.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        primary.copy(
                            alpha = when {
                                !logged -> 0.15f
                                v >= 2 -> 1f
                                else -> 0.3f
                            },
                        ),
                    ),
            )
        }
    }
}