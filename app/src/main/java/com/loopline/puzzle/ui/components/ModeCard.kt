package com.loopline.puzzle.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.loopline.puzzle.game.UiSoundPlayer
import com.loopline.puzzle.ui.theme.LoopLineShapes
import com.loopline.puzzle.ui.theme.TextOnMetal
import com.loopline.puzzle.ui.theme.TextPrimary
import com.loopline.puzzle.ui.theme.TextSecondary
import com.loopline.puzzle.ui.theme.accentBrushFor
import com.loopline.puzzle.ui.theme.accentColorFor
import com.loopline.puzzle.ui.theme.cardSurfaceBrush
import com.loopline.puzzle.ui.theme.metallicBevel
import kotlinx.coroutines.delay

@Composable
fun ModeCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentKey: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeText: String = "Coming soon",
    badgeHighlighted: Boolean = false,
    // Staggers this card's one-time entrance (fade + rise + scale-in) by
    // `appearIndex * 60ms` - called with the grid's item index so a row of
    // cards cascades in left-to-right/top-to-bottom instead of every card
    // on the screen popping in on the exact same frame. 0 (the default)
    // means "appear immediately", which is what any caller not passing
    // this explicitly gets - existing call sites are unaffected.
    appearIndex: Int = 0,
    // Reserved slot rendered above the badge, e.g. a future Daily Puzzle
    // "resets in HH:MM:SS" countdown - left null today so every existing
    // card's layout is unchanged until that mode is wired up.
    footer: (@Composable () -> Unit)? = null
) {
    val accentColor = accentColorFor(accentKey)
    val accentBrush = accentBrushFor(accentKey)
    val context = LocalContext.current

    // One-shot entrance: starts invisible and slightly below/smaller, then
    // eases up to its resting position. Runs once per composition (i.e.
    // once per time this card enters the grid), not on every recomposition.
    val entrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(appearIndex * 60L)
        entrance.animateTo(1f, animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing))
    }

    // A tactile press-in on top of the ambient breathing pulse below - the
    // two multiply together so a highlighted card can be gently breathing
    // *and* still give a firm compress the instant it's tapped.
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "modeCardPressScale"
    )

    // A slow, subtle breathing pulse - scale and glow tick to the same
    // phase - so the one truly playable card reads as tappable and alive.
    // "Coming soon" tiles deliberately don't get this: they used to breathe
    // with the exact same energy as Classic, which made a placeholder like
    // Zen look like a fully-live mode sitting right next to it. Now only
    // badgeHighlighted cards animate; everything else sits still and reads
    // as secondary at a glance.
    val pulseTransition = rememberInfiniteTransition(label = "modeCardPulse")
    val pulse by pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (badgeHighlighted) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(title.hashCode().mod(900), StartOffsetType.FastForward)
        ),
        label = "pulse"
    )
    val pulseScale = 1f + pulse * 0.015f
    val glowAlpha = (if (badgeHighlighted) 0.30f else 0.10f) + pulse * 0.12f

    Column(
        modifier = modifier
            .graphicsLayer {
                // Entrance: fades/scales/rises in together; once entrance
                // reaches 1 this block is a no-op every frame after.
                alpha = entrance.value
                scaleX = pulseScale * pressScale * (0.9f + entrance.value * 0.1f)
                scaleY = scaleX
                translationY = (1f - entrance.value) * 24f
            }
            .shadow(
                elevation = (if (badgeHighlighted) 16.dp else 5.dp) + (pulse * 4f).dp,
                shape = LoopLineShapes.card,
                ambientColor = accentColor.copy(alpha = glowAlpha),
                spotColor = accentColor.copy(alpha = glowAlpha + 0.08f)
            )
            .clip(LoopLineShapes.card)
            .background(cardSurfaceBrush())
            .metallicBevel(cornerDp = LoopLineShapes.cardCornerDp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    UiSoundPlayer.playTap(context)
                    onClick()
                }
            )
            .padding(18.dp)
            .height(158.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(LoopLineShapes.iconChip)
                .background(
                    if (badgeHighlighted) accentBrush
                    else Brush.linearGradient(
                        listOf(accentColor.copy(alpha = 0.20f), accentColor.copy(alpha = 0.08f))
                    )
                )
                .metallicBevel(
                    cornerDp = LoopLineShapes.iconChipCornerDp,
                    highlight = Color.White.copy(alpha = 0.22f),
                    shadow = Color.Black.copy(alpha = 0.22f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (badgeHighlighted) TextOnMetal else accentColor
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(text = title, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            maxLines = 2
        )

        Spacer(modifier = Modifier.weight(1f))

        if (footer != null) {
            footer()
            Spacer(modifier = Modifier.height(8.dp))
        }

        Box(
            modifier = Modifier
                .clip(LoopLineShapes.chip)
                .background(
                    if (badgeHighlighted) accentBrush
                    else Brush.linearGradient(listOf(Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.06f)))
                )
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelLarge,
                color = if (badgeHighlighted) TextOnMetal else TextSecondary
            )
        }
    }
}
