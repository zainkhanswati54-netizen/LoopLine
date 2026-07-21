package com.loopline.puzzle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.loopline.puzzle.game.DailyChallengeStore
import com.loopline.puzzle.game.Difficulty
import com.loopline.puzzle.game.ProgressStore
import com.loopline.puzzle.ui.components.GradientText
import com.loopline.puzzle.ui.components.IconChipButton
import com.loopline.puzzle.ui.theme.LoopLineShapes
import com.loopline.puzzle.ui.theme.TextPrimary
import com.loopline.puzzle.ui.theme.TextSecondary
import com.loopline.puzzle.ui.theme.accentBrushFor
import com.loopline.puzzle.ui.theme.accentColorFor
import com.loopline.puzzle.ui.theme.backgroundBrush
import com.loopline.puzzle.ui.theme.cardSurfaceBrush
import com.loopline.puzzle.ui.theme.goldBrush
import com.loopline.puzzle.ui.theme.metallicBevel

private fun accentKeyFor(difficulty: Difficulty): String = when (difficulty) {
    Difficulty.EASY -> "rosegold"
    Difficulty.NORMAL -> "gold"
    Difficulty.HARD -> "copper"
}

/** "45s", "3m 05s", or "1h 12m" depending on magnitude - never all three units at once. */
private fun formatDuration(totalSeconds: Long): String {
    if (totalSeconds <= 0) return "0s"
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${seconds.toString().padStart(2, '0')}s"
        else -> "${seconds}s"
    }
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero tile - the one number a player checks most often gets a
            // bigger, gradient-filled treatment instead of sitting the same
            // size as everything else below it.
            HeroStatTile(
                value = "${ProgressStore.totalLevelsCompleted(context)}",
                label = "Levels completed",
                icon = Icons.Filled.CheckCircle
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.TipsAndUpdates,
                    value = "${ProgressStore.totalHintsUsed(context)}",
                    label = "Hints used",
                    accentKey = "copper"
                )
                StatTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Timer,
                    value = formatDuration(ProgressStore.totalPlayTimeSeconds(context)),
                    label = "Time played",
                    accentKey = "gold"
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.LocalFireDepartment,
                    value = "${DailyChallengeStore.currentStreak(context)}",
                    label = "Current daily streak",
                    accentKey = "rosegold"
                )
                StatTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.EmojiEvents,
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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeroStatTile(value: String, label: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LoopLineShapes.card)
            .background(cardSurfaceBrush())
            .metallicBevel(cornerDp = LoopLineShapes.cardCornerDp, highlight = TextPrimary.copy(alpha = 0.12f))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(goldBrush()),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = TextPrimary)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            GradientText(
                text = value,
                brush = goldBrush(),
                style = MaterialTheme.typography.headlineLarge
            )
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}

@Composable
private fun StatTile(modifier: Modifier = Modifier, icon: ImageVector, value: String, label: String, accentKey: String) {
    val accentColor = accentColorFor(accentKey)
    Column(
        modifier = modifier
            .clip(LoopLineShapes.card)
            .background(cardSurfaceBrush())
            .metallicBevel(cornerDp = LoopLineShapes.cardCornerDp, highlight = accentColor.copy(alpha = 0.35f))
            .padding(18.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(8.dp))
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
                MiniStat(icon = Icons.Filled.CheckCircle, label = "Completed", value = "$levelsCompleted")
                MiniStat(icon = Icons.Filled.TipsAndUpdates, label = "Hints used", value = "$hintsUsed")
                MiniStat(
                    icon = Icons.Filled.Timer,
                    label = "Avg time",
                    value = averageSeconds?.let { "${it}s" } ?: "\u2014"
                )
            }
        }
    }
}

@Composable
private fun MiniStat(icon: ImageVector, label: String, value: String) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}
