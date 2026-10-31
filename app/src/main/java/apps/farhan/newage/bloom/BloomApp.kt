package apps.farhan.newage.bloom

import android.app.Application
import android.content.Context
import apps.farhan.newage.bloom.data.UserPreferencesRepository
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BloomApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Initialise the Mobile Ads SDK off the main thread.
        // Next step: create the AppOpenAdManager here and register it with ProcessLifecycleOwner.
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@BloomApp) { }
        }
    }
}

class AppContainer(context: Context) {
    val userPreferences = UserPreferencesRepository(context.applicationContext)
}

fun Context.appContainer(): AppContainer = (applicationContext as BloomApp).container