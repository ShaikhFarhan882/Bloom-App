package apps.farhan.newage.bloom.ui.splash

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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

private const val SPLASH_MIN_MS = 1200L
private const val SPLASH_AD_TIMEOUT_MS = 2500L // never block the user longer than this

class SplashViewModel(private val repository: UserPreferencesRepository) : ViewModel() {
    suspend fun isOnboardingCompleted(): Boolean = repository.onboardingCompleted.first()
}

@Composable
fun SplashRoute(onResolved: (onboardingDone: Boolean) -> Unit) {
    val context = LocalContext.current
    val container = context.appContainer()
    val viewModel: SplashViewModel = viewModel(
        factory = viewModelFactory {
            initializer { SplashViewModel(container.userPreferences) }
        },
    )
    val currentOnResolved by rememberUpdatedState(onResolved)

    LaunchedEffect(Unit) {
        delay(SPLASH_MIN_MS)
        val onboardingDone = viewModel.isOnboardingCompleted()
        val activity = context.findActivity()

        // First-time user: no App Open Ad, straight to onboarding
        if (!onboardingDone || activity == null) {
            currentOnResolved(onboardingDone)
            return@LaunchedEffect
        }

        // Returning user: wait briefly for the ad, show it if ready, then continue either way
        val ads = container.appOpenAdManager
        ads.awaitAd(context, SPLASH_AD_TIMEOUT_MS)
        ads.showAdIfAvailable(activity) {
            ads.enableForegroundAds(context)
            currentOnResolved(true)
        }
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

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}