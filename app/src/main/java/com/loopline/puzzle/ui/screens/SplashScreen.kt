package com.loopline.puzzle.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loopline.puzzle.ui.components.GradientText
import com.loopline.puzzle.ui.components.LoopLineLogo
import com.loopline.puzzle.ui.theme.Gold
import com.loopline.puzzle.ui.theme.backgroundBrush
import com.loopline.puzzle.ui.theme.goldBrush
import kotlinx.coroutines.delay

private const val SPLASH_DURATION_MS = 1600L

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.7f,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "logoScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 450),
        label = "logoAlpha"
    )
    val wordmarkAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 500, delayMillis = 150),
        label = "wordmarkAlpha"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(SPLASH_DURATION_MS)
        onFinished()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LoopLineLogo(
            modifier = Modifier
                .size(140.dp)
                .scale(scale)
                .alpha(alpha)
        )

        Spacer(modifier = Modifier.height(28.dp))

        GradientText(
            text = "LOOPLINE",
            brush = goldBrush(),
            style = MaterialTheme.typography.headlineMedium.copy(letterSpacing = 3.sp),
            modifier = Modifier.alpha(wordmarkAlpha)
        )

        Spacer(modifier = Modifier.height(56.dp))

        LoadingDots()
    }
}

@Composable
private fun LoadingDots() {
    val transition = rememberInfiniteTransition(label = "loadingDots")
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(3) { index ->
            val dotScale by transition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 500, delayMillis = index * 150),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot$index"
            )
            val dotAlpha by transition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 500, delayMillis = index * 150),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dotAlpha$index"
            )
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .scale(dotScale)
                    .alpha(dotAlpha)
                    .clip(CircleShape)
                    .background(Gold)
            )
        }
    }
}
