package apps.farhan.newage.bloom.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import apps.farhan.newage.bloom.ads.AdaptiveBanner
import apps.farhan.newage.bloom.appContainer

@Composable
fun HomeScreen(onSignedOut: () -> Unit) {
    val container = LocalContext.current.appContainer()
    val vm: HomeViewModel = viewModel(
        factory = viewModelFactory { initializer { HomeViewModel(container.userPreferences) } },
    )
    val state by vm.state.collectAsStateWithLifecycle()

    var tab by rememberSaveable { mutableStateOf(HomeTab.DASHBOARD) }
    var goalSheet by rememberSaveable { mutableStateOf<GoalType?>(null) }
    var showEditProfile by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = tab != HomeTab.DASHBOARD) { tab = HomeTab.DASHBOARD }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // Banner sits between the content and the navigation, never over either
            Column {
                AdaptiveBanner()
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                BloomBottomBar(selected = tab, onSelect = { tab = it })
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                HomeTab.DASHBOARD -> DashboardScreen(
                    state = state,
                    onOpenGoal = { goalSheet = it },
                    onGoTab = { tab = it },
                    onStartHere = {
                        state.activeGoals
                            .firstOrNull { state.current(it) < state.target(it) }
                            ?.let { goalSheet = it }
                    },
                    onTipDone = vm::dismissTip,
                    onTipLater = vm::dismissTip,
                )
                HomeTab.INSIGHTS -> InsightsScreen(state)
                HomeTab.LEARN -> LearnScreen()
                HomeTab.SETTINGS -> SettingsScreen(
                    state = state,
                    onTrackToggle = vm::setTracking,
                    onTargetChange = vm::changeTarget,
                    onNotifications = vm::setNotifications,
                    onReminderTime = vm::setReminderTime,
                    onEditProfile = { showEditProfile = true },
                    onSignOut = {
                        container.appOpenAdManager.disableForegroundAds()
                        vm.signOut(onSignedOut)
                    },
                )
            }
        }
    }

    goalSheet?.let { goal ->
        if (goal == GoalType.MEALS) {
            MealLogSheet(
                meals = state.meals,
                target = state.target(goal),
                onChoose = vm::setMeal,
                onDismiss = { goalSheet = null },
            )
        } else {
            AmountLogSheet(
                goal = goal,
                current = state.current(goal),
                target = state.target(goal),
                onSave = { vm.setProgress(goal, it) },
                onDismiss = { goalSheet = null },
            )
        }
    }
    if (showEditProfile) {
        EditProfileSheet(
            name = state.userName,
            onSave = vm::setName,
            onDismiss = { showEditProfile = false },
        )
    }
}

@Composable
private fun BloomBottomBar(selected: HomeTab, onSelect: (HomeTab) -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    NavigationBar(containerColor = MaterialTheme.colorScheme.background, tonalElevation = 0.dp) {
        HomeTab.entries.forEach { t ->
            NavigationBarItem(
                selected = t == selected,
                onClick = { onSelect(t) },
                icon = { Icon(t.icon, contentDescription = t.label) },
                label = { Text(t.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    selectedIconColor = primary, selectedTextColor = primary,
                    unselectedIconColor = muted, unselectedTextColor = muted,
                ),
            )
        }
    }
}