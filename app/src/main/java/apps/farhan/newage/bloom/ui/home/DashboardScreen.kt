package apps.farhan.newage.bloom.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apps.farhan.newage.bloom.ui.components.*
import apps.farhan.newage.bloom.ui.onboarding.TrackItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val Sage = Color(0xFF6E9B73)

@Composable
fun DashboardScreen(
    state: HomeUiState,
    onOpenGoal: (GoalType) -> Unit,
    onGoTab: (HomeTab) -> Unit,
    onOpenCycle: () -> Unit,
    onOpenSymptoms: () -> Unit,
    onStartHere: () -> Unit,
    onTipDone: () -> Unit,
    onTipLater: () -> Unit,
) {
    val pink = MaterialTheme.colorScheme.primaryContainer
    val primary = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        GreetingHeader(state.userName, onProfileClick = { onGoTab(HomeTab.SETTINGS) })
        TodayGoalsCard(state, onStartHere)

        SectionTitle("PCOS FAQ")
        FaqCarousel(onClick = { onGoTab(HomeTab.LEARN) })

        if (TrackItem.CYCLE in state.tracked) {
            val cycleDay = remember(state.lastPeriodDay) {
                state.lastPeriodDay?.let { (CycleDates.today() - it + 1).toInt().coerceAtLeast(1) }
            }
            BloomRow(
                title = "Cycle",
                subtitle = cycleSummary(state),
                leading = { IconBadge(TrackItem.CYCLE.icon, TrackItem.CYCLE.color) },
                trailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        cycleDay?.let {
                            Text("Day $it", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = primary)
                            Spacer(Modifier.width(6.dp))
                        }
                        ChevronIcon()
                    }
                },
                containerColor = pink,
                onClick = onOpenCycle,
            )
        }

        if (TrackItem.SYMPTOMS in state.tracked) {
            val logged = state.symptoms.size
            BloomRow(
                title = "Symptom check-in",
                subtitle = if (logged == 0) "Bloating, skin, mood, sleep quality"
                else "$logged of ${SymptomCategory.entries.size} logged today",
                leading = { IconBadge(TrackItem.SYMPTOMS.icon, TrackItem.SYMPTOMS.color) },
                containerColor = pink,
                onClick = onOpenSymptoms,
            )
        }

        SectionTitle("Goal Progress")
        if (state.activeGoals.isEmpty()) {
            Text(
                "Nothing to track yet. Turn on some goals in Settings.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable { onGoTab(HomeTab.SETTINGS) }.padding(vertical = 8.dp),
            )
        } else {
            state.activeGoals.forEach { goal ->
                GoalRow(goal, state, onClick = { onOpenGoal(goal) })
            }
        }

        if (state.tipVisible) {
            SectionTitle("Today's tip")
            TipSection(state, onOpenGoal, onTipDone, onTipLater)
        }
    }
}

/* ------------------------------- Header & hero card ------------------------------- */

@Composable
private fun GreetingHeader(name: String, onProfileClick: () -> Unit) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "GOOD MORNING"
            in 12..16 -> "GOOD AFTERNOON"
            else -> "GOOD EVENING"
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontSize = 11.sp, letterSpacing = 1.2.sp, color = muted)) {
                    append("$greeting, ")
                }
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                    ),
                ) { append(name) }
            },
            modifier = Modifier.weight(1f),
        )
        IconBadge(
            icon = Icons.Outlined.Person,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clip(CircleShape).clickable(onClick = onProfileClick),
        )
    }
}

@Composable
private fun TodayGoalsCard(state: HomeUiState, onStartHere: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val date = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()) }
    val total = state.activeGoals.size
    val done = state.completedCount
    val allDone = total > 0 && done == total
    val fraction = if (total == 0) 0f else done.toFloat() / total
    val tagline = when {
        allDone -> "You've bloomed today"
        done > 0 -> "Keep blooming"
        else -> "Let's get blooming"
    }

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFFF8E3DF), Color(0xFFFFF4F1)))),
    ) {
        BloomLogo(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp).alpha(0.12f),
            logoSize = 110.dp,
        )
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Today's Goals", style = BloomSerifItalic.copy(fontSize = 20.sp), color = primary)
                Text(date, fontSize = 12.sp, color = muted)
                Spacer(Modifier.height(8.dp))
                Text(tagline, style = BloomSerifItalic.copy(fontSize = 14.sp))
                Spacer(Modifier.height(6.dp))
                Text("$done of $total complete", fontSize = 12.sp, color = muted)
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier.width(140.dp).height(3.dp).clip(CircleShape),
                    color = primary,
                    trackColor = MaterialTheme.colorScheme.outline,
                )
            }
            TextButton(onClick = onStartHere, enabled = !allDone && total > 0) {
                Text(
                    if (allDone) "All done" else "Start here",
                    style = BloomSerifItalic.copy(fontSize = 16.sp),
                    color = if (allDone) muted else primary,
                )
            }
        }
    }
}

