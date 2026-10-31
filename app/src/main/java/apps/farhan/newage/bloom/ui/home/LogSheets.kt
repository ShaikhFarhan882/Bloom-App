package apps.farhan.newage.bloom.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apps.farhan.newage.bloom.ui.components.BloomPrimaryButton
import apps.farhan.newage.bloom.ui.components.BloomSerifItalic
import apps.farhan.newage.bloom.ui.components.StepperButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetScaffold(
    title: String,
    subtitle: String,
    onDismiss: () -> Unit,
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
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            content()
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
    SheetScaffold("Log Meals", "How would you describe each meal today?", onDismiss) {
        MealSlot.entries.forEach { slot ->
            Text(slot.label, fontSize = 13.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
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
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) scheme.primaryContainer else androidx.compose.ui.graphics.Color.White,
        border = BorderStroke(1.dp, if (selected) scheme.primary else scheme.outline),
    ) {
        Box(Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            Text(
                label, fontSize = 13.sp,
                color = if (selected) scheme.primary else scheme.onSurface,
            )
        }
    }
}

@Composable
fun AmountLogSheet(
    goal: GoalType,
    current: Int,
    target: Int,
    onSave: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var value by rememberSaveable { mutableIntStateOf(current) }
    SheetScaffold("Log ${goal.title}", "Today's total · goal $target ${goal.unit}", onDismiss) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepperButton("−", enabled = value > 0, onClick = { value = (value - goal.step).coerceAtLeast(0) })
            Column(Modifier.padding(horizontal = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$value", style = BloomSerifItalic.copy(fontSize = 40.sp))
                Text(goal.unit, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StepperButton("+", enabled = value < goal.max, onClick = { value = (value + goal.step).coerceAtMost(goal.max) })
        }
        Spacer(Modifier.height(20.dp))
        BloomPrimaryButton("Done", onClick = { onSave(value); onDismiss() })
    }
}

@Composable
fun EditProfileSheet(name: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var text by rememberSaveable { mutableStateOf(name) }
    SheetScaffold("Edit Profile", "How should we greet you?", onDismiss) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it.take(40) },
            label = { Text("Your first name") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(20.dp))
        BloomPrimaryButton("Save", enabled = text.isNotBlank(), onClick = { onSave(text); onDismiss() })
    }
}