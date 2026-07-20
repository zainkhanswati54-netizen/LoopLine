package com.loopline.puzzle.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

/**
 * A quick horizontal "shake" for invalid-move feedback - the Compose
 * equivalent of a classic-View shake `Animation`/`ObjectAnimator` on
 * `translationX`, just expressed as a Modifier extension instead.
 *
 * Usage: give the composable a piece of state that increments every time
 * an invalid move happens (e.g. `var shakeTrigger by remember { mutableStateOf(0) }`,
 * `shakeTrigger++` on every wrong move), then apply
 * `Modifier.shake(shakeTrigger)` to the element that should rattle (the
 * puzzle grid's Canvas, a form field, etc.).
 *
 * [trigger] is a key, not a boolean - a plain boolean can't fire twice in
 * a row if the value never actually changes between two invalid moves.
 * An ever-incrementing Int guarantees `LaunchedEffect` restarts (and the
 * shake replays) on every single invalid touch, even back-to-back ones.
 */
fun Modifier.shake(trigger: Int): Modifier = composed {
    val offsetX = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        if (trigger != 0) {
            // A short decaying wobble: alternating left/right kicks that
            // shrink in amplitude, landing back at rest.
            val keyframes = listOf(-16f, 16f, -12f, 12f, -7f, 7f, -3f, 3f, 0f)
            for (target in keyframes) {
                offsetX.animateTo(target, animationSpec = tween(durationMillis = 35))
            }
        }
    }
    this.graphicsLayer { translationX = offsetX.value }
}
