package apps.farhan.newage.bloom.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

class AppOpenAdManager(private val adUnitId: String) :
    DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

    private enum class LoadState { IDLE, LOADING, LOADED, FAILED }

    private val state = MutableStateFlow(LoadState.IDLE)
    private var appOpenAd: AppOpenAd? = null
    private var loadedAtMs = 0L
    private var isShowingAd = false
    private var currentActivity: Activity? = null

    // False until we know the user is eligible (returning user, or onboarding finished).
    @Volatile
    private var foregroundAdsEnabled = false

    private fun isAdAvailable(): Boolean =
        appOpenAd != null && (SystemClock.elapsedRealtime() - loadedAtMs) < AD_EXPIRY_MS

    /** Preload. Safe to call repeatedly: ignored while loading or when a fresh ad is cached. */
    fun loadAd(context: Context) {
        if (state.value == LoadState.LOADING || isAdAvailable()) return
        state.value = LoadState.LOADING
        AdAnalytics.log(AdEvents.APP_OPEN_REQUESTED)

        AppOpenAd.load(
            context.applicationContext,
            adUnitId,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    loadedAtMs = SystemClock.elapsedRealtime()
                    state.value = LoadState.LOADED
                    AdAnalytics.log(AdEvents.APP_OPEN_LOADED)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenAd = null
                    state.value = LoadState.FAILED // covers no internet too
                    AdAnalytics.log(AdEvents.APP_OPEN_FAILED, "code" to error.code, "message" to error.message)
                }
            },
        )
    }

    /**
     * Splash helper: waits for an ad to load, but never longer than [timeoutMs].
     * Returns normally on success, failure or timeout, so the caller always continues.
     */
    suspend fun awaitAd(context: Context, timeoutMs: Long) {
        if (!isAdAvailable()) loadAd(context)
        withTimeoutOrNull(timeoutMs) {
            state.first { (it == LoadState.LOADED && isAdAvailable()) || it == LoadState.FAILED }
        }
    }

    /** Shows the ad if one is ready, then calls [onFinished] in every case (shown, skipped, failed). */
    fun showAdIfAvailable(activity: Activity, onFinished: () -> Unit = {}) {
        if (isShowingAd) {
            onFinished()
            return
        }
        val ad = appOpenAd
        val appInForeground = ProcessLifecycleOwner.get().lifecycle.currentState
            .isAtLeast(Lifecycle.State.STARTED)

        if (ad == null || !isAdAvailable() || !appInForeground) {
            onFinished()
            loadAd(activity)
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                AdAnalytics.log(AdEvents.APP_OPEN_SHOWN)
            }

            override fun onAdDismissedFullScreenContent() {
                resetAfterShow()
                AdAnalytics.log(AdEvents.APP_OPEN_DISMISSED)
                onFinished()
                loadAd(activity) // reload the next ad
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                resetAfterShow()
                AdAnalytics.log(AdEvents.APP_OPEN_FAILED, "stage" to "show", "code" to error.code)
                onFinished()
                loadAd(activity)
            }
        }
        isShowingAd = true
        ad.show(activity)
    }

    private fun resetAfterShow() {
        appOpenAd = null
        isShowingAd = false
        state.value = LoadState.IDLE
    }

    /** Call once the user is eligible: returning user after the splash, or right after onboarding. */
    fun enableForegroundAds(context: Context) {
        foregroundAdsEnabled = true
        loadAd(context)
    }

    // Background -> Foreground (process level, so rotations/in-app navigation don't trigger it)
    override fun onStart(owner: LifecycleOwner) {
        if (!foregroundAdsEnabled) return
        currentActivity?.let { showAdIfAvailable(it) }
    }

    // Track the visible activity, but not the ad's own activity
    override fun onActivityStarted(activity: Activity) {
        if (!isShowingAd) currentActivity = activity
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity === activity) currentActivity = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    private companion object {
        const val AD_EXPIRY_MS = 4 * 60 * 60 * 1000L // App Open ads expire after 4 hours
    }

    fun disableForegroundAds() { foregroundAdsEnabled = false }
}