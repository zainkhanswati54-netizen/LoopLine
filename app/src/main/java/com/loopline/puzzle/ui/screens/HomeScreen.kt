package com.loopline.puzzle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.loopline.puzzle.ui.components.ComingSoonDialog
import com.loopline.puzzle.ui.components.LoopLineLogo
import com.loopline.puzzle.ui.components.ModeCard
import com.loopline.puzzle.ui.theme.AccentBlue
import com.loopline.puzzle.ui.theme.AccentGreen
import com.loopline.puzzle.ui.theme.AccentOrange
import com.loopline.puzzle.ui.theme.BackgroundDark
import com.loopline.puzzle.ui.theme.TextPrimary
import com.loopline.puzzle.ui.theme.TextSecondary

data class GameMode(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accent: Color,
    val available: Boolean = false
)

private val modes = listOf(
    GameMode("Classic", "Connect every tile in one stroke", Icons.Filled.GridOn, AccentBlue, available = true),
    GameMode("Daily Puzzle", "A fresh challenge every day", Icons.Filled.CalendarToday, AccentOrange),
    GameMode("Timed", "Beat the clock", Icons.Filled.Timer, AccentGreen),
    GameMode("Zen", "No timer, no pressure", Icons.Filled.SelfImprovement, AccentBlue),
)

@Composable
fun HomeScreen(onPlayClassic: () -> Unit) {
    var showComingSoon by remember { mutableStateOf(false) }
    var showStatsComingSoon by remember { mutableStateOf(false) }
    var showLeaderboardComingSoon by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LoopLineLogo(modifier = Modifier.size(52.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LoopLine",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "One-stroke puzzles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = { showStatsComingSoon = true }) {
                    Icon(Icons.Filled.BarChart, contentDescription = "Stats", tint = TextSecondary)
                }
                IconButton(onClick = { showLeaderboardComingSoon = true }) {
                    Icon(Icons.Filled.Leaderboard, contentDescription = "Leaderboard", tint = TextSecondary)
                }
                IconButton(onClick = { showComingSoon = true }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "Choose a mode",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(modes) { mode ->
                    ModeCard(
                        title = mode.title,
                        description = mode.description,
                        icon = mode.icon,
                        accent = mode.accent,
                        badgeText = if (mode.available) "Play" else "Coming soon",
                        badgeHighlighted = mode.available,
                        onClick = {
                            if (mode.available) onPlayClassic() else showComingSoon = true
                        }
                    )
                }
            }
        }

        if (showComingSoon) {
            ComingSoonDialog(onDismiss = { showComingSoon = false })
        }
        if (showStatsComingSoon) {
            ComingSoonDialog(
                onDismiss = { showStatsComingSoon = false },
                title = "Stats \u2014 coming soon",
                message = "Levels cleared, best times, and streaks per difficulty are on the way."
            )
        }
        if (showLeaderboardComingSoon) {
            ComingSoonDialog(
                onDismiss = { showLeaderboardComingSoon = false },
                title = "Leaderboard \u2014 coming soon",
                message = "See how your best levels stack up against other players. Coming soon!"
            )
        }
    }
}
