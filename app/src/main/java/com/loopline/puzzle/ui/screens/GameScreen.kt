package com.loopline.puzzle.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.loopline.puzzle.game.Cell
import com.loopline.puzzle.game.GameSession
import com.loopline.puzzle.game.LevelRepository
import com.loopline.puzzle.game.PathSolver
import com.loopline.puzzle.game.ProgressStore
import com.loopline.puzzle.ui.components.IconChipButton
import com.loopline.puzzle.ui.components.MetallicButton
import com.loopline.puzzle.ui.theme.Copper
import com.loopline.puzzle.ui.theme.CopperHighlight
import com.loopline.puzzle.ui.theme.Gold
import com.loopline.puzzle.ui.theme.GoldHighlight
import com.loopline.puzzle.ui.theme.LoopLineShapes
import com.loopline.puzzle.ui.theme.RoseGold
import com.loopline.puzzle.ui.theme.RoseGoldHighlight
import com.loopline.puzzle.ui.theme.SurfaceCardElevated
import com.loopline.puzzle.ui.theme.TextOnMetal
import com.loopline.puzzle.ui.theme.TextPrimary
import com.loopline.puzzle.ui.theme.TextSecondary
import com.loopline.puzzle.ui.theme.TextTertiary
import com.loopline.puzzle.ui.theme.TileIdle
import com.loopline.puzzle.ui.theme.TileIdleShade
import com.loopline.puzzle.ui.theme.accentBrushFor
import com.loopline.puzzle.ui.theme.accentColorFor
import com.loopline.puzzle.ui.theme.accentDeepFor
import com.loopline.puzzle.ui.theme.accentHighlightFor
import com.loopline.puzzle.ui.theme.backgroundBrush
import com.loopline.puzzle.ui.theme.cardSurfaceBrush
import com.loopline.puzzle.ui.theme.drawMetallicBevel
import com.loopline.puzzle.ui.theme.goldBrush
import com.loopline.puzzle.ui.theme.metallicBevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val MAX_CELL_SIZE = 58.dp
private val MIN_CELL_SIZE = 32.dp
private val CELL_GAP = 10.dp

