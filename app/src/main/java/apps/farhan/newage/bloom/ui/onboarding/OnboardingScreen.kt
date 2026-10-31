package apps.farhan.newage.bloom.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import apps.farhan.newage.bloom.appContainer
import apps.farhan.newage.bloom.ui.components.BloomLogo
import apps.farhan.newage.bloom.ui.components.BloomPrimaryButton
import apps.farhan.newage.bloom.ui.components.OptionRow
import apps.farhan.newage.bloom.ui.components.StepProgressBar
import apps.farhan.newage.bloom.ui.components.ToggleRow
import apps.farhan.newage.bloom.ui.theme.BloomTheme

@Composable
fun OnboardingRoute(onFinished: () -> Unit) {
    val container = LocalContext.current.appContainer()
    val viewModel: OnboardingViewModel = viewModel(
        factory = viewModelFactory {
            initializer { OnboardingViewModel(container.userPreferences) }
        },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnFinished by rememberUpdatedState(onFinished)

    LaunchedEffect(state.finished) {
        if (state.finished) currentOnFinished()
    }
    BackHandler(enabled = state.step > 0) { viewModel.onBack() }

    OnboardingScreen(
        state = state,
        onNameChange = viewModel::onNameChange,
        onToggle = viewModel::onToggle,
        onSlotSelected = viewModel::onSlotSelected,
        onTimeSelected = viewModel::onTimeSelected,
        onNext = viewModel::onNext,
        onSkip = viewModel::onSkip,
    )
}

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onNameChange: (String) -> Unit,
    onToggle: (TrackItem, Boolean) -> Unit,
    onSlotSelected: (ReminderSlot) -> Unit,
    onTimeSelected: (Int, Int) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        StepProgressBar(current = state.step, total = OnboardingUiState.STEP_COUNT)
        Spacer(Modifier.height(24.dp))

        AnimatedContent(
            targetState = state.step,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                val forward = targetState > initialState
                (slideInHorizontally { if (forward) it / 4 else -it / 4 } + fadeIn()) togetherWith
                        (slideOutHorizontally { if (forward) -it / 4 else it / 4 } + fadeOut())
            },
            label = "onboarding_step",
        ) { step ->
            when (step) {
                0 -> WelcomeStep()
                1 -> NameStep(state.name, onNameChange)
                2 -> TrackStep(state.tracked, onToggle)
                else -> ReminderStep(state, onSlotSelected, onTimeSelected)
            }
        }

        state.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(12.dp))

        BloomPrimaryButton(
            text = when {
                state.isLastStep -> "Enter Bloom"
                state.step == 0 -> "Get started"
                else -> "Continue"
            },
            onClick = onNext,
            enabled = state.canContinue,
            loading = state.isSaving,
        )
        if (state.isLastStep) {
            TextButton(
                onClick = onSkip,
                enabled = !state.isSaving,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text("Set up later", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/* ---------- Steps ---------- */

@Composable
private fun WelcomeStep() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BloomLogo()
        Spacer(Modifier.height(28.dp))
        Text(
            "Welcome to Bloom",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        // Placeholder copy: replace with the exact text from Figma
        Text(
            "A calm space to understand your body, track what matters, and feel supported with PCOS.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun NameStep(name: String, onNameChange: (String) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        StepHeader(
            title = "A little about you",
            subtitle = "What should we call you?",
        )
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Your first name") },
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
            ),
        )
    }
}

@Composable
private fun TrackStep(tracked: Set<TrackItem>, onToggle: (TrackItem, Boolean) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        StepHeader(
            icon = Icons.Outlined.Star,
            title = "What would you like to track daily?",
            subtitle = "Choose what matters to you. You can change this anytime in Settings.",
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TrackItem.entries.forEach { item ->
                ToggleRow(
                    title = item.title,
                    subtitle = item.subtitle,
                    checked = item in tracked,
                    onCheckedChange = { onToggle(item, it) },
                )
            }
        }
    }
}

@Composable
private fun ReminderStep(
    state: OnboardingUiState,
    onSlotSelected: (ReminderSlot) -> Unit,
    onTimeSelected: (Int, Int) -> Unit,
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        StepHeader(
            icon = Icons.Outlined.Notifications,
            title = "When should we remind you?",
            subtitle = "Pick a time that fits your routine. One gentle nudge a day.",
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ReminderSlot.entries.forEach { slot ->
                OptionRow(
                    title = slot.title,
                    subtitle = slot.subtitle,
                    selected = state.slot == slot,
                    onClick = { onSlotSelected(slot) },
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { showPicker = true }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Exact time",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            Text(
                formatTime(state.hour, state.minute),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }

    if (showPicker) {
        TimePickerDialog(
            initialHour = state.hour,
            initialMinute = state.minute,
            onConfirm = { h, m ->
                onTimeSelected(h, m)
                showPicker = false
            },
            onDismiss = { showPicker = false },
        )
    }
}

/* ---------- Helpers ---------- */

@Composable
private fun StepHeader(title: String, subtitle: String, icon: ImageVector? = null) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.height(12.dp))
        }
        Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val pickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(pickerState.hour, pickerState.minute) }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = { TimePicker(state = pickerState) },
    )
}

private fun formatTime(hour: Int, minute: Int): String {
    val hour12 = if (hour % 12 == 0) 12 else hour % 12
    val suffix = if (hour < 12) "AM" else "PM"
    return "%02d:%02d %s".format(hour12, minute, suffix)
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun OnboardingTrackPreview() {
    BloomTheme {
        OnboardingScreen(
            state = OnboardingUiState(step = 2),
            onNameChange = {}, onToggle = { _, _ -> }, onSlotSelected = {},
            onTimeSelected = { _, _ -> }, onNext = {}, onSkip = {},
        )
    }
}