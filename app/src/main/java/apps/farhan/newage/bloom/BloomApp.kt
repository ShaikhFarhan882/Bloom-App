package apps.farhan.newage.bloom

import android.app.Application
import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import apps.farhan.newage.bloom.ads.AppOpenAdManager
import apps.farhan.newage.bloom.data.UserPreferencesRepository
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BloomApp : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        val ads = container.appOpenAdManager

        registerActivityLifecycleCallbacks(ads)
        ProcessLifecycleOwner.get().lifecycle.addObserver(ads)

        appScope.launch(Dispatchers.IO) {
            MobileAds.initialize(this@BloomApp) {
                // Preload only for returning users, so first-time onboarding is never interrupted
                appScope.launch {
                    if (container.userPreferences.onboardingCompleted.first()) {
                        ads.loadAd(this@BloomApp)
                    }
                }
            }
        }
    }
}

class AppContainer(context: Context) {
    val userPreferences = UserPreferencesRepository(context.applicationContext)
    val appOpenAdManager = AppOpenAdManager(BuildConfig.APP_OPEN_AD_UNIT_ID)
}

fun Context.appContainer(): AppContainer {
    val app = applicationContext as? BloomApp
        ?: error("Manifest <application> must declare android:name=\".BloomApp\"")
    return app.container
}