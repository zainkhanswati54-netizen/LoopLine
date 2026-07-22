package com.loopline.puzzle.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Full-screen interstitial shown at natural break points (level complete).
 * Uses the real AdMob Interstitial ad unit ("AdUnit_Interstitial").
 *
 * Shows at most once every [LEVELS_BETWEEN_ADS] completed levels so players
 * aren't interrupted after every single solve, and only if an ad already
 * finished loading - so this never blocks or delays the level-advance flow
 * in [com.loopline.puzzle.ui.screens.GameScreen].
 */
object InterstitialAdManager {

    private const val TAG = "InterstitialAdManager"
    private const val AD_UNIT_ID = "ca-app-pub-9019700052213764/1808661831" // real Interstitial ad unit ID (AdUnit_Interstitial)
    private const val LEVELS_BETWEEN_ADS = 3

    @Volatile
    private var interstitialAd: InterstitialAd? = null

    @Volatile
    private var isLoading = false

    private var levelsSinceLastAd = 0

    fun preload(context: Context) {
        if (interstitialAd != null || isLoading) return
        isLoading = true
        InterstitialAd.load(
            context.applicationContext,
            AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    isLoading = false
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoading = false
                    interstitialAd = null
                    Log.w(TAG, "Interstitial failed to load: ${error.message}")
                }
            }
        )
    }

    /**
     * Call this once when a level finishes. Shows an ad only every
     * [LEVELS_BETWEEN_ADS] levels and only if one is already loaded;
     * otherwise it's a no-op (and a fresh load is queued for next time).
     */
    fun maybeShowOnLevelComplete(activity: Activity) {
        levelsSinceLastAd++
        val ad = interstitialAd
        if (levelsSinceLastAd < LEVELS_BETWEEN_ADS || ad == null) {
            preload(activity)
            return
        }
        levelsSinceLastAd = 0
        interstitialAd = null

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                preload(activity)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(TAG, "Interstitial failed to show: ${error.message}")
                preload(activity)
            }
        }
        ad.show(activity)
    }
}
