package com.loopline.puzzle.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.loopline.puzzle.ui.theme.Copper
import com.loopline.puzzle.ui.theme.Gold
import com.loopline.puzzle.ui.theme.LoopLineShapes

/**
 * The small flame-and-number chip used on both Home (persisted streak from
 * the last time the app was open) and in-game (live-updating as the
 * player solves). Pops with a bouncy scale every time [streak] changes
 * (a new perfect solve just landed) instead of just relabeling itself -
 * the one-frame "jump" is what makes the number read as a live reward
 * rather than a static stat.
 */
@Composable
fun StreakChip(streak: Int, modifier: Modifier = Modifier) {
    var displayedStreak by remember { mutableStateOf(streak) }
    val bump = remember { Animatable(1f) }
    LaunchedEffect(streak) {
        if (streak != displayedStreak) {
            displayedStreak = streak
            bump.snapTo(0.6f)
            bump.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
            )
        }
    }
    Row(
        modifier = modifier
            .scale(bump.value)
            .clip(LoopLineShapes.chip)
            .background(Brush.linearGradient(listOf(Copper.copy(alpha = 0.22f), Gold.copy(alpha = 0.22f))))
            .border(width = 1.dp, color = Gold.copy(alpha = 0.35f), shape = LoopLineShapes.chip)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "\uD83D\uDD25", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$displayedStreak",
            style = MaterialTheme.typography.titleMedium,
            color = Gold
        )
    }
}
