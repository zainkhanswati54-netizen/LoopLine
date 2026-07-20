package com.loopline.puzzle.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.loopline.puzzle.game.Cell
import com.loopline.puzzle.game.DAILY_CHALLENGE_LEVEL_ID
import com.loopline.puzzle.game.DailyChallengeStore
import com.loopline.puzzle.game.GameSession
import com.loopline.puzzle.game.LevelRepository
import com.loopline.puzzle.game.ModeSession
import com.loopline.puzzle.game.PathSolver
import com.loopline.puzzle.game.PlayMode
import com.loopline.puzzle.game.ProgressStore
import com.loopline.puzzle.game.SettingsStore
import com.loopline.puzzle.game.SoundPlayer
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

/** Unifies GameSession.RestoredProgress and ModeSession.RestoredProgress,
 * which carry identical fields but are separate types since the two
 * session objects don't otherwise depend on each other. */
private data class RestoredSnapshot(val path: List<Cell>, val elapsedSeconds: Int, val hintsUsed: Int)

@Composable
fun GameScreen(
    levelId: Int,
    onBack: () -> Unit,
    onNavigateToLevel: (Int) -> Unit,
    onGoHome: () -> Unit
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current

    // Generated levels live in GameSession's cache; the 3 handcrafted ones in
    // LevelRepository are kept as a fallback so a stale/bookmarked id never
    // shows a blank screen.
    val sessionLevel = remember(levelId) { GameSession.lookup(levelId) }
    val level = sessionLevel ?: LevelRepository.byId(levelId)
    val isDailyChallenge = levelId == DAILY_CHALLENGE_LEVEL_ID
    val playMode = remember(levelId) { ModeSession.modeFor(levelId) }
    val isZen = playMode == PlayMode.ZEN
    val isTimed = playMode == PlayMode.TIMED

    if (level == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(backgroundBrush()),
            contentAlignment = Alignment.Center
        ) {
            Text("Level not found", color = TextPrimary)
        }
        return
    }

    // One SoundPool-backed player per time this screen is on screen - loaded
    // once up front so the very first connect has no playback lag, and
    // released when the player navigates away so the pool doesn't leak.
    val soundPlayer = remember { SoundPlayer.create(context) }
    DisposableEffect(Unit) {
        onDispose { soundPlayer.release() }
    }

    // If GameSession/ModeSession reconstructed this exact level from a
    // persisted session (app was restarted mid-puzzle), this carries the
    // stroke, timer, and hint count the player actually left off at -
    // consumed once so navigating onward to a genuinely fresh level
    // doesn't reapply it.
    val restored = remember(levelId) {
        if (playMode != null) {
            ModeSession.consumeRestoredProgress(levelId)?.let {
                RestoredSnapshot(it.path, it.elapsedSeconds, it.hintsUsed)
            }
        } else {
            GameSession.consumeRestoredProgress(levelId)?.let {
                RestoredSnapshot(it.path, it.elapsedSeconds, it.hintsUsed)
            }
        }
    }

    val path = remember(levelId) {
        mutableStateListOf<Cell>().apply {
            val restoredPath = restored?.path?.filter { it in level.cells }
            if (!restoredPath.isNullOrEmpty()) addAll(restoredPath) else add(level.start)
        }
    }
    val isComplete by remember(levelId) { derivedStateOf { path.size == level.cellCount } }
    val accent = remember(level.accentKey) { accentColorFor(level.accentKey) }
    val accentBrush = remember(level.accentKey) { accentBrushFor(level.accentKey) }
    val accentHighlight = remember(level.accentKey) { accentHighlightFor(level.accentKey) }
    val accentDeep = remember(level.accentKey) { accentDeepFor(level.accentKey) }

    var elapsedSeconds by remember(levelId) { mutableStateOf(restored?.elapsedSeconds ?: 0) }
    var completionSeconds by remember(levelId) { mutableStateOf(0) }
    var showDialog by remember(levelId) { mutableStateOf(false) }
    var isAdvancingLevel by remember(levelId) { mutableStateOf(false) }

    // Pausing freezes the clock (checked in the timer LaunchedEffect below)
    // and, because the Pause Menu is a real Dialog, also blocks every touch
    // from reaching the grid underneath - so "pausing" doesn't need to
    // separately suspend the drag handler or the decorative animations.
    var isPaused by remember(levelId) { mutableStateOf(false) }

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

    // A slow breathing glow behind the stroke - width and brightness tick
    // up and down together - layered under the existing three-tone stroke
    // so the whole path reads as a lit rod rather than a static ribbon.
    val glowTransition = rememberInfiniteTransition(label = "pathGlow")
    val glowPulse by glowTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
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
    var hintsUsed by remember(levelId) { mutableStateOf(restored?.hintsUsed ?: 0) }
    val coroutineScope = rememberCoroutineScope()

    // One place that knows which session actually owns this level, so the
    // many call sites that need to persist progress (every connect, every
    // hint, every background) don't each have to re-derive it. The Daily
    // Challenge deliberately does nothing here - it only records a result
    // once, on completion (see DailyChallengeStore.recordCompletion).
    fun persistProgress() {
        when {
            isDailyChallenge -> Unit
            playMode != null -> ModeSession.saveProgress(context, playMode, path.toList(), elapsedSeconds, hintsUsed)
            else -> GameSession.saveProgress(context, path.toList(), elapsedSeconds, hintsUsed)
        }
    }

    // Timed mode: a countdown instead of a stopwatch. Budget scales with
    // the puzzle's size (3s per tile, floor of 20s) so bigger levels get
    // proportionally more time rather than a single fixed number that's
    // generous early and brutal once the grid grows.
    val timeBudgetSeconds = remember(levelId) { (level.cellCount * 3).coerceAtLeast(20) }
    var showTimeUpDialog by remember(levelId) { mutableStateOf(false) }

    fun resetAttempt() {
        path.clear()
        path.add(level.start)
        elapsedSeconds = 0
        hintCell = null
        hintsUsed = 0
    }

    LaunchedEffect(elapsedSeconds, isComplete, isTimed) {
        if (isTimed && !isComplete && !showTimeUpDialog && elapsedSeconds >= timeBudgetSeconds) {
            isPaused = true
            showTimeUpDialog = true
        }
    }

    // Whenever the app itself goes to the background (home button, app
    // switcher, screen lock) - not just our own in-app Pause Menu - the
    // clock now actually stops instead of ticking away unseen, and whatever
    // progress exists so far is flushed to disk immediately. Bug this
    // fixes: the timer only ever checked the in-app `isPaused` flag, which
    // had no idea the app had left the foreground, so a level "played" for
    // 5 real seconds could read as several minutes solved after switching
    // away and back.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                isPaused = true
                persistProgress()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
            if (!isDailyChallenge) {
                if (playMode == null) ProgressStore.recordHintUsed(context)
                persistProgress()
            }
        }
    }

    LaunchedEffect(levelId) {
        while (true) {
            delay(1000)
            if (isComplete) break
            if (!isPaused) elapsedSeconds += 1
        }
    }

    LaunchedEffect(isComplete) {
        if (isComplete) {
            completionSeconds = elapsedSeconds
            if (isDailyChallenge) {
                DailyChallengeStore.recordCompletion(context, completionSeconds)
            } else if (sessionLevel != null) {
                ProgressStore.recordLevelReached(context, GameSession.difficulty, GameSession.levelNumber)
                ProgressStore.recordLevelCompletion(context)
                ProgressStore.recordSolveTime(context, GameSession.difficulty, completionSeconds)
            }
            if (SettingsStore.vibrationEnabled(context)) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }
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
    LaunchedEffect(levelId, path.size, isComplete, isPaused) {
        showNeedHelp = false
        if (!isComplete && !isPaused && hintsUsed < MAX_HINTS_PER_LEVEL) {
            delay(9000)
            if (!isSolvingHint) showNeedHelp = true
        }
    }

    // The old behavior let the system back button pop straight out of the
    // screen (or, on a double-press, out of the app) while the timer kept
    // running underneath - the "33s to 71s" background-timer bug. Now back
    // always opens the Pause Menu instead; once the menu's own Dialog is
    // showing, its default dismiss-on-back (see PauseMenuDialog's
    // onDismissRequest) resumes the game the same way tapping Continue
    // does, so back never has to be handled twice. It's disabled while the
    // level-complete dialog is up so the two modals can't stack.
    BackHandler(enabled = !isPaused && !showDialog) {
        isPaused = true
    }

    fun handleTouch(offset: Offset, cellPx: Float, stridePx: Float) {
        if (isComplete) return
        if (offset.x < 0 || offset.y < 0) return
        val col = (offset.x / stridePx).toInt()
        val row = (offset.y / stridePx).toInt()
        val candidate = Cell(row, col)
        if (candidate !in level.cells) return

        // The stroke is now one-directional: once a tile has been connected,
        // dragging back over it (or over the start dot) does nothing - it no
        // longer retracts the path. This used to double as an undo/reset
        // gesture, but it's a deliberate rule change: a wrong turn is now
        // permanent for that attempt, which raises the stakes of every move
        // instead of letting a careless drag get walked back for free. The
        // header's Restart button (already a separate, deliberate action)
        // is the only way back to the start now.
        val existingIndex = path.indexOf(candidate)
        when {
            candidate == path.last() -> Unit
            existingIndex != -1 -> Unit
            candidate.isAdjacentTo(path.last()) -> {
                path.add(candidate)
                hintCell = null
                if (SettingsStore.vibrationEnabled(context)) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                if (SettingsStore.soundEnabled(context)) {
                    soundPlayer.playConnect()
                }
                if (!isDailyChallenge) {
                    persistProgress()
                }
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
        // Background is its own layer (not just a Column modifier) so the
        // ambient particles below can sit on top of the gradient but
        // underneath the header/grid content, instead of a flat fill.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush())
        )
        AmbientParticles(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 12.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconChipButton(
                icon = Icons.Filled.Pause,
                contentDescription = "Pause",
                enabled = !isComplete,
                onClick = { isPaused = true }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = level.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Text(
                    text = when {
                        isDailyChallenge -> "Today's puzzle"
                        isZen -> "Zen \u00b7 Level ${ModeSession.levelNumberFor(PlayMode.ZEN)}"
                        isTimed -> "Timed \u00b7 Level ${ModeSession.levelNumberFor(PlayMode.TIMED)}"
                        sessionLevel != null -> GameSession.difficulty.label
                        else -> "Preview"
                    },
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
                    resetAttempt()
                    if (!isDailyChallenge) persistProgress()
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = run {
                val timeText = when {
                    isTimed -> "${(timeBudgetSeconds - elapsedSeconds).coerceAtLeast(0)}s left"
                    isZen -> null // no clock pressure in Zen - just tiles and hints
                    else -> "${elapsedSeconds}s"
                }
                listOfNotNull(
                    "${path.size} / ${level.cellCount} tiles",
                    timeText,
                    "${MAX_HINTS_PER_LEVEL - hintsUsed} hint${if (MAX_HINTS_PER_LEVEL - hintsUsed == 1) "" else "s"} left"
                ).joinToString(" \u00b7 ")
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (isTimed && timeBudgetSeconds - elapsedSeconds <= 10) Copper else TextSecondary,
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
                            // Soft glow underlay, widening and brightening
                            // with glowPulse - drawn first so the crisp
                            // stroke on top of it still reads clearly.
                            drawLine(
                                color = accent.copy(alpha = 0.20f + 0.14f * glowPulse),
                                start = centerA,
                                end = drawnEnd,
                                strokeWidth = cellPx * (0.28f + 0.10f * glowPulse),
                                cap = StrokeCap.Round
                            )

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
            isDailyChallenge = isDailyChallenge,
            streak = if (isDailyChallenge) DailyChallengeStore.currentStreak(context) else 0,
            showChangeDifficulty = !isDailyChallenge && playMode == null,
            onLevelSelect = onBack,
            onNext = {
                when {
                    isDailyChallenge -> {
                        showDialog = false
                        onBack()
                    }
                    playMode != null -> {
                        if (!isAdvancingLevel) {
                            isAdvancingLevel = true
                            showDialog = false
                            val nextLevel = ModeSession.next(context, playMode)
                            onNavigateToLevel(nextLevel.id)
                        }
                    }
                    !isAdvancingLevel -> {
                        // Bug this fixes: a fast double-tap on "Next level" used to
                        // fire this callback twice before the navigation away from
                        // this screen could take effect, and each call advanced
                        // GameSession by one real level - so two taps could skip a
                        // whole level. Dismissing the dialog immediately removes
                        // the button from the screen, and the flag is an extra
                        // guard for the one frame where it might still be tappable.
                        isAdvancingLevel = true
                        showDialog = false
                        val nextLevel = GameSession.next(context)
                        onNavigateToLevel(nextLevel.id)
                    }
                }
            }
        )
    }

    if (showTimeUpDialog) {
        TimeUpDialog(
            onRetry = {
                showTimeUpDialog = false
                resetAttempt()
                persistProgress()
                isPaused = false
            },
            onGoHome = {
                showTimeUpDialog = false
                onGoHome()
            }
        )
    }

    if (isPaused) {
        PauseMenuDialog(
            onContinue = { isPaused = false },
            onGoHome = {
                isPaused = false
                onGoHome()
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

/** One drifting mote: a fixed horizontal anchor plus a speed and phase so a
 * whole field of these never reads as one synchronized loop. */
private data class AmbientParticle(
    val seed: Float,
    val speed: Float,
    val phase: Float,
    val radius: Float,
    val color: Color
)

/**
 * Slow-drifting, glowing motes behind the grid - the "no flat color"
 * background. Each one rises from the bottom of the screen to the top on
 * its own loop, fading in and out at the edges so nothing pops in or out
 * abruptly, then wraps back to the bottom. Deliberately faint (peak alpha
 * ~0.3) so it reads as ambient texture, not as something competing with
 * the puzzle itself.
 */
@Composable
private fun AmbientParticles(modifier: Modifier = Modifier) {
    val particles = remember {
        val palette = listOf(Gold, Copper, RoseGold, GoldHighlight)
        List(16) {
            AmbientParticle(
                seed = Random.nextFloat(),
                speed = 0.55f + Random.nextFloat() * 0.7f,
                phase = Random.nextFloat() * 2f * Math.PI.toFloat(),
                radius = 1.5f + Random.nextFloat() * 2.5f,
                color = palette.random()
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "ambientParticles")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 26000, easing = LinearEasing)),
        label = "ambientTime"
    )
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas
        particles.forEach { p ->
            val travel = (time * p.speed + p.seed).mod(1f)
            val y = h * (1f - travel)
            val sway = sin(travel * 4f * Math.PI.toFloat() + p.phase) * (w * 0.025f)
            var x = p.seed * w + sway
            if (x < 0f) x += w
            if (x > w) x -= w
            // Fade in near the bottom, fade out near the top, so the
            // wrap-around point is never visible as a hard pop.
            val alpha = minOf(travel * 6f, (1f - travel) * 6f).coerceIn(0f, 1f) * 0.32f
            drawCircle(color = p.color.copy(alpha = alpha * 0.4f), radius = p.radius * 2.6f, center = Offset(x, y))
            drawCircle(color = p.color.copy(alpha = alpha), radius = p.radius, center = Offset(x, y))
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
        progress.animateTo(1f, animationSpec = tween(durationMillis = 850))
    }
    val particles = remember {
        val palette = listOf(Gold, GoldHighlight, Copper, CopperHighlight, RoseGold, RoseGoldHighlight)
        List(28) {
            ConfettiParticle(
                angle = Random.nextFloat() * 2f * Math.PI.toFloat(),
                distance = 50f + Random.nextFloat() * 100f,
                color = palette.random(),
                isDiamond = Random.nextBoolean(),
                sizeScale = 0.75f + Random.nextFloat() * 0.6f
            )
        }
    }
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val p = progress.value
        // A quick soft-white flash at the moment of completion, expanding
        // and fading under the confetti - the "reward" punch before the
        // particles take over.
        val flashAlpha = (1f - p).coerceIn(0f, 1f) * 0.45f
        if (flashAlpha > 0f) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = flashAlpha), Color.Transparent),
                    center = center,
                    radius = size.minDimension * (0.35f + p * 0.5f)
                ),
                radius = size.minDimension * (0.35f + p * 0.5f),
                center = center
            )
        }
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
    isDailyChallenge: Boolean = false,
    streak: Int = 0,
    showChangeDifficulty: Boolean = true,
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
                if (isDailyChallenge) "Daily Challenge complete!" else "Level complete!",
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
                if (isDailyChallenge) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        if (streak > 1) "\ud83d\udd25 $streak day streak \u2014 come back tomorrow for the next one" else "Come back tomorrow for the next one",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        },
        confirmButton = {
            MetallicButton(
                text = if (isDailyChallenge) "Back to Home" else "Next level",
                onClick = onNext,
                accentKey = accentKey
            )
        },
        dismissButton = {
            if (showChangeDifficulty) {
                TextButton(onClick = onLevelSelect) {
                    Text("Change difficulty", color = TextSecondary)
                }
            }
        }
    )
}

