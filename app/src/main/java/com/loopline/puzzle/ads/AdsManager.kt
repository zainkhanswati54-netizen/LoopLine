package com.loopline.puzzle.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Wraps Google Mobile Ads' rewarded-ad flow behind the one thing the game
 * actually needs: "show a rewarded ad, then tell me whether the player
 * actually earned the reward." Loading, pre-fetching the *next* ad, and
 * retrying after a failed load all happen internally, so [GameScreen]'s
 * Hint button doesn't need to know anything about the ad SDK itself.
 *
 * *** BEFORE A REAL PLAY STORE RELEASE ***
 * [HINT_REWARDED_AD_UNIT_ID] below, and the `APPLICATION_ID` meta-data in
 * AndroidManifest.xml, are both Google's published *test* IDs. They always
 * serve a real, functional test ad on every device with zero AdMob account
 * setup, so hints are already properly ad-gated and testable right now -
 * but test ads earn no real revenue and Google's policies prohibit
 * shipping them to production. Create an AdMob account + app + rewarded ad
 * unit, then swap both IDs for your real ones before release:
 * https://support.google.com/admob/answer/3052638
 */
object AdsManager {
    private const val TAG = "AdsManager"

    private const val HINT_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    private var initialized = false
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    /** Call once, as early as possible (MainActivity.onCreate) - starts the
     * SDK and kicks off the first preload so a rewarded ad is usually
     * already sitting in memory by the time a player reaches a level and
     * taps Hint. */
    fun initialize(context: Context) {
        if (initialized) return
        initialized = true
        MobileAds.initialize(context.applicationContext) {}
        preload(context)
    }

    /** Fetches the next rewarded ad in the background. Safe to call as
     * often as you like - it's a no-op while one is already loaded or a
     * load is already in flight. */
    fun preload(context: Context) {
        if (rewardedAd != null || isLoading) return
        isLoading = true
        RewardedAd.load(
            context.applicationContext,
            HINT_REWARDED_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    isLoading = false
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoading = false
                    rewardedAd = null
                    Log.w(TAG, "Rewarded ad failed to load: ${error.message}")
                }
            }
        )
    }

    fun isReady(): Boolean = rewardedAd != null

    /**
     * Shows the preloaded rewarded ad if one is ready. [onEarnedReward]
     * fires only if the player actually watched it through to the reward
     * callback (the ad SDK decides that, not this app - closing early
     * doesn't count). [onUnavailable] covers every other case: no ad
     * loaded yet, or it failed/was dismissed before rewarding - the caller
     * should treat that as "no hint this time," never grant one anyway.
     */
    fun showForHint(activity: Activity, onEarnedReward: () -> Unit, onUnavailable: () -> Unit) {
        val ad = rewardedAd
        if (ad == null) {
            preload(activity)
            onUnavailable()
            return
        }

        // Consumed either way - a RewardedAd instance is one-time-use, so
        // the next tap needs a freshly loaded one regardless of outcome.
        rewardedAd = null
        var earned = false

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                preload(activity)
                if (!earned) onUnavailable()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                preload(activity)
                onUnavailable()
            }
        }

        ad.show(activity) {
            earned = true
            onEarnedReward()
        }
    }
}
