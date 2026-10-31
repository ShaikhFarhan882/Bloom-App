package apps.farhan.newage.bloom.ui.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import apps.farhan.newage.bloom.ui.home.HomeScreen
import apps.farhan.newage.bloom.ui.onboarding.OnboardingRoute
import apps.farhan.newage.bloom.ui.splash.SplashRoute

import androidx.compose.ui.platform.LocalContext
import apps.farhan.newage.bloom.appContainer

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
}

@Composable
fun BloomNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashRoute(
                onResolved = { onboardingDone ->
                    // First launch -> Onboarding. Returning user -> Home.
                    // (App Open Ad for returning users plugs in here in the next step.)
                    val target = if (onboardingDone) Routes.HOME else Routes.ONBOARDING
                    navController.navigate(target) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.ONBOARDING) {
            OnboardingRoute(
                onFinished = {
                    // From now on, background -> foreground may show the App Open Ad
                    context.appContainer().appOpenAdManager.enableForegroundAds(context)
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                onSignedOut = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
            )
        }
    }
}

