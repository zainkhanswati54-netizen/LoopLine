package com.loopline.puzzle.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ChevronRight
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
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var showTerms by remember { mutableStateOf(false) }

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
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
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
                text = "ABOUT",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(LoopLineShapes.card)
                    .background(cardSurfaceBrush())
                    .metallicBevel(cornerDp = LoopLineShapes.cardCornerDp)
            ) {
                InfoRow(
                    icon = Icons.Filled.PrivacyTip,
                    title = "Privacy Policy",
                    onClick = { showPrivacyPolicy = true }
                )
                InfoRow(
                    icon = Icons.Filled.Article,
                    title = "Terms of Service",
                    onClick = { showTerms = true }
                )
                InfoRow(
                    icon = Icons.Filled.Star,
                    title = "Rate LoopLine",
                    onClick = {
                        val uri = Uri.parse("market://details?id=${context.packageName}")
                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                            setPackage("com.android.vending")
                        }
                        runCatching { context.startActivity(intent) }.onFailure {
                            // Play Store app isn't installed (e.g. an emulator, or
                            // a device without Google Play) - fall back to the
                            // plain web listing in whatever browser is available.
                            val webUri = Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                        }
                    }
                )
                InfoRow(
                    icon = Icons.Filled.Share,
                    title = "Share with a friend",
                    onClick = {
                        val shareText = "Try LoopLine - a one-stroke tile puzzle game: " +
                            "https://play.google.com/store/apps/details?id=${context.packageName}"
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share LoopLine"))
                    }
                )
                InfoRow(
                    icon = Icons.Filled.Email,
                    title = "Contact / Feedback",
                    isLast = true,
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@loopline.game"))
                            putExtra(Intent.EXTRA_SUBJECT, "LoopLine feedback")
                        }
                        runCatching { context.startActivity(intent) }
                    }
                )
            }

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
            Spacer(modifier = Modifier.height(24.dp))
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

    if (showPrivacyPolicy) {
        LegalTextDialog(
            title = "Privacy Policy",
            body = PRIVACY_POLICY_TEXT,
            onDismiss = { showPrivacyPolicy = false }
        )
    }

    if (showTerms) {
        LegalTextDialog(
            title = "Terms of Service",
            body = TERMS_OF_SERVICE_TEXT,
            onDismiss = { showTerms = false }
        )
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    title: String,
    isLast: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        androidx.compose.material3.Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = TextSecondary
        )
    }
    if (!isLast) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .padding(horizontal = 20.dp)
                .background(TextSecondary.copy(alpha = 0.12f))
        )
    }
}

@Composable
private fun LegalTextDialog(title: String, body: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCardElevated,
        shape = LoopLineShapes.dialog,
        modifier = Modifier.metallicBevel(cornerDp = LoopLineShapes.dialogCornerDp),
        title = { Text(title, style = MaterialTheme.typography.headlineSmall, color = TextPrimary) },
        text = {
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            )
        },
        confirmButton = {
            MetallicButton(text = "Close", accentKey = "gold", onClick = onDismiss)
        }
    )
}

/**
 * Placeholder legal copy so the app isn't shipping with dead menu items.
 * This is a reasonable starting template for a simple offline single-player
 * game with no accounts, no ads, and no analytics SDK - but it isn't legal
 * advice, and the contact address below is a placeholder. Swap in your own
 * support email and have someone review the actual text before shipping to
 * the Play Store, especially if that changes (ads, analytics, accounts,
 * a real online leaderboard, etc).
 */
private const val PRIVACY_POLICY_TEXT = """LoopLine is a single-player puzzle game. It does not require an account, does not collect personal information, and does not use analytics or advertising SDKs.

All game data - your level progress, best times, hint usage, settings, and Daily Challenge streak - is stored only on your device, using Android's local app storage. Nothing is uploaded to a server, and nothing is shared with third parties.

Uninstalling the app, or using "Reset everything" in Settings, permanently deletes this local data.

If a future version of LoopLine adds online features (such as a shared leaderboard), this policy will be updated before that feature is released, and you'll be able to review the change here.

Questions about this policy can be sent using "Contact / Feedback" above."""

private const val TERMS_OF_SERVICE_TEXT = """By playing LoopLine, you agree to the following:

1. License: You're granted a personal, non-transferable license to install and play LoopLine for your own entertainment. The game and its assets remain the property of their creator.

2. No warranty: LoopLine is provided "as is," without warranty of any kind. While care is taken to keep it working correctly, it's not guaranteed to be bug-free or uninterrupted.

3. Local data: Your progress is stored locally on your device only (see the Privacy Policy above). The developer isn't responsible for data lost due to uninstalling the app, switching devices, or device failure.

4. Acceptable use: Don't reverse-engineer, redistribute, or resell the app outside of what the platform (e.g. Google Play) you downloaded it from allows.

5. Changes: These terms may be updated in future versions; continuing to play after an update means you accept the revised terms.

Questions about these terms can be sent using "Contact / Feedback" above."""

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
