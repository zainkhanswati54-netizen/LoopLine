package com.loopline.puzzle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.loopline.puzzle.game.Difficulty
import com.loopline.puzzle.game.GameSession
import com.loopline.puzzle.game.ProgressStore
import com.loopline.puzzle.ui.components.IconChipButton
import com.loopline.puzzle.ui.components.MetallicButton
import com.loopline.puzzle.ui.theme.LoopLineShapes
import com.loopline.puzzle.ui.theme.SurfaceCardElevated
import com.loopline.puzzle.ui.theme.TextPrimary
import com.loopline.puzzle.ui.theme.TextSecondary
import com.loopline.puzzle.ui.theme.accentBrushFor
import com.loopline.puzzle.ui.theme.accentColorFor
import com.loopline.puzzle.ui.theme.backgroundBrush
import com.loopline.puzzle.ui.theme.cardSurfaceBrush
import com.loopline.puzzle.ui.theme.metallicBevel

/**
 * Difficulty -> accent metal. Easy gets the softest tone (rose gold),
 * Normal gets the core brand metal (gold), Hard gets the deepest and
 * boldest (copper) — the palette itself signals the step up in intensity.
 */
private fun accentKeyFor(difficulty: Difficulty): String = when (difficulty) {
    Difficulty.EASY -> "rosegold"
    Difficulty.NORMAL -> "gold"
    Difficulty.HARD -> "copper"
}

@Composable
fun DifficultySelectScreen(
    onBack: () -> Unit,
    onDifficultySelected: (Difficulty) -> Unit,
    onRestartDifficulty: (Difficulty) -> Unit
) {
    val context = LocalContext.current

    // Which difficulty (if any) the player has tapped "restart" on - shown
    // as a confirmation dialog before we actually throw away its progress.
    var pendingRestart by remember { mutableStateOf<Difficulty?>(null) }

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
            Text(
                text = "Classic",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "PICK A DIFFICULTY",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Each difficulty tracks its own progress independently, so
            // Easy/Normal/Hard can each have their own in-progress session
            // at the same time without stepping on one another - switching
            // between them and back no longer resets anything.
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DifficultyCard(
                    difficulty = Difficulty.EASY,
                    bestLevel = ProgressStore.bestLevel(context, Difficulty.EASY),
                    inProgressLevel = GameSession.levelNumberFor(Difficulty.EASY)
                        .takeIf { GameSession.hasSession(Difficulty.EASY) },
                    onClick = { onDifficultySelected(Difficulty.EASY) },
                    onRestartClick = { pendingRestart = Difficulty.EASY }
                )
                DifficultyCard(
                    difficulty = Difficulty.NORMAL,
                    bestLevel = ProgressStore.bestLevel(context, Difficulty.NORMAL),
                    inProgressLevel = GameSession.levelNumberFor(Difficulty.NORMAL)
                        .takeIf { GameSession.hasSession(Difficulty.NORMAL) },
                    onClick = { onDifficultySelected(Difficulty.NORMAL) },
                    onRestartClick = { pendingRestart = Difficulty.NORMAL }
                )
                DifficultyCard(
                    difficulty = Difficulty.HARD,
                    bestLevel = ProgressStore.bestLevel(context, Difficulty.HARD),
                    inProgressLevel = GameSession.levelNumberFor(Difficulty.HARD)
                        .takeIf { GameSession.hasSession(Difficulty.HARD) },
                    onClick = { onDifficultySelected(Difficulty.HARD) },
                    onRestartClick = { pendingRestart = Difficulty.HARD }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Every puzzle is generated fresh, and gets a little bigger the further you go.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }

    pendingRestart?.let { difficulty ->
        RestartConfirmDialog(
            difficulty = difficulty,
            onConfirm = {
                onRestartDifficulty(difficulty)
                pendingRestart = null
            },
            onDismiss = { pendingRestart = null }
        )
    }
}

@Composable
private fun DifficultyCard(
    difficulty: Difficulty,
    bestLevel: Int,
    inProgressLevel: Int?,
    onClick: () -> Unit,
    onRestartClick: () -> Unit
) {
    val accentKey = accentKeyFor(difficulty)
    val accentColor = accentColorFor(accentKey)
    val inProgress = inProgressLevel != null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LoopLineShapes.card)
            .background(
                if (inProgress) {
                    Brush.verticalGradient(
                        listOf(accentColor.copy(alpha = 0.16f), accentColor.copy(alpha = 0.06f))
                    )
                } else {
                    cardSurfaceBrush()
                }
            )
            .metallicBevel(
                cornerDp = LoopLineShapes.cardCornerDp,
                highlight = if (inProgress) accentColor.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.35f)
            )
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(accentBrushFor(accentKey))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(difficulty.label, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            val subtitle = when {
                inProgressLevel != null && bestLevel > 0 ->
                    "Continue \u00b7 Level $inProgressLevel  \u2022  Best: Level $bestLevel"
                inProgressLevel != null -> "Continue \u00b7 Level $inProgressLevel"
                bestLevel > 0 -> "Best: Level $bestLevel"
                else -> difficulty.description
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        if (inProgressLevel != null) {
            IconButton(onClick = onRestartClick) {
                Icon(
                    Icons.Filled.RestartAlt,
                    contentDescription = "Start ${difficulty.label} over",
                    tint = TextSecondary
                )
            }
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary)
    }
}

@Composable
private fun RestartConfirmDialog(
    difficulty: Difficulty,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCardElevated,
        shape = LoopLineShapes.dialog,
        modifier = Modifier.metallicBevel(cornerDp = LoopLineShapes.dialogCornerDp),
        title = { Text("Start ${difficulty.label} over?", style = MaterialTheme.typography.headlineMedium, color = TextPrimary) },
        text = {
            Text(
                "This clears your current ${difficulty.label} progress and starts again from " +
                    "Level 1. Your saved best level stays as it is.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        },
        confirmButton = {
            MetallicButton(text = "Restart", onClick = onConfirm, accentKey = accentKeyFor(difficulty))
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
