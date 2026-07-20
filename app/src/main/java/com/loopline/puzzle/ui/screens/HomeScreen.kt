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
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loopline.puzzle.ui.components.DailyChallengeBanner
import com.loopline.puzzle.ui.components.FeaturedModeBanner
import com.loopline.puzzle.ui.components.GradientText
import com.loopline.puzzle.ui.components.LoopLineLogo
import com.loopline.puzzle.ui.components.ModeCard
import com.loopline.puzzle.ui.components.ShineText
import com.loopline.puzzle.ui.theme.TextSecondary
import com.loopline.puzzle.ui.theme.backgroundBrush
import com.loopline.puzzle.ui.theme.goldBrush

data class GameMode(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentKey: String,
    val badgeText: String,
    val onClick: (HomeActions) -> Unit
)

/** Bundles Home's navigation callbacks so the mode list below can be a
 * plain top-level val instead of being rebuilt inline every recomposition. */
class HomeActions(
    val onPlayZen: () -> Unit,
    val onPlayTimed: () -> Unit,
    val onOpenSettings: () -> Unit,
    val onOpenStatistics: () -> Unit,
    val onOpenLeaderboard: () -> Unit
)

private val featuredMode = Triple("Classic", "Connect every tile in one stroke", Icons.Filled.GridOn)

// Everything here is a real, live screen now - Zen and Timed are playable
// modes (see GameScreen's isZen/isTimed branching), and Settings/
// Statistics/Leaderboard are real screens too. They share this one grid
// and the same ModeCard styling on purpose, at the user's request, instead
// of gameplay modes and utility screens looking like two different kinds
// of thing.
private val moreCards = listOf(
    GameMode("Zen", "No timer, no pressure", Icons.Filled.Spa, "rosegold", "Play") { it.onPlayZen() },
    GameMode("Timed", "Beat the clock", Icons.Filled.Timer, "copper", "Play") { it.onPlayTimed() },
    GameMode("Settings", "Sound, vibration, reset progress", Icons.Filled.Settings, "gold", "Open") { it.onOpenSettings() },
    GameMode("Statistics", "Levels, streaks, best times", Icons.Filled.BarChart, "copper", "Open") { it.onOpenStatistics() },
    GameMode("Leaderboard", "Your personal best runs", Icons.Filled.Leaderboard, "rosegold", "Open") { it.onOpenLeaderboard() },
)

@Composable
fun HomeScreen(
    onPlayClassic: () -> Unit,
    onPlayDaily: () -> Unit,
    onPlayZen: () -> Unit,
    onPlayTimed: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStatistics: () -> Unit,
    onOpenLeaderboard: () -> Unit
) {
    val actions = remember(onPlayZen, onPlayTimed, onOpenSettings, onOpenStatistics, onOpenLeaderboard) {
        HomeActions(onPlayZen, onPlayTimed, onOpenSettings, onOpenStatistics, onOpenLeaderboard)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush())
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LoopLineLogo(modifier = Modifier.size(52.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    GradientText(
                        text = "LoopLine",
                        brush = goldBrush(),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "One-stroke puzzles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Daily Challenge sits above Classic on purpose - it's the one
            // thing that's actually live and time-sensitive (a fresh puzzle
            // every 24h, with a streak on the line), so it earns the top
            // slot over the evergreen endless mode beneath it.
            DailyChallengeBanner(
                onClick = onPlayDaily,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            FeaturedModeBanner(
                title = featuredMode.first,
                description = featuredMode.second,
                icon = featuredMode.third,
                accentKey = "gold",
                onClick = onPlayClassic,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            ShineText(
                text = "MORE",
                style = MaterialTheme.typography.headlineMedium.copy(letterSpacing = 1.2.sp),
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(moreCards) { mode ->
                    ModeCard(
                        title = mode.title,
                        description = mode.description,
                        icon = mode.icon,
                        accentKey = mode.accentKey,
                        badgeText = mode.badgeText,
                        badgeHighlighted = true,
                        onClick = { mode.onClick(actions) }
                    )
                }
            }
        }
    }
}
