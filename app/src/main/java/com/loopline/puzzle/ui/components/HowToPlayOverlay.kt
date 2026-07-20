package com.loopline.puzzle.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.loopline.puzzle.game.Cell
import com.loopline.puzzle.ui.theme.Gold
import com.loopline.puzzle.ui.theme.LoopLineShapes
import com.loopline.puzzle.ui.theme.SurfaceCardElevated
import com.loopline.puzzle.ui.theme.TextOnMetal
import com.loopline.puzzle.ui.theme.TextPrimary
import com.loopline.puzzle.ui.theme.TextSecondary
import com.loopline.puzzle.ui.theme.TileIdle
import com.loopline.puzzle.ui.theme.accentBrushFor
import com.loopline.puzzle.ui.theme.accentColorFor
import com.loopline.puzzle.ui.theme.goldBrush
import com.loopline.puzzle.ui.theme.metallicBevel
import kotlinx.coroutines.delay

/**
 * A tiny, self-contained 3x3 demo board with a hand icon that continuously
 * drags itself along a fixed snake path - shows *how* the drag mechanic
 * works instead of describing it in a paragraph.
 *
 * Deliberately built as plain [Animatable] + [Canvas] rather than
 * `ObjectAnimator` + an `ImageView`: those are the classic-View-system
 * tools this project doesn't use (it's Jetpack Compose throughout). The
 * Compose equivalent of "animate a view's position along a path" is
 * animating an offset value with `Animatable`/`LaunchedEffect` and reading
 * it during layout/draw - same idea, just expressed in Compose's own
 * animation APIs instead of a View `Animator`.
 */
private val demoPath = listOf(
    Cell(0, 0), Cell(0, 1), Cell(0, 2),
    Cell(1, 2), Cell(2, 2), Cell(2, 1), Cell(2, 0)
)

@Composable
private fun DragDemo(modifier: Modifier = Modifier) {
    // Progress walks 0f..(demoPath.size - 1) across the whole path, then
    // pauses, snaps back to the start, and loops - an endless looping demo
    // rather than a one-shot animation that would need re-triggering.
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            progress.snapTo(0f)
            delay(500)
            for (step in 0 until demoPath.size - 1) {
                progress.animateTo(
                    targetValue = (step + 1).toFloat(),
                    animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing)
                )
            }
            delay(900)
        }
    }

    val accent = remember { accentColorFor("gold") }
    val accentBrush = remember { accentBrushFor("gold") }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(180.dp)) {
            val cellPx = size.width / 3f
            val gap = cellPx * 0.12f
            val tile = cellPx - gap

            fun centerOf(cell: Cell) = Offset(
                cell.col * cellPx + cellPx / 2f,
                cell.row * cellPx + cellPx / 2f
            )

            val visitedCount = progress.value.toInt() + 1

            demoPath.forEachIndexed { index, cell ->
                val topLeft = Offset(cell.col * cellPx + gap / 2f, cell.row * cellPx + gap / 2f)
                val corner = CornerRadius(tile * 0.22f)
                if (index < visitedCount) {
                    drawRoundRect(
                        brush = accentBrush,
                        topLeft = topLeft,
                        size = Size(tile, tile),
                        cornerRadius = corner
                    )
                } else {
                    drawRoundRect(
                        color = TileIdle,
                        topLeft = topLeft,
                        size = Size(tile, tile),
                        cornerRadius = corner
                    )
                }
            }

            // The connecting stroke, drawn up to the hand's current
            // fractional position along the path.
            for (i in 0 until demoPath.size - 1) {
                val segStart = i.toFloat()
                if (progress.value <= segStart) continue
                val a = centerOf(demoPath[i])
                val b = centerOf(demoPath[i + 1])
                val t = (progress.value - segStart).coerceIn(0f, 1f)
                val end = lerp(a, b, t)
                drawLine(color = accent, start = a, end = end, strokeWidth = tile * 0.14f, cap = StrokeCap.Round)
            }
        }

        // The hand icon rides on top of the Canvas, positioned via the
        // same progress value - Compose's version of an ObjectAnimator
        // moving an ImageView along a path.
        val cellDp = 180.dp / 3
        val currentIndex = progress.value.toInt().coerceIn(0, demoPath.size - 2)
        val t = (progress.value - currentIndex).coerceIn(0f, 1f)
        val a = demoPath[currentIndex]
        val b = demoPath[currentIndex + 1]
        val col = a.col + (b.col - a.col) * t
        val row = a.row + (b.row - a.row) * t

        Icon(
            imageVector = Icons.Filled.TouchApp,
            contentDescription = null,
            tint = TextOnMetal,
            modifier = Modifier
                .size(30.dp)
                .offset(
                    x = cellDp * (col + 0.5f) - 15.dp,
                    y = cellDp * (row + 0.5f) - 15.dp
                )
        )
    }
}

/**
 * Full-screen "How to Play" overlay for new players - shown automatically
 * the first time Home loads (gated by [com.loopline.puzzle.game.SettingsStore.hasSeenTutorial])
 * and reopenable any time from Home's "?" chip.
 */
@Composable
fun HowToPlayOverlay(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.72f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .shadow(
                        elevation = 28.dp,
                        shape = LoopLineShapes.dialog,
                        ambientColor = Gold.copy(alpha = 0.25f),
                        spotColor = Gold.copy(alpha = 0.3f)
                    )
                    .clip(LoopLineShapes.dialog)
                    .background(SurfaceCardElevated)
                    .metallicBevel(cornerDp = LoopLineShapes.dialogCornerDp)
                    .padding(horizontal = 26.dp, vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GradientText(
                    text = "How to Play",
                    brush = goldBrush(),
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(18.dp))

                DragDemo()

                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    "Drag from tile to tile to connect every one in a single stroke.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Once a tile is connected it stays connected - there's no dragging back, so a wrong turn is permanent for that attempt.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                MetallicButton(
                    text = "Got it",
                    onClick = onDismiss,
                    accentKey = "gold",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
