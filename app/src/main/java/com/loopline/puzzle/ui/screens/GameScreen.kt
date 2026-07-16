package com.loopline.puzzle.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.loopline.puzzle.game.Cell
import com.loopline.puzzle.game.LevelRepository
import com.loopline.puzzle.ui.theme.AccentBlue
import com.loopline.puzzle.ui.theme.BackgroundDark
import com.loopline.puzzle.ui.theme.SurfaceCard
import com.loopline.puzzle.ui.theme.TextPrimary
import com.loopline.puzzle.ui.theme.TextSecondary
import com.loopline.puzzle.ui.theme.TileIdle
import com.loopline.puzzle.ui.theme.accentColorFor

private val CELL_SIZE = 58.dp
private val CELL_GAP = 10.dp

@Composable
fun GameScreen(
    levelId: Int,
    onBack: () -> Unit,
    onNextLevel: (Int) -> Unit,
    onNoMoreLevels: () -> Unit
) {
    val level = remember(levelId) { LevelRepository.byId(levelId) }

    if (level == null) {
        // Unknown level id - shouldn't normally happen, but fail safe.
        Box(
            modifier = Modifier.fillMaxSize().background(BackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            Text("Level not found", color = TextPrimary)
        }
        return
    }

    val path = remember(levelId) { mutableStateListOf(level.start) }
    val isComplete by remember(levelId) { derivedStateOf { path.size == level.cellCount } }
    val accent = remember(level.accentKey) { accentColorFor(level.accentKey) }
    val density = LocalDensity.current

    val cellPx = with(density) { CELL_SIZE.toPx() }
    val gapPx = with(density) { CELL_GAP.toPx() }
    val stridePx = cellPx + gapPx

    fun cellAt(offset: Offset): Cell? {
        if (offset.x < 0 || offset.y < 0) return null
        val col = (offset.x / stridePx).toInt()
        val row = (offset.y / stridePx).toInt()
        val candidate = Cell(row, col)
        return if (candidate in level.cells) candidate else null
    }

    fun handleTouch(offset: Offset) {
        if (isComplete) return
        val candidate = cellAt(offset) ?: return

        when {
            candidate == path.last() -> Unit // finger sitting on current end, no-op
            candidate == level.start && path.size > 1 -> {
                // Touching the start dot again resets the path - quick restart gesture.
                path.clear()
                path.add(level.start)
            }
            path.size >= 2 && candidate == path[path.size - 2] -> {
                // Stepping back onto the previous cell undoes the last move.
                path.removeAt(path.lastIndex)
            }
            candidate !in path && candidate.isAdjacentTo(path.last()) -> {
                path.add(candidate)
            }
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 4.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                text = level.title,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                path.clear()
                path.add(level.start)
            }) {
                Icon(Icons.Filled.Refresh, contentDescription = "Restart", tint = TextPrimary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${path.size} / ${level.cellCount} tiles",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(start = 24.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val gridWidth = CELL_SIZE * level.cols + CELL_GAP * (level.cols - 1)
            val gridHeight = CELL_SIZE * level.rows + CELL_GAP * (level.rows - 1)

            Canvas(
                modifier = Modifier
                    .width(gridWidth)
                    .height(gridHeight)
                    .pointerInput(levelId) {
                        detectDragGestures(
                            onDragStart = { offset -> handleTouch(offset) },
                            onDrag = { change, _ -> handleTouch(change.position) }
                        )
                    }
            ) {
                // idle / filled tiles
                level.cells.forEach { cell ->
                    val topLeft = Offset(cell.col * stridePx, cell.row * stridePx)
                    val inPath = cell in path
                    drawRoundRect(
                        color = if (inPath) accent.copy(alpha = 0.35f) else TileIdle,
                        topLeft = topLeft,
                        size = Size(cellPx, cellPx),
                        cornerRadius = CornerRadius(cellPx * 0.22f)
                    )
                }

                // connecting stroke through the drawn path
                if (path.size > 1) {
                    for (i in 0 until path.size - 1) {
                        val a = path[i]
                        val b = path[i + 1]
                        val centerA = Offset(a.col * stridePx + cellPx / 2f, a.row * stridePx + cellPx / 2f)
                        val centerB = Offset(b.col * stridePx + cellPx / 2f, b.row * stridePx + cellPx / 2f)
                        drawLine(
                            color = accent,
                            start = centerA,
                            end = centerB,
                            strokeWidth = cellPx * 0.16f,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // start dot
                val startCenter = Offset(
                    level.start.col * stridePx + cellPx / 2f,
                    level.start.row * stridePx + cellPx / 2f
                )
                drawCircle(color = accent, radius = cellPx * 0.24f, center = startCenter)
            }
        }
    }

    if (isComplete) {
        LevelCompleteDialog(
            onLevelSelect = onBack,
            onNext = {
                val nextId = LevelRepository.nextIdAfter(levelId)
                if (nextId != null) onNextLevel(nextId) else onNoMoreLevels()
            }
        )
    }
}

@Composable
private fun LevelCompleteDialog(onLevelSelect: () -> Unit, onNext: () -> Unit) {
    AlertDialog(
        onDismissRequest = { /* force a choice via the buttons below */ },
        containerColor = SurfaceCard,
        title = { Text("Level complete!", color = TextPrimary) },
        text = { Text("Nice line. Every tile connected in one stroke.", color = TextSecondary) },
        confirmButton = {
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Next level")
            }
        },
        dismissButton = {
            TextButton(onClick = onLevelSelect) {
                Text("Level select", color = TextSecondary)
            }
        }
    )
}
