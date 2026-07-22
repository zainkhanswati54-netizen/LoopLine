package com.loopline.puzzle.ui.screens

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import android.widget.VideoView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.viewinterop.AndroidView
import com.loopline.puzzle.R
import com.loopline.puzzle.ui.theme.backgroundBrush
import kotlinx.coroutines.delay

/**
 * Mentric Studios' brand bumper, shown once before [SplashScreen] on every
 * cold start. `res/raw/studio_intro.mp4` is the source
 * `Loading_Mentric_Studios.mp4` clip re-rendered offline: its original
 * white background was matted out and replaced with LoopLine's own
 * [backgroundBrush] gradient (the same gold-on-near-black canvas the rest
 * of the app uses), instead of the plain white it shipped with - so
 * nothing needs to be composited at runtime, the clip already *is* the
 * brand moment. The rest of this screen's own backdrop is that same
 * gradient, so there's no color seam between the video and the loading
 * screen that follows it - one continuous intro rather than two visually
 * unrelated screens stitched together.
 *
 * The clip itself is ~1.5s, but a hard [MAX_WAIT_MS] safety timeout
 * guarantees we always move on even if playback stalls or fails on a given
 * device/codec - a studio bumper should never be able to soft-lock the app.
 */
private const val MAX_WAIT_MS = 4000L
private const val FADE_OUT_MS = 260
private const val FADE_IN_MS = 420

/**
 * Plain [VideoView] measures itself at the clip's *natural* pixel size and
 * centers that inside its parent - on any screen whose aspect ratio doesn't
 * exactly match the clip, that leaves plain backdrop-colored letterbox bars
 * above/below (or left/right) of the video instead of the video covering
 * the whole screen. This subclass forces a center-crop (like ImageView's
 * `centerCrop` scaleType): it measures itself *larger* than the available
 * space, scaled up just enough that one axis matches the clip's aspect
 * ratio while covering the whole screen, and relies on the parent clipping
 * the overflow (see [clipToBounds] on the AndroidView below) - so the video
 * always fully fills the screen instead of showing bars around it.
 */
private class CenterCropVideoView(context: Context) : VideoView(context) {
    private var srcWidth = 0
    private var srcHeight = 0

    fun setSourceSize(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        srcWidth = width
        srcHeight = height
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val parentWidth = ViewGroup.getChildMeasureSpec(widthMeasureSpec, 0, ViewGroup.LayoutParams.MATCH_PARENT)
        val parentHeight = ViewGroup.getChildMeasureSpec(heightMeasureSpec, 0, ViewGroup.LayoutParams.MATCH_PARENT)
        if (srcWidth <= 0 || srcHeight <= 0) {
            super.onMeasure(parentWidth, parentHeight)
            return
        }
        val availableWidth = android.view.View.MeasureSpec.getSize(parentWidth)
        val availableHeight = android.view.View.MeasureSpec.getSize(parentHeight)
        val availableRatio = availableWidth.toFloat() / availableHeight
        val sourceRatio = srcWidth.toFloat() / srcHeight

        val (measuredWidth, measuredHeight) = if (sourceRatio > availableRatio) {
            // Clip is relatively wider than the screen: match height, let width overflow.
            (availableHeight * sourceRatio).toInt() to availableHeight
        } else {
            // Clip is relatively taller than the screen: match width, let height overflow.
            availableWidth to (availableWidth / sourceRatio).toInt()
        }
        setMeasuredDimension(measuredWidth, measuredHeight)
    }
}

@Composable
fun StudioSplashScreen(onFinished: () -> Unit) {
    val latestOnFinished = rememberUpdatedState(onFinished)
    var visible by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    // Fade IN on entry (instead of an instant hard cut to the first video
    // frame), then fade out before handing off at the end - reads as one
    // continuous, deliberately animated brand moment bookended on both
    // sides instead of a video that just abruptly appears and swaps away.
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(if (visible) FADE_IN_MS else FADE_OUT_MS),
        label = "studioSplashFade"
    )

    LaunchedEffect(Unit) {
        visible = true
    }

    fun finishOnce() {
        if (!finished) {
            finished = true
            visible = false
        }
    }

    // Once the fade-out finishes, actually navigate. Keyed on `finished` (real
    // Compose state) so this reliably re-runs the moment finishOnce() is called.
    LaunchedEffect(finished) {
        if (finished) {
            delay(FADE_OUT_MS.toLong())
            latestOnFinished.value()
        }
    }

    // Safety net: never let a stalled/broken video keep the player stuck here.
    LaunchedEffect(Unit) {
        delay(MAX_WAIT_MS)
        finishOnce()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush())
            .alpha(alpha)
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds(),
            factory = { ctx ->
                CenterCropVideoView(ctx).apply {
                    val uri = Uri.parse("android.resource://${ctx.packageName}/${R.raw.studio_intro}")
                    setVideoURI(uri)
                    setOnCompletionListener { finishOnce() }
                    setOnErrorListener { _, _, _ -> finishOnce(); true }
                    setOnPreparedListener { player ->
                        player.isLooping = false
                        setSourceSize(player.videoWidth, player.videoHeight)
                    }
                    start()
                }
            }
        )
    }
}
