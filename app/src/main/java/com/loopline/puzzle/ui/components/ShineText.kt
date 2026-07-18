package com.loopline.puzzle.ui.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import com.loopline.puzzle.ui.theme.Gold
import com.loopline.puzzle.ui.theme.GoldHighlight

/**
 * Gold foil text with a periodic shine: a bright band sweeps once across
 * the label, then sits idle for a few seconds before sweeping again -
 * unlike [GradientText], which is a static metal ramp, this one moves.
 *
 * The moving band is a translated 5-stop linear gradient (base -> highlight
 * -> white -> highlight -> base) with TileMode.Clamp, so outside the band
 * the text simply reads as flat [baseColor] and the loop point is
 * invisible - no shader tricks, just an animated Brush recomputed as the
 * sweep progresses.
 */
@Composable
fun ShineText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    baseColor: Color = Gold,
    shineColor: Color = Color.White
) {
    var widthPx by remember { mutableStateOf(0f) }

    val transition = rememberInfiniteTransition(label = "textShine")
    val sweep by transition.animateFloat(
        initialValue = -0.35f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3200
                -0.35f at 0
                1.35f at 1000
                1.35f at 3200 // hold off-screen - the idle stretch between sweeps
            }
        ),
        label = "sweep"
    )

    val brush = if (widthPx <= 0f) {
        Brush.linearGradient(colors = listOf(baseColor, baseColor))
    } else {
        val bandHalf = widthPx * 0.35f
        val center = sweep * widthPx
        Brush.linearGradient(
            colors = listOf(baseColor, GoldHighlight, shineColor, GoldHighlight, baseColor),
            start = Offset(center - bandHalf, 0f),
            end = Offset(center + bandHalf, 0f),
            tileMode = TileMode.Clamp
        )
    }

    Text(
        text = text,
        style = style.copy(brush = brush),
        modifier = modifier.onGloballyPositioned { widthPx = it.size.width.toFloat() }
    )
}
