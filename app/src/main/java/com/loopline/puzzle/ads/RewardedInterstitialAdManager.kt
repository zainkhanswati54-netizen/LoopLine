package com.loopline.puzzle.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Rewarded interstitial - a skippable full-screen ad that still pays out a
 * reward if watched through, shown at bigger, less frequent milestones
 * (e.g. a streak milestone or "double reward" prompt) rather than gating a
 * core action the way [AdsManager]'s plain rewarded ad gates Hint.
 *
 * Uses the real AdMob Rewarded Interstitial ad unit
 * ("AdUnit_Rewarded interstitial reward 1").
 */
object RewardedInterstitialAdManager {

    private const val TAG = "RewardedInterstitialAdManager"
    private const val AD_UNIT_ID = "ca-app-pub-9019700052213764/6869416828" // real Rewarded Interstitial ad unit ID

    sealed class Result {
        data object Rewarded : Result()
        data object Failed : Result()
    }

    @Volatile
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null

    @Volatile
    private var isLoading = false

    /** Call at app startup (and again after each ad is consumed) to have one ready to go. */
    fun preload(context: Context) {
        if (rewardedInterstitialAd != null || isLoading) return
        isLoading = true
        RewardedInterstitialAd.load(
            context.applicationContext,
            AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    isLoading = false
                    rewardedInterstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoading = false
                    rewardedInterstitialAd = null
                    Log.w(TAG, "Rewarded interstitial failed to load: ${error.message}")
                }
            }
        )
    }

    fun isReady(): Boolean = rewardedInterstitialAd != null

    /**
     * Shows the rewarded interstitial if one is ready, awaiting the
     * player's watch-through. Unlike a plain rewarded ad, the player can
     * skip it after a short delay - [Result.Failed] covers both "skipped
     * before earning" and "nothing was loaded".
     */
    suspend fun show(activity: Activity): Result {
        val ad = rewardedInterstitialAd
        if (ad == null) {
            preload(activity)
            return Result.Failed
        }
        rewardedInterstitialAd = null // consume it; a new one is loaded below regardless of outcome

        return suspendCancellableCoroutine { continuation ->
            var earnedReward = false

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    preload(activity)
                    if (continuation.isActive) {
                        continuation.resume(if (earnedReward) Result.Rewarded else Result.Failed)
                    }
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    preload(activity)
                    Log.w(TAG, "Rewarded interstitial failed to show: ${error.message}")
                    if (continuation.isActive) {
                        continuation.resume(Result.Failed)
                    }
                }
            }

            ad.show(activity) { earnedReward = true }
        }
    }
}
