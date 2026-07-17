package com.loopline.puzzle.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.loopline.puzzle.ui.theme.GoldHighlight
import com.loopline.puzzle.ui.theme.RoseGold
import com.loopline.puzzle.ui.theme.RoseGoldHighlight
import com.loopline.puzzle.ui.theme.SurfaceCardElevated
import com.loopline.puzzle.ui.theme.TileIdleShade
import com.loopline.puzzle.ui.theme.cardSurfaceBrush
import com.loopline.puzzle.ui.theme.goldBrush

/**
 * The LoopLine mark: a 2x2 tile grid with a single brushed-gold stroke
 * connecting two tiles and ending in a rose-gold dot — the same "one
 * continuous line" reference as before, redrawn in the metallic palette.
 * Still entirely code-drawn (Canvas), no image asset.
 */
@Composable
fun LoopLineLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.clip(RoundedCornerShape(28))) {
        val cell = size.width / 4f
        val tileSize = cell * 0.8f

        drawRect(brush = cardSurfaceBrush(), size = size)

        val tileOrigins = listOf(
            Offset(cell * 1.2f, cell * 0.6f),
            Offset(cell * 2.2f, cell * 0.6f),
            Offset(cell * 1.2f, cell * 1.6f),
            Offset(cell * 2.2f, cell * 1.6f),
        )
        tileOrigins.forEach { origin ->
            drawRoundRect(
                color = SurfaceCardElevated,
                topLeft = origin,
                size = Size(tileSize, tileSize),
                cornerRadius = CornerRadius(tileSize * 0.28f)
            )
            drawRoundRect(
                color = TileIdleShade.copy(alpha = 0.25f),
                topLeft = origin,
                size = Size(tileSize, tileSize),
                cornerRadius = CornerRadius(tileSize * 0.28f),
                style = Stroke(width = tileSize * 0.05f)
            )
        }

        val linePath = Path().apply {
            moveTo(cell * 1.6f, cell * 1.0f)
            lineTo(cell * 2.6f, cell * 1.0f)
            lineTo(cell * 2.6f, cell * 2.0f)
        }

        // Soft glow: a wider, faint pass beneath the crisp stroke — cheap
        // stand-in for a blur that still works on every API level.
        drawPath(
            path = linePath,
            brush = Brush.linearGradient(listOf(GoldHighlight.copy(alpha = 0.35f), RoseGoldHighlight.copy(alpha = 0.35f))),
            style = Stroke(width = tileSize * 0.32f, cap = StrokeCap.Round)
        )
        drawPath(
            path = linePath,
            brush = goldBrush(),
            style = Stroke(width = tileSize * 0.16f, cap = StrokeCap.Round)
        )

        val dotCenter = Offset(cell * 2.6f, cell * 2.0f)
        drawCircle(
            color = RoseGoldHighlight.copy(alpha = 0.30f),
            radius = tileSize * 0.36f,
            center = dotCenter
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(RoseGoldHighlight, RoseGold),
                center = dotCenter,
                radius = tileSize * 0.24f
            ),
            radius = tileSize * 0.22f,
            center = dotCenter
        )
    }
}
