package com.loopline.puzzle.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.loopline.puzzle.R
import com.loopline.puzzle.ui.theme.backgroundBrush
import kotlinx.coroutines.delay

/**
 * Mentric Studios' brand bumper, shown once before [SplashScreen] on every
 * cold start. The source video (`Loading_Mentric_Studios.mp4`, a white-
 * background logo reveal) was pre-processed offline: the white background
 * was matted out to a real alpha channel (no white fringe on the gold
 * lettering) and every frame was exported into `res/drawable-nodpi` as
 * `company_loading_001.png` ... `company_loading_030.png`, 20 frames/sec so
 * the reveal keeps its original 1.5s timing.
 *
 * Because the frames are already transparent, this screen just draws them
 * on top of the exact same [backgroundBrush] every other screen in the app
 * uses - so there's no color seam or flash between "Mentric Studios" and
 * the LoopLine splash that follows it, it reads as one continuous intro.
 */
private const val FRAME_COUNT = 30
private const val FRAME_INTERVAL_MS = 50L // 20fps, matches the source export
private const val HOLD_ON_LAST_FRAME_MS = 450L // let the finished logo sit for a beat

// Listed explicitly (not reflected by name) so a typo or a renamed/missing
// drawable fails the build immediately instead of crashing at runtime.
private val frameResIds: List<Int> = listOf(
    R.drawable.company_loading_001, R.drawable.company_loading_002, R.drawable.company_loading_003,
    R.drawable.company_loading_004, R.drawable.company_loading_005, R.drawable.company_loading_006,
    R.drawable.company_loading_007, R.drawable.company_loading_008, R.drawable.company_loading_009,
    R.drawable.company_loading_010, R.drawable.company_loading_011, R.drawable.company_loading_012,
    R.drawable.company_loading_013, R.drawable.company_loading_014, R.drawable.company_loading_015,
    R.drawable.company_loading_016, R.drawable.company_loading_017, R.drawable.company_loading_018,
    R.drawable.company_loading_019, R.drawable.company_loading_020, R.drawable.company_loading_021,
    R.drawable.company_loading_022, R.drawable.company_loading_023, R.drawable.company_loading_024,
    R.drawable.company_loading_025, R.drawable.company_loading_026, R.drawable.company_loading_027,
    R.drawable.company_loading_028, R.drawable.company_loading_029, R.drawable.company_loading_030,
)

@Composable
fun StudioSplashScreen(onFinished: () -> Unit) {
    var frameIndex by remember { mutableIntStateOf(0) }
    val latestOnFinished by rememberUpdatedState(onFinished)

    LaunchedEffect(Unit) {
        for (i in 0 until FRAME_COUNT) {
            frameIndex = i
            delay(FRAME_INTERVAL_MS)
        }
        delay(HOLD_ON_LAST_FRAME_MS)
        latestOnFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush()),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = frameResIds[frameIndex]),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
