package apps.farhan.newage.bloom.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import apps.farhan.newage.bloom.appContainer
import apps.farhan.newage.bloom.data.UserPreferencesRepository
import apps.farhan.newage.bloom.ui.components.BloomLogo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

class SplashViewModel(private val repository: UserPreferencesRepository) : ViewModel() {
    suspend fun isOnboardingCompleted(): Boolean = repository.onboardingCompleted.first()
}

@Composable
fun SplashRoute(onResolved: (onboardingDone: Boolean) -> Unit) {
    val container = LocalContext.current.appContainer()
    val viewModel: SplashViewModel = viewModel(
        factory = viewModelFactory {
            initializer { SplashViewModel(container.userPreferences) }
        },
    )
    val currentOnResolved by rememberUpdatedState(onResolved)

    LaunchedEffect(Unit) {
        delay(1200) // brief branding moment; never waits on an ad
        currentOnResolved(viewModel.isOnboardingCompleted())
    }

    SplashScreen()
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        BloomLogo()
    }
}