package com.loopline.puzzle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.loopline.puzzle.game.DailyChallengeStore
import com.loopline.puzzle.game.Difficulty
import com.loopline.puzzle.game.ProgressStore
import com.loopline.puzzle.ui.components.IconChipButton
import com.loopline.puzzle.ui.theme.LoopLineShapes
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

@Composable
fun StatisticsScreen(onBack: () -> Unit) {
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
            Text(text = "Statistics", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    modifier = Modifier.weight(1f),
                    value = "${ProgressStore.totalLevelsCompleted(context)}",
                    label = "Levels completed",
                    accentKey = "gold"
                )
                StatTile(
                    modifier = Modifier.weight(1f),
                    value = "${ProgressStore.totalHintsUsed(context)}",
                    label = "Hints used",
                    accentKey = "copper"
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    modifier = Modifier.weight(1f),
                    value = "${DailyChallengeStore.currentStreak(context)}",
                    label = "Current daily streak",
                    accentKey = "rosegold"
                )
                StatTile(
                    modifier = Modifier.weight(1f),
                    value = "${DailyChallengeStore.bestStreak(context)}",
                    label = "Best daily streak",
                    accentKey = "gold"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "BY DIFFICULTY",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )

            Difficulty.entries.forEach { difficulty ->
                DifficultyStatRow(
                    difficulty = difficulty,
                    bestLevel = ProgressStore.bestLevel(context, difficulty),
                    fastestSeconds = ProgressStore.fastestSeconds(context, difficulty),
                    levelsCompleted = ProgressStore.levelsCompletedFor(context, difficulty),
                    hintsUsed = ProgressStore.hintsUsedFor(context, difficulty),
                    averageSeconds = ProgressStore.averageSolveSeconds(context, difficulty)
                )
            }
        }
    }
}

@Composable
private fun StatTile(modifier: Modifier = Modifier, value: String, label: String, accentKey: String) {
    val accentColor = accentColorFor(accentKey)
    Column(
        modifier = modifier
            .clip(LoopLineShapes.card)
            .background(cardSurfaceBrush())
            .metallicBevel(cornerDp = LoopLineShapes.cardCornerDp, highlight = accentColor.copy(alpha = 0.35f))
            .padding(18.dp)
    ) {
        Text(text = value, style = MaterialTheme.typography.headlineMedium, color = accentColor)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

@Composable
private fun DifficultyStatRow(
    difficulty: Difficulty,
    bestLevel: Int,
    fastestSeconds: Int?,
    levelsCompleted: Int,
    hintsUsed: Int,
    averageSeconds: Int?
) {
    val accentKey = accentKeyFor(difficulty)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LoopLineShapes.card)
            .background(cardSurfaceBrush())
            .metallicBevel(cornerDp = LoopLineShapes.cardCornerDp)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(accentBrushFor(accentKey))
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(difficulty.label, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (bestLevel > 0) "Best: Level $bestLevel" else "Not played yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            if (fastestSeconds != null) {
                Text(
                    text = "${fastestSeconds}s best",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Granular breakdown - only shown once there's at least one
        // completed level at this difficulty, so a fresh install doesn't
        // show a row of zeroes for every difficulty the player hasn't
        // touched yet.
        if (levelsCompleted > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MiniStat(label = "Completed", value = "$levelsCompleted")
                MiniStat(label = "Hints used", value = "$hintsUsed")
                MiniStat(label = "Avg time", value = averageSeconds?.let { "${it}s" } ?: "\u2014")
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Column {
        Text(text = value, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}
