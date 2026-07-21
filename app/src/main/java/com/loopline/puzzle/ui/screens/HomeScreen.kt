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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.HelpOutline
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.loopline.puzzle.game.FeatureFlags
import com.loopline.puzzle.game.ProgressStore
import com.loopline.puzzle.game.SettingsStore
import com.loopline.puzzle.ui.components.DailyChallengeBanner
import com.loopline.puzzle.ui.components.FeaturedModeBanner
import com.loopline.puzzle.ui.components.GradientText
import com.loopline.puzzle.ui.components.HowToPlayOverlay
import com.loopline.puzzle.ui.components.IconChipButton
import com.loopline.puzzle.ui.components.LoopLineLogo
import com.loopline.puzzle.ui.components.ModeCard
import com.loopline.puzzle.ui.components.ShineText
import com.loopline.puzzle.ui.components.StreakChip
import com.loopline.puzzle.ui.theme.TextSecondary
import com.loopline.puzzle.ui.theme.backgroundBrush
import com.loopline.puzzle.ui.theme.goldBrush

data class GameMode(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentKey: String,
    val badgeText: String,
    // Kept for any future card that's shown but temporarily non-interactive.
    // Nothing currently sets this to false - a feature that isn't ready
    // simply isn't added to moreCards() below, via FeatureFlags, rather
    // than being shown greyed-out.
    val enabled: Boolean = true,
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

// Settings/Statistics are always shown - they're real, live screens,
// sharing this one grid and the same ModeCard styling on purpose, at the
// user's request, instead of gameplay modes and utility screens looking
// like two different kinds of thing. Zen/Timed/Leaderboard are appended
// below only if their FeatureFlags entry is on, so an unfinished or
// intentionally-hidden feature never shows up as a card at all - not even
// a greyed-out "Coming soon" one.
private fun moreCards(): List<GameMode> = buildList {
    if (FeatureFlags.ZEN_MODE_ENABLED) {
        add(GameMode("Zen", "No timer, no pressure", Icons.Filled.Spa, "rosegold", "Open") { it.onPlayZen() })
    }
    if (FeatureFlags.TIMED_MODE_ENABLED) {
        add(GameMode("Timed", "Beat the clock", Icons.Filled.Timer, "copper", "Open") { it.onPlayTimed() })
    }
    add(GameMode("Settings", "Sound, vibration, reset progress", Icons.Filled.Settings, "gold", "Open") { it.onOpenSettings() })
    add(GameMode("Statistics", "Levels, streaks, best times", Icons.Filled.BarChart, "copper", "Open") { it.onOpenStatistics() })
    if (FeatureFlags.LEADERBOARD_ENABLED) {
        add(GameMode("Leaderboard", "Your personal best runs", Icons.Filled.Leaderboard, "rosegold", "Open") { it.onOpenLeaderboard() })
    }
}

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

    // Shown automatically the very first time Home loads for a player
    // (persisted via SettingsStore so it never nags again after that),
    // and reopenable any time via the "?" chip next to the logo.
    val context = LocalContext.current
    var showHowToPlay by remember { mutableStateOf(!SettingsStore.hasSeenTutorial(context)) }

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
                // Visible here (not just mid-game) so a returning player is
                // reminded what they've got riding before they even tap
                // Play - the same "don't lose the streak" pull that gets
                // people to open habit apps daily.
                val streak = remember { ProgressStore.currentStreak(context) }
                if (streak > 0) {
                    StreakChip(streak = streak, modifier = Modifier.padding(end = 8.dp))
                }
                IconChipButton(
                    icon = Icons.Filled.HelpOutline,
                    contentDescription = "How to play",
                    onClick = { showHowToPlay = true }
                )
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
                itemsIndexed(moreCards()) { index, mode ->
                    ModeCard(
                        title = mode.title,
                        description = mode.description,
                        icon = mode.icon,
                        accentKey = mode.accentKey,
                        badgeText = mode.badgeText,
                        badgeHighlighted = true,
                        appearIndex = index,
                        onClick = { mode.onClick(actions) }
                    )
                }
            }
        }

        if (showHowToPlay) {
            HowToPlayOverlay(
                onDismiss = {
                    showHowToPlay = false
                    SettingsStore.setHasSeenTutorial(context, true)
                }
            )
        }
    }
}
