package com.loopline.puzzle.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle

/**
 * Text filled with a Brush instead of a flat color — used for the
 * "LoopLine" wordmark so it reads as gold foil rather than flat gold ink.
 */
@Composable
fun GradientText(
    text: String,
    brush: Brush,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current
) {
    Text(
        text = text,
        style = style.copy(brush = brush),
        modifier = modifier
    )
}
