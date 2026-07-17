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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.loopline.puzzle.game.Difficulty
import com.loopline.puzzle.game.GameSession
import com.loopline.puzzle.game.ProgressStore
import com.loopline.puzzle.ui.theme.AccentBlue
import com.loopline.puzzle.ui.theme.AccentGreen
import com.loopline.puzzle.ui.theme.AccentOrange
import com.loopline.puzzle.ui.theme.BackgroundDark
import com.loopline.puzzle.ui.theme.SurfaceCard
import com.loopline.puzzle.ui.theme.TextPrimary
import com.loopline.puzzle.ui.theme.TextSecondary
import com.loopline.puzzle.ui.theme.accentColorFor

@Composable
fun DifficultySelectScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onDifficultySelected: (Difficulty) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 4.dp, end = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
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
            if (GameSession.hasActiveSession) {
                Text(
                    text = "In progress",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                ContinueCard(
                    difficulty = GameSession.difficulty,
                    levelNumber = GameSession.levelNumber,
                    onClick = onContinue
                )
                Spacer(modifier = Modifier.height(28.dp))
            }

            Text(
                text = if (GameSession.hasActiveSession) "Or start a new difficulty" else "Pick a difficulty",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DifficultyCard(
                    difficulty = Difficulty.EASY,
                    accent = AccentGreen,
                    bestLevel = ProgressStore.bestLevel(context, Difficulty.EASY),
                    onClick = { onDifficultySelected(Difficulty.EASY) }
                )
                DifficultyCard(
                    difficulty = Difficulty.NORMAL,
                    accent = AccentBlue,
                    bestLevel = ProgressStore.bestLevel(context, Difficulty.NORMAL),
                    onClick = { onDifficultySelected(Difficulty.NORMAL) }
                )
                DifficultyCard(
                    difficulty = Difficulty.HARD,
                    accent = AccentOrange,
                    bestLevel = ProgressStore.bestLevel(context, Difficulty.HARD),
                    onClick = { onDifficultySelected(Difficulty.HARD) }
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
}

@Composable
private fun ContinueCard(difficulty: Difficulty, levelNumber: Int, onClick: () -> Unit) {
    val accent = accentColorFor(
        when (difficulty) {
            Difficulty.EASY -> "green"
            Difficulty.NORMAL -> "blue"
            Difficulty.HARD -> "orange"
        }
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(accent.copy(alpha = 0.16f))
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accent),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text("Level $levelNumber \u00b7 ${difficulty.label}", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            Text("Continue where you left off", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}

@Composable
private fun DifficultyCard(difficulty: Difficulty, accent: Color, bestLevel: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceCard)
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(difficulty.label, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (bestLevel > 0) "Best: Level $bestLevel" else difficulty.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary)
    }
}
