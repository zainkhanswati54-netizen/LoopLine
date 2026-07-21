package com.loopline.puzzle.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.loopline.puzzle.game.UiSoundPlayer
import com.loopline.puzzle.ui.theme.TextPrimary
import com.loopline.puzzle.ui.theme.TextSecondary

/**
 * A quiet circular chip around an icon — used for the back arrow and
 * header utility icons so tap targets read as one deliberate family
 * instead of bare Material icons floating on the background.
 *
 * Plays the shared [UiSoundPlayer] tap by default; pass [playTapSound] =
 * false for a chip that plays its own distinct cue elsewhere instead.
 */
@Composable
fun IconChipButton(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tint: Color = TextSecondary,
    prominent: Boolean = false,
    enabled: Boolean = true,
    playTapSound: Boolean = true,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "iconChipPressScale"
    )
    IconButton(
        onClick = {
            if (playTapSound) UiSoundPlayer.playTap(context)
            onClick()
        },
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .scale(pressScale)
            .size(40.dp)
            .clip(CircleShape)
            .background(TextPrimary.copy(alpha = if (prominent) 0.08f else 0.05f))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}
