package com.loopline.puzzle.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.loopline.puzzle.game.Cell
import com.loopline.puzzle.game.GameSession
import com.loopline.puzzle.game.LevelRepository
import com.loopline.puzzle.game.ProgressStore
import com.loopline.puzzle.ui.theme.AccentBlue
import com.loopline.puzzle.ui.theme.AccentGreen
import com.loopline.puzzle.ui.theme.AccentOrange
import com.loopline.puzzle.ui.theme.BackgroundDark
import com.loopline.puzzle.ui.theme.SurfaceCard
import com.loopline.puzzle.ui.theme.TextPrimary
import com.loopline.puzzle.ui.theme.TextSecondary
import com.loopline.puzzle.ui.theme.TileIdle
import com.loopline.puzzle.ui.theme.accentColorFor
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val MAX_CELL_SIZE = 58.dp
private val MIN_CELL_SIZE = 32.dp
private val CELL_GAP = 10.dp

@Composable
fun GameScreen(
    levelId: Int,
    onBack: () -> Unit,
    onNavigateToLevel: (Int) -> Unit
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current

    // Generated levels live in GameSession's cache; the 3 handcrafted ones in
    // LevelRepository are kept as a fallback so a stale/bookmarked id never
    // shows a blank screen.
    val sessionLevel = remember(levelId) { GameSession.lookup(levelId) }
    val level = sessionLevel ?: LevelRepository.byId(levelId)

    if (level == null) {
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

    var elapsedSeconds by remember(levelId) { mutableStateOf(0) }
    var completionSeconds by remember(levelId) { mutableStateOf(0) }
    var showDialog by remember(levelId) { mutableStateOf(false) }

    LaunchedEffect(levelId) {
        while (true) {
            delay(1000)
            if (!isComplete) elapsedSeconds += 1 else break
        }
    }

    LaunchedEffect(isComplete) {
        if (isComplete) {
            completionSeconds = elapsedSeconds
            if (sessionLevel != null) {
                ProgressStore.recordLevelReached(context, GameSession.difficulty, GameSession.levelNumber)
            }
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(650) // let the confetti play before the dialog covers it
            showDialog = true
        } else {
            showDialog = false
        }
    }

    fun handleTouch(offset: Offset, cellPx: Float, stridePx: Float) {
        if (isComplete) return
        if (offset.x < 0 || offset.y < 0) return
        val col = (offset.x / stridePx).toInt()
        val row = (offset.y / stridePx).toInt()
        val candidate = Cell(row, col)
        if (candidate !in level.cells) return

        when {
            candidate == path.last() -> Unit
            candidate == level.start && path.size > 1 -> {
                path.clear()
                path.add(level.start)
            }
            path.size >= 2 && candidate == path[path.size - 2] -> {
                path.removeAt(path.lastIndex)
            }
            candidate !in path && candidate.isAdjacentTo(path.last()) -> {
                path.add(candidate)
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = level.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Text(
                    text = if (sessionLevel != null) GameSession.difficulty.label else "Preview",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            IconButton(onClick = {
                path.clear()
                path.add(level.start)
                elapsedSeconds = 0
            }) {
                Icon(Icons.Filled.Refresh, contentDescription = "Restart", tint = TextPrimary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${path.size} / ${level.cellCount} tiles \u00b7 ${elapsedSeconds}s",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(start = 24.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                val availableWidthPx = with(density) { maxWidth.toPx() }
                val gapPx = with(density) { CELL_GAP.toPx() }
                val maxCellPx = with(density) { MAX_CELL_SIZE.toPx() }
                val minCellPx = with(density) { MIN_CELL_SIZE.toPx() }

                val cellPx = ((availableWidthPx - gapPx * (level.cols - 1)) / level.cols)
                    .coerceAtMost(maxCellPx)
                    .coerceAtLeast(minCellPx)
                val stridePx = cellPx + gapPx

                val gridWidthDp = with(density) { (cellPx * level.cols + gapPx * (level.cols - 1)).toDp() }
                val gridHeightDp = with(density) { (cellPx * level.rows + gapPx * (level.rows - 1)).toDp() }

                Canvas(
                    modifier = Modifier
                        .width(gridWidthDp)
                        .height(gridHeightDp)
                        .pointerInput(levelId) {
                            detectDragGestures(
                                onDragStart = { offset -> handleTouch(offset, cellPx, stridePx) },
                                onDrag = { change, _ -> handleTouch(change.position, cellPx, stridePx) }
                            )
                        }
                ) {
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

                    val startCenter = Offset(
                        level.start.col * stridePx + cellPx / 2f,
                        level.start.row * stridePx + cellPx / 2f
                    )
                    drawCircle(color = accent, radius = cellPx * 0.24f, center = startCenter)
                }

                if (isComplete) {
                    ConfettiBurst(modifier = Modifier.width(gridWidthDp).height(gridHeightDp))
                }
            }
        }
    }

    if (showDialog) {
        LevelCompleteDialog(
            stars = starsFor(completionSeconds, level.cellCount),
            elapsedSeconds = completionSeconds,
            onLevelSelect = onBack,
            onNext = {
                val nextLevel = GameSession.next()
                onNavigateToLevel(nextLevel.id)
            }
        )
    }
}

private fun starsFor(seconds: Int, cellCount: Int): Int = when {
    seconds <= cellCount * 1.2 -> 3
    seconds <= cellCount * 2.5 -> 2
    else -> 1
}

@Composable
private fun ConfettiBurst(modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(durationMillis = 700))
    }
    val particles = remember {
        List(20) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val distance = 50f + Random.nextFloat() * 90f
            val color = listOf(AccentBlue, AccentOrange, AccentGreen).random()
            Triple(angle, distance, color)
        }
    }
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val p = progress.value
        particles.forEach { (angle, distance, color) ->
            val x = center.x + cos(angle) * distance * p
            val y = center.y + sin(angle) * distance * p - (90f * p)
            drawCircle(
                color = color.copy(alpha = (1f - p).coerceIn(0f, 1f)),
                radius = 5f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun LevelCompleteDialog(
    stars: Int,
    elapsedSeconds: Int,
    onLevelSelect: () -> Unit,
    onNext: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* force a choice via the buttons below */ },
        containerColor = SurfaceCard,
        title = { Text("Level complete!", color = TextPrimary) },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) { index ->
                        Icon(
                            imageVector = if (index < stars) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = null,
                            tint = if (index < stars) AccentOrange else TextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("Solved in ${elapsedSeconds}s", color = TextSecondary)
            }
        },
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
                Text("Change difficulty", color = TextSecondary)
            }
        }
    )
}
