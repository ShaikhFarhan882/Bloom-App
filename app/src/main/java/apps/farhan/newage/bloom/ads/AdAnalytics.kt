package apps.farhan.newage.bloom.ads

import android.util.Log

object AdEvents {
    const val APP_OPEN_REQUESTED = "app_open_ad_requested"
    const val APP_OPEN_LOADED = "app_open_ad_loaded"
    const val APP_OPEN_FAILED = "app_open_ad_failed"
    const val APP_OPEN_SHOWN = "app_open_ad_shown"
    const val APP_OPEN_DISMISSED = "app_open_ad_dismissed"
    const val BANNER_LOADED = "banner_ad_loaded"
    const val BANNER_FAILED = "banner_ad_failed"
    const val BANNER_IMPRESSION = "banner_ad_impression"
}

/* (filter by tag "BloomAds"). */
object AdAnalytics {
    private const val TAG = "BloomAds"

    fun log(event: String, vararg params: Pair<String, Any?>) {
        val extras = params.joinToString(prefix = " ", separator = ", ") { "${it.first}=${it.second}" }
        Log.d(TAG, if (params.isEmpty()) event else event + extras)
    }
}