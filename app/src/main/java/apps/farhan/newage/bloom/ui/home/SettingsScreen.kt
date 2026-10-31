package apps.farhan.newage.bloom.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apps.farhan.newage.bloom.ui.components.*
import apps.farhan.newage.bloom.ui.onboarding.TrackItem
import apps.farhan.newage.bloom.ui.onboarding.formatTime

@Composable
fun SettingsScreen(
    state: HomeUiState,
    onTrackToggle: (TrackItem, Boolean) -> Unit,
    onTargetChange: (GoalType, Int) -> Unit,
    onNotifications: (Boolean) -> Unit,
    onReminderClick: () -> Unit,
    onPreviewClick: () -> Unit,
    onEditProfile: () -> Unit,
    onSignOut: () -> Unit,
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ScreenHeader("Settings", "Your profile and daily goal targets")

        BloomCard {
            Column {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(Icons.Outlined.Person, MaterialTheme.colorScheme.primary, badgeSize = 44.dp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(state.userName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(
                            "PCOS journey · ${state.journeyStage?.title ?: "Not set"}",
                            fontSize = 12.sp, color = muted,
                        )
                    }
                }
                SettingsDivider()
                SettingsRow("Edit profile", onClick = onEditProfile)
            }
        }

        SectionTitle("What you're tracking")
        BloomCard {
            Column {
                TRACKING_DISPLAY_ORDER.forEachIndexed { i, item ->
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconBadge(item.icon, item.color, badgeSize = 32.dp)
                        Spacer(Modifier.width(12.dp))
                        Text(item.title, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Switch(
                            checked = item in state.tracked,
                            onCheckedChange = { onTrackToggle(item, it) },
                            colors = bloomSwitchColors(),
                        )
                    }
                    if (i < TRACKING_DISPLAY_ORDER.lastIndex) SettingsDivider()
                }
            }
        }

        SectionTitle("Daily goal targets")
        BloomCard {
            Column {
                GoalType.entries.forEachIndexed { i, goal ->
                    val target = state.target(goal)
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconBadge(goal.icon, goal.color, badgeSize = 32.dp)
                        Spacer(Modifier.width(12.dp))
                        Text(goal.title, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        StepperButton("–", enabled = target > goal.step, onClick = { onTargetChange(goal, -1) }, buttonSize = 26.dp)
                        Text(
                            "$target ${goal.unit}", fontSize = 12.sp, fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center, modifier = Modifier.widthIn(min = 64.dp),
                        )
                        StepperButton("+", enabled = target < goal.max, onClick = { onTargetChange(goal, 1) }, buttonSize = 26.dp)
                    }
                    if (i < GoalType.entries.lastIndex) SettingsDivider()
                }
            }
        }

        BloomCard {
            Column {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Notifications", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Daily reminders to log", fontSize = 12.sp, color = muted)
                    }
                    Switch(
                        checked = state.notificationsOn,
                        onCheckedChange = onNotifications,
                        colors = bloomSwitchColors(),
                    )
                }
                SettingsDivider()
                SettingsRow(
                    "Reminder time",
                    subtitle = formatTime(state.reminderHour, state.reminderMinute),
                    onClick = onReminderClick,
                )
                SettingsDivider()
                SettingsRow("Live notification preview", subtitle = "See lock-screen updates", onClick = onPreviewClick)
                SettingsDivider()
                Row(Modifier.fillMaxWidth().padding(14.dp)) {
                    Text("App version", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Text("1.0 · Prototype", fontSize = 12.sp, color = muted)
                }
            }
        }

        OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            shape = RoundedCornerShape(14.dp),
        ) { Text("Sign out") }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
}

@Composable
private fun SettingsRow(title: String, subtitle: String? = null, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            subtitle?.let { Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        ChevronIcon()
    }
}