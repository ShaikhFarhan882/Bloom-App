package apps.farhan.newage.bloom.ui.home
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Schedule
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
import apps.farhan.newage.bloom.ui.onboarding.TimePickerDialog
import apps.farhan.newage.bloom.ui.onboarding.formatTime
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val Amber = Color(0xFFD4A24C)

/* ---------------------------------- Log Cycle ---------------------------------- */

@Composable
fun CycleLogSheet(lastPeriodDay: Long?, onSelect: (Long) -> Unit, onDismiss: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    val todayDay = remember { CycleDates.today() }
    val now = remember { Calendar.getInstance() }
    val nowYear = now.get(Calendar.YEAR)
    val nowMonth = now.get(Calendar.MONTH)
    var year by rememberSaveable { mutableIntStateOf(nowYear) }
    var month by rememberSaveable { mutableIntStateOf(nowMonth) }
    val atCurrentMonth = year == nowYear && month == nowMonth
    val nextDay = lastPeriodDay?.let { it + CycleDates.DEFAULT_LENGTH }

    val first = remember(year, month) { Calendar.getInstance().apply { clear(); set(year, month, 1) } }
    val offset = (first.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday-first grid
    val daysInMonth = first.getActualMaximum(Calendar.DAY_OF_MONTH)
    val rows = (offset + daysInMonth + 6) / 7
    val monthTitle = remember(year, month) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(first.time)
    }

    SheetScaffold("Log Cycle", onDismiss, "When did your last period start?") {
        BloomCard {
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        val t = year * 12 + month - 1
                        year = t / 12; month = t % 12
                    }) { Icon(Icons.Outlined.ChevronLeft, contentDescription = "Previous month") }
                    Text(
                        monthTitle, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                        style = BloomSerifItalic.copy(fontSize = 15.sp),
                    )
                    IconButton(
                        enabled = !atCurrentMonth,
                        onClick = {
                            val t = year * 12 + month + 1
                            year = t / 12; month = t % 12
                        },
                    ) { Icon(Icons.Outlined.ChevronRight, contentDescription = "Next month") }
                }
                Row(Modifier.fillMaxWidth()) {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                        Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 11.sp, color = muted)
                    }
                }
                repeat(rows) { r ->
                    Row(Modifier.fillMaxWidth()) {
                        repeat(7) { c ->
                            val day = r * 7 + c - offset + 1
                            if (day in 1..daysInMonth) {
                                val key = CycleDates.dayOf(year, month, day)
                                DayCell(
                                    day = day,
                                    selected = key == lastPeriodDay,
                                    expected = key == nextDay,
                                    enabled = key <= todayDay,
                                    onClick = { onSelect(key) },
                                    modifier = Modifier.weight(1f),
                                )
                            } else {
                                Spacer(Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            LegendDot(filled = true, color = primary, label = "Last period start")
            LegendDot(filled = false, color = Amber, label = "Next expected")
        }

        Spacer(Modifier.height(12.dp))
        val info = if (lastPeriodDay == null) {
            "Tap a date on the calendar above and Bloom will estimate your next period and let you know if it looks delayed."
        } else {
            val next = lastPeriodDay + CycleDates.DEFAULT_LENGTH
            "Last period started ${CycleDates.format(lastPeriodDay)}. Next expected around ${CycleDates.format(next)}." +
                    if (todayDay > next) " It may be running a little late." else ""
        }
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer).padding(14.dp),
        ) {
            Text(
                if (lastPeriodDay == null) "Log your last period" else "Last period logged",
                fontSize = 13.sp, fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(info, fontSize = 12.sp, lineHeight = 17.sp, color = muted)
        }

        Spacer(Modifier.height(16.dp))
        BloomPrimaryButton("Done", onClick = onDismiss)
    }
}

@Composable
private fun DayCell(
    day: Int,
    selected: Boolean,
    expected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier
            .aspectRatio(1f)
            .padding(3.dp)
            .clip(CircleShape)
            .then(if (selected) Modifier.background(scheme.primary) else Modifier)
            .then(if (expected && !selected) Modifier.border(1.5.dp, Amber, CircleShape) else Modifier)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "$day", fontSize = 12.sp,
            color = when {
                selected -> Color.White
                !enabled -> scheme.onSurfaceVariant.copy(alpha = 0.4f)
                else -> scheme.onSurface
            },
        )
    }
}

@Composable
private fun LegendDot(filled: Boolean, color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(8.dp).clip(CircleShape)
                .then(if (filled) Modifier.background(color) else Modifier.border(1.5.dp, color, CircleShape)),
        )
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/* ------------------------------- Today's symptoms ------------------------------ */

