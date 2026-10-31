package apps.farhan.newage.bloom.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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

@Composable
fun DashboardScreen(
    state: HomeUiState,
    onOpenGoal: (GoalType) -> Unit,
    onGoTab: (HomeTab) -> Unit,
    onStartHere: () -> Unit,
    onTipDone: () -> Unit,
    onTipLater: () -> Unit,
) {
    val pink = MaterialTheme.colorScheme.primaryContainer

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        GreetingHeader(state.userName)
        TodayGoalsCard(state, onStartHere)

        SectionTitle("PCOS FAQ")
        FaqCarousel(onClick = { onGoTab(HomeTab.LEARN) })

        if (TrackItem.CYCLE in state.tracked) {
            BloomRow(
                title = "Cycle", subtitle = "Log your last period date",
                leading = { IconBadge(TrackItem.CYCLE.icon, TrackItem.CYCLE.color) },
                containerColor = pink, onClick = { onGoTab(HomeTab.INSIGHTS) },
            )
        }
        if (TrackItem.SYMPTOMS in state.tracked) {
            BloomRow(
                title = "Symptom check-in", subtitle = "Bloating, skin, mood, sleep quality",
                leading = { IconBadge(TrackItem.SYMPTOMS.icon, TrackItem.SYMPTOMS.color) },
                containerColor = pink, onClick = { onGoTab(HomeTab.INSIGHTS) },
            )
        }

        SectionTitle("Goal Progress")
        state.activeGoals.forEach { goal ->
            BloomRow(
                title = goal.title,
                subtitle = "${state.current(goal)}/${state.target(goal)} ${goal.unit}",
                leading = { ProgressRing(state.fraction(goal), goal.color, goal.icon) },
                onClick = { onOpenGoal(goal) },
            )
        }

        if (state.tipVisible) {
            SectionTitle("Today's tip")
            TipCard(onDone = onTipDone, onLater = onTipLater)
        }
    }
}

@Composable
private fun GreetingHeader(name: String) {
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
                        fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.SemiBold, fontSize = 18.sp,
                    ),
                ) { append(name) }
            },
            modifier = Modifier.weight(1f),
        )
        IconBadge(Icons.Outlined.Person, MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun TodayGoalsCard(state: HomeUiState, onStartHere: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val date = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()) }
    val total = state.activeGoals.size
    val fraction = if (total == 0) 0f else state.completedCount.toFloat() / total

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
                Text("Let's get blooming", style = BloomSerifItalic.copy(fontSize = 14.sp))
                Spacer(Modifier.height(6.dp))
                Text("${state.completedCount} of $total complete", fontSize = 12.sp, color = muted)
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier.width(140.dp).height(3.dp).clip(CircleShape),
                    color = primary,
                    trackColor = MaterialTheme.colorScheme.outline,
                )
            }
            TextButton(onClick = onStartHere) {
                Text("Start here", style = BloomSerifItalic.copy(fontSize = 16.sp), color = primary)
            }
        }
    }
}

@Composable
private fun FaqCarousel(onClick: () -> Unit) {
    val items = remember { LearnContent.sections.flatMap { it.items }.take(3) }
    val pagerState = rememberPagerState(pageCount = { items.size })
    val primary = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.outline

    Column {
        HorizontalPager(state = pagerState, pageSpacing = 12.dp) { page ->
            BloomRow(title = items[page].question, subtitle = items[page].answer, onClick = onClick)
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.align(Alignment.CenterHorizontally), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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

@Composable
private fun TipCard(onDone: () -> Unit, onLater: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF3EAD3))
            .padding(16.dp),
    ) {
        Row {
            IconBadge(Icons.Outlined.Bedtime, TrackItem.SLEEP.color, badgeSize = 32.dp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    "LOG LAST NIGHT'S SLEEP", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp, color = Color(0xFF8A6D3B),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "You haven't logged sleep yet. Consistent, close-to-8-hr nights can help keep your " +
                            "hormones more balanced. Avoiding phones and screens before bed makes it easier to fall asleep.",
                    fontSize = 13.sp, lineHeight = 19.sp,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.align(Alignment.End), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PillButton("Remind me later", onLater, Color(0xFFE6DFD0), Color(0xFF5B5146))
            PillButton("Mark as done", onDone, Color(0xFF6E9B73), Color.White, leadingIcon = Icons.Outlined.Check)
        }
    }
}