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
import com.loopline.puzzle.ui.theme.AccentBlue
import com.loopline.puzzle.ui.theme.AccentOrange
import com.loopline.puzzle.ui.theme.SurfaceCard
import com.loopline.puzzle.ui.theme.TileOutline

/**
 * The LoopLine mark: a 2x2 tile grid with a single stroke connecting two
 * tiles and ending in a dot — a small, original visual reference to the
 * "one continuous line" puzzle mechanic, drawn entirely in code.
 */
@Composable
fun LoopLineLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.clip(RoundedCornerShape(28))) {
        val cell = size.width / 4f
        val tileSize = cell * 0.8f

        drawRect(color = SurfaceCard, size = size)

        val tileOrigins = listOf(
            Offset(cell * 1.2f, cell * 0.6f),
            Offset(cell * 2.2f, cell * 0.6f),
            Offset(cell * 1.2f, cell * 1.6f),
            Offset(cell * 2.2f, cell * 1.6f),
        )
        tileOrigins.forEach { origin ->
            drawRoundRect(
                color = TileOutline,
                topLeft = origin,
                size = Size(tileSize, tileSize),
                cornerRadius = CornerRadius(tileSize * 0.28f)
            )
        }

        val linePath = Path().apply {
            moveTo(cell * 1.6f, cell * 1.0f)
            lineTo(cell * 2.6f, cell * 1.0f)
            lineTo(cell * 2.6f, cell * 2.0f)
        }
        drawPath(
            path = linePath,
            brush = Brush.linearGradient(listOf(AccentBlue, AccentOrange)),
            style = Stroke(width = tileSize * 0.18f, cap = StrokeCap.Round)
        )

        drawCircle(
            color = AccentOrange,
            radius = tileSize * 0.22f,
            center = Offset(cell * 2.6f, cell * 2.0f)
        )
    }
}