/* ----------------------------------- FAQ pager ----------------------------------- */

// Learn answers that are still placeholders are hidden in the preview row
private fun FaqItem.preview(): String? = answer.takeIf { !it.startsWith("Explainer content") }

@Composable
private fun FaqCarousel(onClick: () -> Unit) {
    val items = remember { LearnContent.sections.flatMap { it.items } }
    val pagerState = rememberPagerState(pageCount = { items.size })
    val primary = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.outline

    Column {
        HorizontalPager(state = pagerState, pageSpacing = 12.dp) { page ->
            val item = items[page]
            BloomRow(title = item.question, subtitle = item.preview(), onClick = onClick)
        }
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier.align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            repeat(items.size) { i ->
                val selected = pagerState.currentPage == i
                Box(
                    Modifier
                        .height(3.dp)
                        .width(if (selected) 14.dp else 4.dp)
                        .clip(CircleShape)
                        .background(if (selected) primary else outline),
                )
            }
        }
    }
}

/* ---------------------------------- Goal progress ---------------------------------- */

@Composable
private fun GoalRow(goal: GoalType, state: HomeUiState, onClick: () -> Unit) {
    val current = state.current(goal)
    val target = state.target(goal)
    val done = current >= target
    BloomRow(
        title = goal.title,
        subtitle = "$current/$target ${goal.unit}" + if (done) " · Done" else "",
        leading = { ProgressRing(state.fraction(goal), goal.color, goal.icon) },
        trailing = {
            if (done) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = "Complete", tint = Sage, modifier = Modifier.size(20.dp))
            } else {
                ChevronIcon()
            }
        },
        onClick = onClick,
    )
}

/* ------------------------------------ Today's tip ------------------------------------ */

@Composable
private fun TipSection(
    state: HomeUiState,
    onOpenGoal: (GoalType) -> Unit,
    onDone: () -> Unit,
    onLater: () -> Unit,
) {
    val open = state.activeGoals.filter { state.current(it) < state.target(it) }
    // Prefer the sleep tip (matches the design), otherwise the first unfinished goal
    val goal = open.firstOrNull { it == GoalType.SLEEP } ?: open.firstOrNull()

    if (goal == null) {
        AllDoneCard(hasGoals = state.activeGoals.isNotEmpty())
    } else {
        val (title, body) = amountTip(goal, state.current(goal), state.target(goal))
        TipCard(
            goal = goal,
            title = title.uppercase(),
            body = body,
            onOpen = { onOpenGoal(goal) },
            onDone = onDone,
            onLater = onLater,
        )
    }
}

@Composable
private fun TipCard(
    goal: GoalType,
    title: String,
    body: String,
    onOpen: () -> Unit,
    onDone: () -> Unit,
    onLater: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF3EAD3))
            .padding(16.dp),
    ) {
        Row(Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onOpen)) {
            IconBadge(goal.icon, goal.color, badgeSize = 32.dp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    title, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp, color = Color(0xFF8A6D3B),
                )
                Spacer(Modifier.height(4.dp))
                Text(body, fontSize = 13.sp, lineHeight = 19.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.align(Alignment.End), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PillButton("Remind me later", onLater, Color(0xFFE6DFD0), Color(0xFF5B5146))
            PillButton("Mark as done", onDone, Sage, Color.White, leadingIcon = Icons.Outlined.Check)
        }
    }
}

@Composable
private fun AllDoneCard(hasGoals: Boolean) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFE5F0E6))
            .padding(16.dp),
    ) {
        Text(
            if (hasGoals) "ALL GOALS COMPLETE" else "NO GOALS ON",
            fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp, color = Color(0xFF4F7A54),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            if (hasGoals) "Lovely work today. Rest well and come back tomorrow to keep your streak of gentle habits going."
            else "Turn on the goals you want to follow in Settings and your tips will show up here.",
            fontSize = 13.sp, lineHeight = 19.sp,
        )
    }
}