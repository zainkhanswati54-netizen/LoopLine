package com.loopline.puzzle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.loopline.puzzle.game.DailyChallengeStore
import com.loopline.puzzle.ui.theme.Gold
import com.loopline.puzzle.ui.theme.LoopLineShapes
import com.loopline.puzzle.ui.theme.TextOnMetal
import com.loopline.puzzle.ui.theme.TextPrimary
import com.loopline.puzzle.ui.theme.TextSecondary
import com.loopline.puzzle.ui.theme.TextTertiary
import com.loopline.puzzle.ui.theme.accentBrushFor
import com.loopline.puzzle.ui.theme.accentColorFor
import com.loopline.puzzle.ui.theme.cardSurfaceBrush
import com.loopline.puzzle.ui.theme.metallicBevel
import kotlinx.coroutines.delay

private fun formatCountdown(totalSeconds: Long): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return "%02d:%02d:%02d".format(h, m, s)
}

@Composable
fun DailyChallengeBanner(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val accentColor = accentColorFor("gold")
    val accentBrush = accentBrushFor("gold")

    var secondsLeft by remember { mutableStateOf(DailyChallengeStore.secondsUntilReset()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            secondsLeft = DailyChallengeStore.secondsUntilReset()
        }
    }

    // Re-read on every composition of this banner (Home re-composes when
    // navigating back to it after playing) so "Done for today" / the
    // streak flip the moment a solve is recorded, without needing a
    // dedicated observable store.
    val completedToday = DailyChallengeStore.isCompletedToday(context)
    val bestTime = DailyChallengeStore.bestTimeSecondsToday(context)
    val streak = DailyChallengeStore.currentStreak(context)
    val last7 = remember(completedToday, streak) { DailyChallengeStore.last7Days(context) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 14.dp,
                shape = LoopLineShapes.card,
                ambientColor = accentColor.copy(alpha = 0.30f),
                spotColor = accentColor.copy(alpha = 0.38f)
            )
            .clip(LoopLineShapes.card)
            .background(cardSurfaceBrush())
            .metallicBevel(cornerDp = LoopLineShapes.cardCornerDp, highlight = accentColor.copy(alpha = 0.45f))
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(LoopLineShapes.iconChip)
                    .background(accentBrush)
                    .metallicBevel(
                        cornerDp = LoopLineShapes.iconChipCornerDp,
                        highlight = Color.White.copy(alpha = 0.28f),
                        shadow = Color.Black.copy(alpha = 0.22f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Filled.CalendarMonth, contentDescription = "Daily Challenge", tint = TextOnMetal)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Daily Challenge", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    if (streak > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = Gold,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "$streak",
                            style = MaterialTheme.typography.labelLarge,
                            color = Gold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (completedToday) {
                        "Done for today \u00b7 ${bestTime ?: 0}s \u00b7 resets in ${formatCountdown(secondsLeft)}"
                    } else {
                        "Live now \u00b7 resets in ${formatCountdown(secondsLeft)}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            MetallicButton(
                text = if (completedToday) "Replay" else "Play",
                onClick = onClick,
                accentKey = "gold"
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            last7.forEach { (label, done) ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (done) accentBrush else Color.White.copy(alpha = 0.06f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (done) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = TextOnMetal,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                }
            }
        }
    }
}
