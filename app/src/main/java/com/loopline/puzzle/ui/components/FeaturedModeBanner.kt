package com.loopline.puzzle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.loopline.puzzle.ui.theme.LoopLineShapes
import com.loopline.puzzle.ui.theme.TextOnMetal
import com.loopline.puzzle.ui.theme.TextPrimary
import com.loopline.puzzle.ui.theme.TextSecondary
import com.loopline.puzzle.ui.theme.accentBrushFor
import com.loopline.puzzle.ui.theme.accentColorFor
import com.loopline.puzzle.ui.theme.cardSurfaceBrush
import com.loopline.puzzle.ui.theme.metallicBevel

/**
 * The one hero card on Home: whatever's actually playable right now
 * (Classic). Deliberately not just another tile in the mode grid - it's a
 * full-width banner with its own "Play" button, the same way NumRush gives
 * its live Daily Challenge its own prominent row above the mode grid
 * instead of making it compete with placeholder tiles for attention.
 */
@Composable
fun FeaturedModeBanner(
    title: String,
    description: String,
    icon: ImageVector,
    accentKey: String,
    ctaText: String = "Play",
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val accentColor = accentColorFor(accentKey)
    val accentBrush = accentBrushFor(accentKey)

    Row(
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
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(LoopLineShapes.iconChip)
                .background(accentBrush)
                .metallicBevel(
                    cornerDp = LoopLineShapes.iconChipCornerDp,
                    highlight = Color.White.copy(alpha = 0.28f),
                    shadow = Color.Black.copy(alpha = 0.22f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = TextOnMetal)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 2
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        MetallicButton(text = ctaText, onClick = onClick, accentKey = accentKey)
    }
}