@Composable
fun SymptomsSheet(
    selected: Map<SymptomCategory, Int>,
    onSelect: (SymptomCategory, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    SheetScaffold(
        title = "Today's symptoms",
        onDismiss = onDismiss,
        subtitle = "These are separate from your daily goals — logging them just helps you (and " +
                "eventually your doctor) spot patterns over time.",
    ) {
        SymptomCategory.entries.forEach { cat ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(cat.icon, contentDescription = null, tint = primary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(cat.label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = primary)
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                cat.options.forEachIndexed { i, label ->
                    ChoiceChip(
                        label = label,
                        selected = selected[cat] == i,
                        onClick = { onSelect(cat, i) },
                        modifier = Modifier.weight(1f),
                        fontSize = 12.sp,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        BloomPrimaryButton("Done", onClick = onDismiss)
    }
}

/* -------------------------------- Edit profile -------------------------------- */

@Composable
fun EditProfileSheet(
    name: String,
    stage: JourneyStage?,
    onSave: (String, JourneyStage?) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by rememberSaveable { mutableStateOf(name) }
    var selected by rememberSaveable { mutableStateOf(stage) }

    SheetScaffold("Edit Profile", onDismiss) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it.take(40) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Your first name") },
        )
        Spacer(Modifier.height(14.dp))
        Text("Journey stage", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        JourneyStage.entries.forEach { s ->
            OptionRow(
                title = s.title,
                subtitle = s.subtitle,
                selected = selected == s,
                onClick = { selected = s },
            )
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(8.dp))
        BloomPrimaryButton(
            "Save",
            enabled = text.isNotBlank(),
            onClick = { onSave(text, selected); onDismiss() },
        )
    }
}

/* -------------------------------- Reminder time -------------------------------- */

private data class ReminderPreset(val title: String, val hour: Int, val minute: Int, val blurb: String)

private val reminderPresets = listOf(
    ReminderPreset("Morning", 7, 30, "start the day on track"),
    ReminderPreset("Afternoon", 13, 0, "a midday check-in"),
    ReminderPreset("Evening", 20, 0, "wind down and reflect"),
)

@Composable
fun ReminderTimeSheet(
    hour: Int,
    minute: Int,
    onSave: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var h by rememberSaveable { mutableIntStateOf(hour) }
    var m by rememberSaveable { mutableIntStateOf(minute) }
    var showPicker by rememberSaveable { mutableStateOf(false) }

    SheetScaffold("Reminder Time", onDismiss, "When should Bloom nudge you to log your day?") {
        reminderPresets.forEach { p ->
            OptionRow(
                title = p.title,
                subtitle = "${formatTime(p.hour, p.minute).removePrefix("0")} — ${p.blurb}",
                selected = h == p.hour && m == p.minute,
                onClick = { h = p.hour; m = p.minute },
            )
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(6.dp))
        Text("Or set a custom time", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        BloomCard(onClick = { showPicker = true }) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(formatTime(h, m), fontSize = 14.sp, modifier = Modifier.weight(1f))
                Icon(Icons.Outlined.Schedule, contentDescription = "Pick a time", modifier = Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.height(20.dp))
        BloomPrimaryButton("Save", onClick = { onSave(h, m); onDismiss() })
    }

    if (showPicker) {
        TimePickerDialog(
            initialHour = h,
            initialMinute = m,
            onConfirm = { nh, nm -> h = nh; m = nm; showPicker = false },
            onDismiss = { showPicker = false },
        )
    }
}

/* ---------------------------- Live notification preview ---------------------------- */

private enum class PreviewTab(val label: String) {
    MEDITATION("Meditation"), DAILY_GOAL("Daily goal"), WATER("Water")
}

private const val MEDITATION_SECONDS = 300

@Composable
fun NotificationPreviewSheet(state: HomeUiState, onDismiss: () -> Unit) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    var tab by rememberSaveable { mutableStateOf(PreviewTab.MEDITATION) }
    var remaining by rememberSaveable { mutableIntStateOf(MEDITATION_SECONDS) }
    var running by rememberSaveable { mutableStateOf(false) }

    // Live countdown: this is the "live update" the preview demonstrates
    LaunchedEffect(running) {
        while (running && remaining > 0) {
            delay(1000)
            remaining--
        }
        running = false
    }

    val now = remember { Date() }
    val clock = remember { SimpleDateFormat("h:mm a", Locale.getDefault()).format(now) }
    val date = remember { SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(now) }

    val (headline, progress) = when (tab) {
        PreviewTab.MEDITATION ->
            (if (remaining == 0) "Meditation complete"
            else "Meditation: ${remaining / 60}:${"%02d".format(remaining % 60)} remaining") to
                    (1f - remaining / MEDITATION_SECONDS.toFloat())
        PreviewTab.DAILY_GOAL ->
            "Daily goal: ${state.completedCount} of ${state.activeGoals.size} complete" to
                    (if (state.activeGoals.isEmpty()) 0f else state.completedCount.toFloat() / state.activeGoals.size)
        PreviewTab.WATER ->
            "Water: ${state.current(GoalType.WATER)}/${state.target(GoalType.WATER)} glasses" to
                    state.fraction(GoalType.WATER)
    }
    val isMeditation = tab == PreviewTab.MEDITATION

    SheetScaffold(
        title = "Live Notification Preview",
        onDismiss = onDismiss,
        subtitle = "A preview of how Bloom could show live, real-time updates on the lock screen.",
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PreviewTab.entries.forEach { t ->
                ChoiceChip(
                    label = t.label,
                    selected = tab == t,
                    onClick = { tab = t },
                    modifier = Modifier.weight(1f),
                    fontSize = 12.sp,
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Column(
            Modifier
                .align(Alignment.CenterHorizontally)
                .widthIn(max = 280.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF111111))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(clock, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Medium)
            Text(date, color = Color(0xFF9A9A9A), fontSize = 11.sp)
            Spacer(Modifier.height(16.dp))
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF2A2A2A)).padding(12.dp),
            ) {
                Row {
                    Text("Bloom", color = Color(0xFFBDBDBD), fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Text("now", color = Color(0xFF8A8A8A), fontSize = 11.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text(headline, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(3.dp).clip(CircleShape),
                    color = Color.White,
                    trackColor = Color(0xFF4A4A4A),
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    if (remaining == 0) remaining = MEDITATION_SECONDS
                    running = true
                },
                enabled = isMeditation && !running,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                shape = RoundedCornerShape(14.dp),
            ) { Text("Begin") }
            Button(
                onClick = { running = false; remaining = MEDITATION_SECONDS },
                enabled = isMeditation,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) { Text("Reset") }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "Updates live while a meditation session is running — same pattern as an order-tracking " +
                    "or workout notification.",
            fontSize = 11.sp, lineHeight = 15.sp, color = muted, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}