package apps.farhan.newage.bloom.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apps.farhan.newage.bloom.ui.components.*
import apps.farhan.newage.bloom.ui.onboarding.TrackItem
import kotlin.math.roundToInt

// Demo data: the design shows no real history source
private enum class Period(val label: String, val unit: String, val bars: List<String>, val values: List<Float>) {
    WEEK("Week", "day", listOf("M", "T", "W", "T", "F", "S", "S"), listOf(0.55f, 0.85f, 0.65f, 0.75f, 0.80f, 0.85f, 0f)),
    MONTH("Month", "week", listOf("W1", "W2", "W3", "W4"), listOf(0.50f, 0.65f, 0.70f, 0.60f)),
}

private data class SymptomPattern(val name: String, val icon: ImageVector, val days: List<Int>)

private val symptomPatterns = listOf(
    SymptomPattern("Bloating", Icons.Outlined.Cloud, listOf(1, 0, 3, 3, 2, 3, 3)),
    SymptomPattern("Skin & acne", Icons.Outlined.AutoAwesome, listOf(0, 1, 2, 0, 1, 2, 1)),
    SymptomPattern("Mood", Icons.Outlined.SentimentSatisfied, listOf(1, 0, 2, 1, 0, 2, 1)),
    SymptomPattern("Sleep quality", Icons.Outlined.Bedtime, listOf(0, 2, 1, 0, 2, 1, 0)),
)

@Composable
fun InsightsScreen(state: HomeUiState) {
    var period by rememberSaveable { mutableStateOf(Period.WEEK) }
    var selected by rememberSaveable { mutableIntStateOf(-1) }
    val primary = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val score = (period.values.average() * 100).roundToInt()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 24.dp),
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
                BarChart(period.bars, period.values, selected) { selected = it }
                Spacer(Modifier.height(10.dp))
                Text(
                    if (selected >= 0) "${period.bars[selected]} · ${(period.values[selected] * 100).roundToInt()}%"
                    else "Tap a ${period.unit} to see its summary",
                    fontSize = 11.sp, color = muted,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }

        SectionTitle("Cycle")
        BloomRow(
            title = "No cycle logged yet", subtitle = "Log your last period date",
            leading = { IconBadge(TrackItem.CYCLE.icon, TrackItem.CYCLE.color) },
        )

        SectionTitle("Daily summary")
        BloomCard {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
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
        }

        SectionTitle("Symptom patterns")
        symptomPatterns.forEach { s ->
            val n = s.days.count { it >= 2 }
            BloomRow(
                title = s.name, subtitle = "Moderate or higher on $n of 7 days",
                leading = { IconBadge(s.icon, primary) },
                trailing = { Sparkline(s.days) },
            )
        }
    }
}

@Composable
private fun PeriodToggle(selected: Period, onSelect: (Period) -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant).padding(3.dp),
    ) {
        Period.entries.forEach { p ->
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
private fun BarChart(labels: List<String>, values: List<Float>, selected: Int, onSelect: (Int) -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        values.forEachIndexed { i, v ->
            Column(
                Modifier.weight(1f).clickable { onSelect(i) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(Modifier.height(110.dp).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                    Box(
                        Modifier
                            .fillMaxWidth(0.7f)
                            .fillMaxHeight(v.coerceAtLeast(0.03f))
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(if (i == selected) primary else primary.copy(alpha = 0.35f)),
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(labels[i], fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun Sparkline(days: List<Int>) {
    val primary = MaterialTheme.colorScheme.primary
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.Bottom) {
        days.forEach {
            Box(
                Modifier.width(4.dp).height((8 + it * 5).dp).clip(RoundedCornerShape(2.dp))
                    .background(primary.copy(alpha = if (it >= 2) 1f else 0.3f)),
            )
        }
    }
}