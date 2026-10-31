package apps.farhan.newage.bloom.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apps.farhan.newage.bloom.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetScaffold(
    title: String,
    onDismiss: () -> Unit,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = BloomSerifItalic.copy(fontSize = 24.sp), modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) { Icon(Icons.Outlined.Close, contentDescription = "Close") }
            }
            subtitle?.let {
                Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
internal fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 13.sp,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) scheme.primaryContainer else Color.White,
        border = BorderStroke(1.dp, if (selected) scheme.primary else scheme.outline),
    ) {
        Box(Modifier.padding(horizontal = 4.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
            Text(
                label, fontSize = fontSize, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis,
                color = if (selected) scheme.primary else scheme.onSurface,
            )
        }
    }
}

@Composable
fun MealLogSheet(
    meals: Map<MealSlot, MealChoice>,
    target: Int,
    onChoose: (MealSlot, MealChoice) -> Unit,
    onDismiss: () -> Unit,
) {
    SheetScaffold("Log Meals", onDismiss, "How would you describe each meal today?") {
        MealSlot.entries.forEach { slot ->
            Text(
                slot.label, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))
            MealChoice.entries.chunked(2).forEach { pair ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    pair.forEach { choice ->
                        ChoiceChip(
                            label = choice.label,
                            selected = meals[slot] == choice,
                            onClick = { onChoose(slot, choice) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(8.dp))
        }
        Text(
            "${meals.size} of $target meals logged today",
            fontSize = 12.sp, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun AmountLogSheet(
    goal: GoalType,
    current: Int,
    target: Int,
    onChange: (Int) -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val quickAdd = when (goal) {
        GoalType.MOVEMENT -> 10
        GoalType.RELAXATION -> 5
        else -> 1
    }
    val (tipTitle, tipBody) = amountTip(goal, current, target)

    SheetScaffold("Log ${goal.title}", onDismiss, "Today's ${goal.title.lowercase()}") {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepperButton("−", enabled = current > 0, onClick = { onChange((current - goal.step).coerceAtLeast(0)) })
            Row(Modifier.padding(horizontal = 24.dp), verticalAlignment = Alignment.Bottom) {
                Text("$current", style = BloomSerifItalic.copy(fontSize = 36.sp))
                Spacer(Modifier.width(4.dp))
                Text(goal.unit, fontSize = 13.sp, color = muted, modifier = Modifier.padding(bottom = 6.dp))
            }
            StepperButton("+", enabled = current < goal.max, onClick = { onChange((current + goal.step).coerceAtMost(goal.max)) })
        }

        Spacer(Modifier.height(8.dp))
        Row(Modifier.align(Alignment.CenterHorizontally)) {
            Text("Goal $target ${goal.unit} · adjust in ", fontSize = 12.sp, color = muted)
            Text(
                "Settings", fontSize = 12.sp, color = Color(0xFF3B5BDB),
                modifier = Modifier.clickable(onClick = onOpenSettings),
            )
        }

        Spacer(Modifier.height(16.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(goal.icon, contentDescription = null, tint = goal.color, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(tipTitle, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            Text(tipBody, fontSize = 12.sp, lineHeight = 17.sp, color = muted)
            Spacer(Modifier.height(10.dp))
            PillButton(
                text = "+$quickAdd ${goal.unit}",
                onClick = { onChange((current + quickAdd).coerceAtMost(goal.max)) },
                container = Color.White,
                content = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

internal fun amountTip(goal: GoalType, current: Int, target: Int): Pair<String, String> {
    if (current >= target) {
        return "Goal reached" to "You've hit today's ${goal.title.lowercase()} goal. Nice work."
    }
    if (current > 0) {
        return "Keep going" to "You're at $current of $target ${goal.unit}. Every bit counts."
    }
    return when (goal) {
        GoalType.MOVEMENT -> "Get moving today" to
                "No movement logged yet today. Try a brisk 30-minute walk, a swim, or some light strength " +
                "training — these are especially good for insulin sensitivity with PCOS."
        GoalType.WATER -> "Start hydrating" to
                "No water logged yet today. Try starting with a glass first thing in the morning."
        GoalType.RELAXATION -> "Make time to unwind" to
                "No relaxation time logged yet. Even 5-10 minutes of deep breathing, meditation, or gentle " +
                "yoga can help lower stress hormones."
        GoalType.SLEEP -> "Log last night's sleep" to
                "You haven't logged sleep yet. Consistent, close-to-8-hr nights can help keep your hormones " +
                "more balanced — avoiding phones and screens before bed makes it easier to fall asleep."
        GoalType.MEALS -> "Log your meals" to "No meals logged yet today."
    }
}