/**
 * Continue resumes exactly where the player left off (the timer never
 * moved while this was up); Go to Home exits to mode selection. Tapping
 * outside the dialog or pressing system back both count as "Continue" via
 * onDismissRequest, since a real Dialog window intercepts back itself -
 * the BackHandler in GameScreen only needs to handle the *unpaused* case.
 */
@Composable
private fun PauseMenuDialog(
    onContinue: () -> Unit,
    onGoHome: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onContinue,
        containerColor = SurfaceCardElevated,
        shape = LoopLineShapes.dialog,
        modifier = Modifier.metallicBevel(cornerDp = LoopLineShapes.dialogCornerDp),
        title = {
            Text("Paused", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
        },
        text = {
            Text(
                "The clock's stopped \u2014 take your time.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        },
        confirmButton = {
            MetallicButton(text = "Continue", onClick = onContinue)
        },
        dismissButton = {
            TextButton(onClick = onGoHome) {
                Text("Go to Home", color = TextSecondary)
            }
        }
    )
}

/**
 * Timed mode's failure state: the countdown hit zero before the stroke
 * covered every tile. Retry resets this same attempt (same puzzle, same
 * level number - it doesn't burn a level or regenerate a new one); Go to
 * Home leaves the mode entirely.
 */
@Composable
private fun TimeUpDialog(
    onRetry: () -> Unit,
    onGoHome: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onRetry,
        containerColor = SurfaceCardElevated,
        shape = LoopLineShapes.dialog,
        modifier = Modifier.metallicBevel(cornerDp = LoopLineShapes.dialogCornerDp),
        title = {
            Text("Time's up!", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
        },
        text = {
            Text(
                "The clock ran out before the stroke covered every tile. Give this one another go?",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        },
        confirmButton = {
            MetallicButton(text = "Retry", onClick = onRetry, accentKey = "copper")
        },
        dismissButton = {
            TextButton(onClick = onGoHome) {
                Text("Go to Home", color = TextSecondary)
            }
        }
    )
}
