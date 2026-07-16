package com.loopline.puzzle.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.loopline.puzzle.ui.theme.AccentBlue
import com.loopline.puzzle.ui.theme.SurfaceCard
import com.loopline.puzzle.ui.theme.TextPrimary
import com.loopline.puzzle.ui.theme.TextSecondary

@Composable
fun ComingSoonDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        title = { Text("Coming soon", color = TextPrimary) },
        text = { Text("This mode is still being built. Stay tuned!", color = TextSecondary) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it", color = AccentBlue)
            }
        }
    )
}
