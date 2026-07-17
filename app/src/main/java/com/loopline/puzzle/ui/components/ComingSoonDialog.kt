package com.loopline.puzzle.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.loopline.puzzle.ui.theme.LoopLineShapes
import com.loopline.puzzle.ui.theme.SurfaceCardElevated
import com.loopline.puzzle.ui.theme.TextPrimary
import com.loopline.puzzle.ui.theme.TextSecondary
import com.loopline.puzzle.ui.theme.metallicBevel

@Composable
fun ComingSoonDialog(
    onDismiss: () -> Unit,
    title: String = "Coming soon",
    message: String = "This mode is still being built. Stay tuned!"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCardElevated,
        shape = LoopLineShapes.dialog,
        modifier = Modifier.metallicBevel(cornerDp = LoopLineShapes.dialogCornerDp),
        title = { Text(title, style = MaterialTheme.typography.headlineMedium, color = TextPrimary) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary) },
        confirmButton = {
            MetallicButton(text = "Got it", onClick = onDismiss)
        }
    )
}
