package com.loopline.puzzle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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

@Composable
fun ModeCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentKey: String,
    onClick: () -> Unit,
    badgeText: String = "Coming soon",
    badgeHighlighted: Boolean = false
) {
    val accentColor = accentColorFor(accentKey)
    val accentBrush = accentBrushFor(accentKey)

    Column(
        modifier = Modifier
            .shadow(
                elevation = if (badgeHighlighted) 16.dp else 5.dp,
                shape = LoopLineShapes.card,
                ambientColor = accentColor.copy(alpha = 0.30f),
                spotColor = accentColor.copy(alpha = 0.38f)
            )
            .clip(LoopLineShapes.card)
            .background(cardSurfaceBrush())
            .metallicBevel(cornerDp = LoopLineShapes.cardCornerDp)
            .clickable(onClick = onClick)
            .padding(18.dp)
            .height(158.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(LoopLineShapes.iconChip)
                .background(
                    if (badgeHighlighted) accentBrush
                    else Brush.linearGradient(
                        listOf(accentColor.copy(alpha = 0.20f), accentColor.copy(alpha = 0.08f))
                    )
                )
                .metallicBevel(
                    cornerDp = LoopLineShapes.iconChipCornerDp,
                    highlight = Color.White.copy(alpha = 0.22f),
                    shadow = Color.Black.copy(alpha = 0.22f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (badgeHighlighted) TextOnMetal else accentColor
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(text = title, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            maxLines = 2
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .clip(LoopLineShapes.chip)
                .background(if (badgeHighlighted) accentBrush else Color.White.copy(alpha = 0.06f))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelLarge,
                color = if (badgeHighlighted) TextOnMetal else TextSecondary
            )
        }
    }
}
