package com.loopline.puzzle.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.loopline.puzzle.game.UiSoundPlayer
import com.loopline.puzzle.ui.theme.LoopLineShapes
import com.loopline.puzzle.ui.theme.TextOnMetal
import com.loopline.puzzle.ui.theme.TextTertiary
import com.loopline.puzzle.ui.theme.accentBrushFor
import com.loopline.puzzle.ui.theme.accentColorFor
import com.loopline.puzzle.ui.theme.metallicBevel

/**
 * The app's one primary-button treatment: a brushed-metal gradient fill,
 * softly rounded corners, and a shadow tinted with the same accent so the
 * button looks lit by its own metal rather than a generic drop shadow.
 *
 * Every tap plays the shared [UiSoundPlayer] click by default - set
 * [playTapSound] to false for the rare button that plays its own distinct
 * cue instead (e.g. GameScreen's Restart uses a paper-rip sound, not the
 * generic tap).
 */
@Composable
fun MetallicButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentKey: String = "gold",
    enabled: Boolean = true,
    playTapSound: Boolean = true,
    // Default (TextOnMetal, a dark brown) reads fine against the paler
    // highlight end of the gold/copper gradients, but on the darker
    // Deep/Shadow stops - and especially at the smaller Hint/Restart size -
    // it gets close to unreadable. Callers on darker or smaller buttons can
    // pass an explicit lighter color instead of changing the shared default
    // for every other metallic button in the app.
    textColor: Color = TextOnMetal
) {
    val accentColor = accentColorFor(accentKey)
    val context = LocalContext.current
    val fillBrush: Brush = if (enabled) {
        accentBrushFor(accentKey)
    } else {
        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.06f)))
    }

    // A tactile "give" on press - the button compresses toward 0.94x with a
    // snappy spring the instant a finger lands and springs back the instant
    // it lifts, instead of the ripple alone standing in for tactile
    // feedback. Cheap (one interaction source, one animated float) but it's
    // the single micro-interaction that makes every tap in the app feel
    // physically pressed rather than just clicked.
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "buttonPressScale"
    )

    Box(
        modifier = modifier
            .scale(pressScale)
            .shadow(
                elevation = if (enabled) (10.dp * pressScale) else 0.dp,
                shape = LoopLineShapes.button,
                ambientColor = accentColor.copy(alpha = 0.35f),
                spotColor = accentColor.copy(alpha = 0.45f)
            )
            .clip(LoopLineShapes.button)
            .background(fillBrush)
            .metallicBevel(cornerDp = LoopLineShapes.buttonCornerDp)
            .let {
                if (enabled) {
                    it.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            if (playTapSound) UiSoundPlayer.playTap(context)
                            onClick()
                        }
                    )
                } else {
                    it
                }
            }
            .padding(horizontal = 24.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = if (enabled) textColor else TextTertiary
        )
    }
}
