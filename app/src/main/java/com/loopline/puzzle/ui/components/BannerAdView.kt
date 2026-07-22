package com.loopline.puzzle.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * Standard banner ad, drop this into any screen (e.g. pinned at the bottom
 * of Home). Uses the real AdMob Banner ad unit ("AdUnit_Bannner").
 *
 * IMPORTANT: this is given an explicit fixed size (320x50dp, AdSize.BANNER's
 * own dimensions) instead of relying on the wrapped AdView's intrinsic
 * measurement. Without that, Compose's AndroidView can permanently reserve
 * zero height for the native view before the ad finishes loading, so the
 * banner never becomes visible even after a successful load - looks exactly
 * like "no ad ever shows".
 */
private const val TAG = "BannerAdView"
private const val BANNER_AD_UNIT_ID = "ca-app-pub-9019700052213764/4769867092" // real Banner ad unit ID

@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        AndroidView(
            modifier = Modifier.width(320.dp).height(50.dp),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = BANNER_AD_UNIT_ID
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            Log.d(TAG, "Banner ad loaded")
                        }
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            // Common while the app is still pending AdMob review -
                            // fill is intentionally limited/zero until approved.
                            Log.w(TAG, "Banner failed to load: ${error.message}")
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            },
            // Without this, navigating away from a screen that shows a banner
            // leaves the AdView running in the background forever - still
            // holding native ad resources and still able to fire ad-refresh
            // network calls for a screen the player can no longer see.
            onRelease = { it.destroy() }
        )
    }
}