// Free for now since the person building this is wiring up monetization
// separately. To gate extra hints behind a rewarded ad later, this is the
// number to make ad-unlockable (e.g. grant +1 and re-check this constant
// after a rewarded-ad callback succeeds).
private const val MAX_HINTS_PER_LEVEL = 3

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
            modifier = Modifier.fillMaxSize().background(backgroundBrush()),
            contentAlignment = Alignment.Center
        ) {
            Text("Level not found", color = TextPrimary)
        }
        return
    }

    val path = remember(levelId) { mutableStateListOf(level.start) }
    val isComplete by remember(levelId) { derivedStateOf { path.size == level.cellCount } }
    val accent = remember(level.accentKey) { accentColorFor(level.accentKey) }
    val accentBrush = remember(level.accentKey) { accentBrushFor(level.accentKey) }
    val accentHighlight = remember(level.accentKey) { accentHighlightFor(level.accentKey) }
    val accentDeep = remember(level.accentKey) { accentDeepFor(level.accentKey) }

    var elapsedSeconds by remember(levelId) { mutableStateOf(0) }
    var completionSeconds by remember(levelId) { mutableStateOf(0) }
    var showDialog by remember(levelId) { mutableStateOf(false) }

    // Per-connection "juice": every time a new tile joins the stroke, these
    // reset and animate back to their resting value, driving (1) the newest
    // segment drawing itself in rather than popping into existence, (2) a
    // small bounce on the tile that just got claimed, and (3) an expanding
    // ring at that tile. Three different views of one satisfying moment.
    val connectProgress = remember(levelId) { Animatable(1f) }
    val burstProgress = remember(levelId) { Animatable(1f) }
    var justConnectedCell by remember(levelId) { mutableStateOf<Cell?>(null) }

    // A small light that continuously travels along the completed stroke -
    // ambient motion so the path reads as "alive" even between drags,
    // rather than only reacting the instant a tile is touched.
    val flowTransition = rememberInfiniteTransition(label = "pathFlow")
    val flowPhase by flowTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing)
        ),
        label = "flowPhase"
    )

    // A gentle, non-blocking nudge: if the player sits without extending
    // the stroke for a while, offer the existing Hint feature instead of
    // leaving them stuck. It never pauses or covers the board - it just
    // floats at the bottom until they either use it, dismiss it, or move
    // again (which cancels and restarts the idle timer).
    var showNeedHelp by remember(levelId) { mutableStateOf(false) }

    // Hint state: which cell (if any) to highlight as the suggested next
    // move, whether a solve is currently running in the background, and how
    // many hints have been spent on this particular level.
    var hintCell by remember(levelId) { mutableStateOf<Cell?>(null) }
    var isSolvingHint by remember(levelId) { mutableStateOf(false) }
    var hintsUsed by remember(levelId) { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    fun requestHint() {
        if (isComplete || isSolvingHint || hintsUsed >= MAX_HINTS_PER_LEVEL) return
        isSolvingHint = true
        val currentPath = path.toList()
        coroutineScope.launch {
            // The search can take a moment on a large board, so it runs off
            // the main thread; the lightbulb shows a spinner while it works.
            val solution = withContext(Dispatchers.Default) {
                PathSolver.solveRemaining(level.cells, currentPath)
            }
            isSolvingHint = false
            val next = solution?.firstOrNull() ?: return@launch
            hintCell = next
            hintsUsed += 1
        }
    }

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

    // Restarts every time the stroke's length changes (grows or retracts)
    // or the level completes - so any real progress hides the banner and
    // gives the player a fresh 9s window before it's offered again. A
    // manual dismiss (see the banner's onDismiss) isn't overridden by this
    // effect, since setting showNeedHelp = false doesn't re-trigger it.
    LaunchedEffect(levelId, path.size, isComplete) {
        showNeedHelp = false
        if (!isComplete && hintsUsed < MAX_HINTS_PER_LEVEL) {
            delay(9000)
            if (!isSolvingHint) showNeedHelp = true
        }
    }

    fun handleTouch(offset: Offset, cellPx: Float, stridePx: Float) {
        if (isComplete) return
        if (offset.x < 0 || offset.y < 0) return
        val col = (offset.x / stridePx).toInt()
        val row = (offset.y / stridePx).toInt()
        val candidate = Cell(row, col)
        if (candidate !in level.cells) return

        // Backtracking used to only check one step back (path[size - 2]),
        // which worked fine when the finger moved one tile at a time - but
        // a fast drag samples touch positions in bigger jumps, so it could
        // skip straight past that one cell and land two-or-more tiles back
        // on the stroke. That candidate was already `in path`, so it didn't
        // match the "extend" branch either, and nothing happened: the
        // stroke looked stuck moving backward, and the only way out was
        // tapping the start dot to reset the whole level. Checking the
        // touched cell's position anywhere in the current path - and
        // retracting to it - fixes that regardless of how many tiles the
        // drag skipped, while still behaving exactly like the old one-step
        // undo when it lands on path[size - 2], and like the old full reset
        // when it lands on the start dot.
        val existingIndex = path.indexOf(candidate)
        when {
            candidate == path.last() -> Unit
            existingIndex != -1 -> {
                while (path.size > existingIndex + 1) {
                    path.removeAt(path.lastIndex)
                }
                hintCell = null
            }
            candidate.isAdjacentTo(path.last()) -> {
                path.add(candidate)
                hintCell = null
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                justConnectedCell = candidate
                coroutineScope.launch {
                    connectProgress.snapTo(0f)
                    connectProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }
                coroutineScope.launch {
                    burstProgress.snapTo(0f)
                    burstProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 450, easing = LinearEasing)
                    )
                }
            }
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush())
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 12.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconChipButton(icon = Icons.Filled.ArrowBack, contentDescription = "Back", onClick = onBack)
            Spacer(modifier = Modifier.width(12.dp))
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
            if (isSolvingHint) {
                Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = accent
                    )
                }
            } else {
                IconChipButton(
                    icon = Icons.Filled.Lightbulb,
                    contentDescription = "Hint",
                    tint = if (hintsUsed < MAX_HINTS_PER_LEVEL) Gold else TextTertiary,
                    prominent = hintsUsed < MAX_HINTS_PER_LEVEL,
                    enabled = !isComplete && hintsUsed < MAX_HINTS_PER_LEVEL,
                    onClick = { requestHint() }
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconChipButton(
                icon = Icons.Filled.Refresh,
                contentDescription = "Restart",
                onClick = {
                    path.clear()
                    path.add(level.start)
                    elapsedSeconds = 0
                    hintCell = null
                    hintsUsed = 0
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${path.size} / ${level.cellCount} tiles \u00b7 ${elapsedSeconds}s \u00b7 " +
                "${MAX_HINTS_PER_LEVEL - hintsUsed} hint${if (MAX_HINTS_PER_LEVEL - hintsUsed == 1) "" else "s"} left",
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
                        val cellSize = Size(cellPx, cellPx)
                        val corner = CornerRadius(cellPx * 0.22f)
                        val inPath = cell in path
                        if (inPath) {
                            // The tile that was *just* connected bounces in
                            // (spring overshoot on connectProgress carries
                            // it slightly past 1.0 before settling) instead
                            // of simply appearing filled.
                            val popScale = if (cell == justConnectedCell) {
                                0.6f + connectProgress.value * 0.4f
                            } else {
                                1f
                            }
                            val tileCenter = Offset(topLeft.x + cellPx / 2f, topLeft.y + cellPx / 2f)
                            scale(scale = popScale, pivot = tileCenter) {
                                drawRoundRect(brush = accentBrush, topLeft = topLeft, size = cellSize, cornerRadius = corner)
                                drawMetallicBevel(
                                    topLeft = topLeft,
                                    boxSize = cellSize,
                                    cornerRadiusPx = corner.x,
                                    highlight = Color.White.copy(alpha = 0.4f),
                                    shadow = Color.Black.copy(alpha = 0.3f),
                                    strokeWidthPx = cellPx * 0.045f
                                )
                            }
                        } else {
                            drawRoundRect(color = TileIdle, topLeft = topLeft, size = cellSize, cornerRadius = corner)
                            drawMetallicBevel(
                                topLeft = topLeft,
                                boxSize = cellSize,
                                cornerRadiusPx = corner.x,
                                highlight = Color.White.copy(alpha = 0.55f),
                                shadow = TileIdleShade.copy(alpha = 0.7f),
                                strokeWidthPx = cellPx * 0.035f
                            )
                        }
                        if (cell == hintCell) {
                            drawRoundRect(
                                color = Gold,
                                topLeft = topLeft,
                                size = cellSize,
                                cornerRadius = corner,
                                style = Stroke(width = cellPx * 0.08f)
                            )
                        }
                    }

                    if (path.size > 1) {
                        for (i in 0 until path.size - 1) {
                            val a = path[i]
                            val b = path[i + 1]
                            val centerA = Offset(a.col * stridePx + cellPx / 2f, a.row * stridePx + cellPx / 2f)
                            val centerB = Offset(b.col * stridePx + cellPx / 2f, b.row * stridePx + cellPx / 2f)
                            // The newest segment draws itself in from the
                            // previous tile toward the new one rather than
                            // appearing instantly - a fluid "connect"
                            // rather than a static fill.
                            val isNewestSegment = i == path.size - 2
                            val drawnEnd = if (isNewestSegment) {
                                lerp(centerA, centerB, connectProgress.value.coerceIn(0f, 1f))
                            } else {
                                centerB
                            }
                            // Three-layer stroke - a darker under-edge, the
                            // core accent, and a thin bright sheen on top -
                            // reads as a polished metal rod rather than a
                            // flat line, echoing the same highlight/core/
                            // deep ramp used everywhere else in the app.
                            drawLine(
                                color = accentDeep,
                                start = centerA,
                                end = drawnEnd,
                                strokeWidth = cellPx * 0.18f,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = accent,
                                start = centerA,
                                end = drawnEnd,
                                strokeWidth = cellPx * 0.14f,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = accentHighlight.copy(alpha = 0.6f),
                                start = centerA,
                                end = drawnEnd,
                                strokeWidth = cellPx * 0.05f,
                                cap = StrokeCap.Round
                            )
                        }

                        // A small light traveling endlessly along the
                        // completed stroke - since every step is exactly
                        // one grid stride, each segment can be treated as
                        // equal-length for the purposes of placing it.
                        if (!isComplete) {
                            val segCount = path.size - 1
                            val scaledT = flowPhase * segCount
                            val segIndex = scaledT.toInt().coerceIn(0, segCount - 1)
                            val localT = (scaledT - segIndex).coerceIn(0f, 1f)
                            val sa = path[segIndex]
                            val sb = path[segIndex + 1]
                            val sparkCenter = lerp(
                                Offset(sa.col * stridePx + cellPx / 2f, sa.row * stridePx + cellPx / 2f),
                                Offset(sb.col * stridePx + cellPx / 2f, sb.row * stridePx + cellPx / 2f),
                                localT
                            )
                            drawCircle(color = accentHighlight.copy(alpha = 0.35f), radius = cellPx * 0.16f, center = sparkCenter)
                            drawCircle(color = Color.White.copy(alpha = 0.9f), radius = cellPx * 0.07f, center = sparkCenter)
                        }
                    }

                    // The expanding, fading ring left behind by the most
                    // recent connection - a quick ripple rather than the
                    // ambient spark above, which loops continuously.
                    justConnectedCell?.let { jc ->
                        if (burstProgress.value < 1f) {
                            val burstCenter = Offset(
                                jc.col * stridePx + cellPx / 2f,
                                jc.row * stridePx + cellPx / 2f
                            )
                            drawCircle(
                                color = accentHighlight.copy(alpha = (1f - burstProgress.value) * 0.55f),
                                radius = cellPx * (0.32f + burstProgress.value * 0.5f),
                                center = burstCenter,
                                style = Stroke(width = cellPx * 0.05f)
                            )
                        }
                    }

                    val startCenter = Offset(
                        level.start.col * stridePx + cellPx / 2f,
                        level.start.row * stridePx + cellPx / 2f
                    )
                    // A soft double glow behind the start dot, then a
                    // radial-gradient fill so it reads as a lit metal bead
                    // rather than a flat circle.
                    drawCircle(color = accent.copy(alpha = 0.18f), radius = cellPx * 0.55f, center = startCenter)
                    drawCircle(color = accent.copy(alpha = 0.28f), radius = cellPx * 0.38f, center = startCenter)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(accentHighlight, accent, accentDeep),
                            center = startCenter,
                            radius = cellPx * 0.26f
                        ),
                        radius = cellPx * 0.24f,
                        center = startCenter
                    )
                }

                if (isComplete) {
                    ConfettiBurst(modifier = Modifier.width(gridWidthDp).height(gridHeightDp))
                }
            }
        }
        }

        AnimatedVisibility(
            visible = showNeedHelp,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
        ) {
            NeedHelpBanner(
                onUseHint = {
                    showNeedHelp = false
                    requestHint()
                },
                onDismiss = { showNeedHelp = false }
            )
        }
    }

    if (showDialog) {
        LevelCompleteDialog(
            stars = starsFor(completionSeconds, level.cellCount),
            elapsedSeconds = completionSeconds,
            accentKey = level.accentKey,
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

/**
 * A floating, dismissible nudge - not a dialog. It never blocks the grid
 * or demands a choice before continuing; tapping it uses a hint the same
 * way the header's lightbulb does, and the small X lets the player wave
 * it off without any penalty or follow-up.
 */
@Composable
private fun NeedHelpBanner(
    onUseHint: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .widthIn(max = 320.dp)
            .shadow(
                elevation = 14.dp,
                shape = LoopLineShapes.card,
                ambientColor = Gold.copy(alpha = 0.3f),
                spotColor = Gold.copy(alpha = 0.35f)
            )
            .clip(LoopLineShapes.card)
            .background(cardSurfaceBrush())
            .border(width = 1.dp, color = Gold.copy(alpha = 0.3f), shape = LoopLineShapes.card)
            .clickable(onClick = onUseHint)
            .padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(goldBrush()),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Lightbulb,
                contentDescription = null,
                tint = TextOnMetal,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Need help?", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Text(
                "Tap for a hint on your next move",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Dismiss",
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/** A confetti particle: where it flies to, what color, and whether it's
 * drawn as a soft dot or a small diamond sparkle - the two shapes mixed
 * together read closer to scattered jewelry than a generic party popper. */
private data class ConfettiParticle(
    val angle: Float,
    val distance: Float,
    val color: Color,
    val isDiamond: Boolean,
    val sizeScale: Float
)

@Composable
private fun ConfettiBurst(modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(durationMillis = 700))
    }
    val particles = remember {
        val palette = listOf(Gold, GoldHighlight, Copper, CopperHighlight, RoseGold, RoseGoldHighlight)
        List(22) {
            ConfettiParticle(
                angle = Random.nextFloat() * 2f * Math.PI.toFloat(),
                distance = 50f + Random.nextFloat() * 90f,
                color = palette.random(),
                isDiamond = Random.nextBoolean(),
                sizeScale = 0.75f + Random.nextFloat() * 0.6f
            )
        }
    }
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val p = progress.value
        particles.forEach { particle ->
            val x = center.x + cos(particle.angle) * particle.distance * p
            val y = center.y + sin(particle.angle) * particle.distance * p - (90f * p)
            val alpha = (1f - p).coerceIn(0f, 1f)
            val radius = 5f * particle.sizeScale
            if (particle.isDiamond) {
                val diamond = Path().apply {
                    moveTo(x, y - radius)
                    lineTo(x + radius, y)
                    lineTo(x, y + radius)
                    lineTo(x - radius, y)
                    close()
                }
                drawPath(path = diamond, color = particle.color.copy(alpha = alpha))
            } else {
                drawCircle(color = particle.color.copy(alpha = alpha), radius = radius, center = Offset(x, y))
            }
        }
    }
}

@Composable
private fun LevelCompleteDialog(
    stars: Int,
    elapsedSeconds: Int,
    accentKey: String,
    onLevelSelect: () -> Unit,
    onNext: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* force a choice via the buttons below */ },
        containerColor = SurfaceCardElevated,
        shape = LoopLineShapes.dialog,
        modifier = Modifier.metallicBevel(cornerDp = LoopLineShapes.dialogCornerDp),
        title = {
            Text(
                "Level complete!",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
        },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) { index ->
                        Icon(
                            imageVector = if (index < stars) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = null,
                            tint = if (index < stars) Gold else TextTertiary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Solved in ${elapsedSeconds}s",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        },
        confirmButton = {
            MetallicButton(text = "Next level", onClick = onNext, accentKey = accentKey)
        },
        dismissButton = {
            TextButton(onClick = onLevelSelect) {
                Text("Change difficulty", color = TextSecondary)
            }
        }
    )
}
