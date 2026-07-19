package com.loopline.puzzle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.loopline.puzzle.game.Difficulty
import com.loopline.puzzle.game.ProgressStore
import com.loopline.puzzle.ui.components.IconChipButton
import com.loopline.puzzle.ui.theme.Gold
import com.loopline.puzzle.ui.theme.LoopLineShapes
import com.loopline.puzzle.ui.theme.TextOnMetal
import com.loopline.puzzle.ui.theme.TextPrimary
import com.loopline.puzzle.ui.theme.TextSecondary
import com.loopline.puzzle.ui.theme.accentBrushFor
import com.loopline.puzzle.ui.theme.accentColorFor
import com.loopline.puzzle.ui.theme.backgroundBrush
import com.loopline.puzzle.ui.theme.cardSurfaceBrush
import com.loopline.puzzle.ui.theme.metallicBevel

private fun accentKeyFor(difficulty: Difficulty): String = when (difficulty) {
    Difficulty.EASY -> "rosegold"
    Difficulty.NORMAL -> "gold"
    Difficulty.HARD -> "copper"
}

/**
 * There's no server behind this app, so there's no way to honestly show
 * *other* players' scores here - that would mean either standing up real
 * backend infrastructure or faking numbers, and faking them would be
 * actively misleading. This shows the one leaderboard that's actually true
 * today: the player's own best times per difficulty. If online
 * leaderboards get built later, this is the screen to extend.
 */
@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 12.dp, end = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconChipButton(icon = Icons.Filled.ArrowBack, contentDescription = "Back", onClick = onBack)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "Leaderboard", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your best runs \u2014 online leaderboards need a server, so this is just you for now.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Difficulty.entries.forEachIndexed { index, difficulty ->
                BestRunRow(
                    rank = index + 1,
                    difficulty = difficulty,
                    bestLevel = ProgressStore.bestLevel(context, difficulty),
                    fastestSeconds = ProgressStore.fastestSeconds(context, difficulty)
                )
            }
        }
    }
}

@Composable
private fun BestRunRow(rank: Int, difficulty: Difficulty, bestLevel: Int, fastestSeconds: Int?) {
    val accentKey = accentKeyFor(difficulty)
    val accentColor = accentColorFor(accentKey)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LoopLineShapes.card)
            .background(cardSurfaceBrush())
            .metallicBevel(cornerDp = LoopLineShapes.cardCornerDp, highlight = accentColor.copy(alpha = 0.35f))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accentBrushFor(accentKey)),
            contentAlignment = Alignment.Center
        ) {
            if (rank == 1) {
                Icon(imageVector = Icons.Filled.EmojiEvents, contentDescription = null, tint = TextOnMetal)
            } else {
                Text(text = "$rank", style = MaterialTheme.typography.titleMedium, color = TextOnMetal)
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(difficulty.label, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (bestLevel > 0) "Reached Level $bestLevel" else "Not played yet",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        if (fastestSeconds != null) {
            Text(
                text = "${fastestSeconds}s",
                style = MaterialTheme.typography.headlineSmall,
                color = Gold
            )
        }
    }
}
