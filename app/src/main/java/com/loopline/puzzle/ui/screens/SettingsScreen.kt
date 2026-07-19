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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.loopline.puzzle.game.SettingsStore
import com.loopline.puzzle.ui.components.IconChipButton
import com.loopline.puzzle.ui.components.MetallicButton
import com.loopline.puzzle.ui.theme.Copper
import com.loopline.puzzle.ui.theme.Gold
import com.loopline.puzzle.ui.theme.LoopLineShapes
import com.loopline.puzzle.ui.theme.SurfaceCardElevated
import com.loopline.puzzle.ui.theme.TextPrimary
import com.loopline.puzzle.ui.theme.TextSecondary
import com.loopline.puzzle.ui.theme.backgroundBrush
import com.loopline.puzzle.ui.theme.cardSurfaceBrush
import com.loopline.puzzle.ui.theme.metallicBevel

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var soundEnabled by remember { mutableStateOf(SettingsStore.soundEnabled(context)) }
    var vibrationEnabled by remember { mutableStateOf(SettingsStore.vibrationEnabled(context)) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var showResetDone by remember { mutableStateOf(false) }

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
            Text(text = "Settings", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingToggleRow(
                icon = Icons.Filled.VolumeUp,
                accentColor = Gold,
                title = "Sound effects",
                subtitle = "The connect pop when your stroke moves",
                checked = soundEnabled,
                onCheckedChange = {
                    soundEnabled = it
                    SettingsStore.setSoundEnabled(context, it)
                }
            )
            SettingToggleRow(
                icon = Icons.Filled.NotificationsActive,
                accentColor = Copper,
                title = "Vibration",
                subtitle = "A short haptic tick on every tile you connect",
                checked = vibrationEnabled,
                onCheckedChange = {
                    vibrationEnabled = it
                    SettingsStore.setVibrationEnabled(context, it)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "DANGER ZONE",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(LoopLineShapes.card)
                    .background(cardSurfaceBrush())
                    .metallicBevel(cornerDp = LoopLineShapes.cardCornerDp)
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Reset all progress", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Clears every difficulty's best level and in-progress session, plus your Daily Challenge streak. Can't be undone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            MetallicButton(
                text = "Reset everything",
                onClick = { showResetConfirm = true },
                accentKey = "copper"
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "LoopLine \u00b7 v1.0",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            containerColor = SurfaceCardElevated,
            shape = LoopLineShapes.dialog,
            modifier = Modifier.metallicBevel(cornerDp = LoopLineShapes.dialogCornerDp),
            title = { Text("Reset everything?", style = MaterialTheme.typography.headlineMedium, color = TextPrimary) },
            text = {
                Text(
                    "This permanently clears your best levels, in-progress sessions, lifetime stats, and Daily Challenge streak. This can't be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                MetallicButton(
                    text = "Reset",
                    accentKey = "copper",
                    onClick = {
                        SettingsStore.resetAllProgress(context)
                        showResetConfirm = false
                        showResetDone = true
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (showResetDone) {
        AlertDialog(
            onDismissRequest = { showResetDone = false },
            containerColor = SurfaceCardElevated,
            shape = LoopLineShapes.dialog,
            modifier = Modifier.metallicBevel(cornerDp = LoopLineShapes.dialogCornerDp),
            title = { Text("Done", style = MaterialTheme.typography.headlineMedium, color = TextPrimary) },
            text = {
                Text(
                    "Everything's been reset. Next time you play, every difficulty starts fresh at Level 1.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                MetallicButton(text = "OK", accentKey = "gold", onClick = { showResetDone = false })
            }
        )
    }
}

@Composable
private fun SettingToggleRow(
    icon: ImageVector,
    accentColor: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LoopLineShapes.card)
            .background(cardSurfaceBrush())
            .metallicBevel(cornerDp = LoopLineShapes.cardCornerDp)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(40.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(accentColor.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(imageVector = icon, contentDescription = null, tint = accentColor)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = accentColor, checkedThumbColor = TextPrimary)
        )
    }
}
