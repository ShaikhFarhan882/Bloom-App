package apps.farhan.newage.bloom.ads

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import apps.farhan.newage.bloom.BuildConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/** Adaptive banner sized to the available width. Collapses to nothing if the ad fails to load. */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun AdaptiveBanner(
    modifier: Modifier = Modifier,
    unitId: String = BuildConfig.BANNER_AD_UNIT_ID,
) {
    val context = LocalContext.current
    var failed by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        if (failed) return@BoxWithConstraints

        val widthDp = maxWidth.value.toInt()
        val adSize = remember(widthDp) {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
        }
        val adView = remember(adSize) {
            AdView(context).apply {
                setAdSize(adSize)
                adUnitId = unitId
                adListener = object : AdListener() {
                    override fun onAdLoaded() = AdAnalytics.log(AdEvents.BANNER_LOADED)
                    override fun onAdImpression() = AdAnalytics.log(AdEvents.BANNER_IMPRESSION)
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        failed = true
                        AdAnalytics.log(AdEvents.BANNER_FAILED, "code" to error.code, "message" to error.message)
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        }
        DisposableEffect(adView) { onDispose { adView.destroy() } }

        AndroidView(
            factory = { adView },
            modifier = Modifier.fillMaxWidth().height(adSize.height.dp),
        )
    }
}