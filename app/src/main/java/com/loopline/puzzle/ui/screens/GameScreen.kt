package com.loopline.puzzle.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.loopline.puzzle.game.Cell
import com.loopline.puzzle.game.DAILY_CHALLENGE_LEVEL_ID
import com.loopline.puzzle.game.DailyChallengeStore
import com.loopline.puzzle.game.GameSession
import com.loopline.puzzle.game.Level
import com.loopline.puzzle.game.LevelRepository
import com.loopline.puzzle.game.ModeSession
import com.loopline.puzzle.game.PathSolver
import com.loopline.puzzle.game.PlayMode
import com.loopline.puzzle.game.ProgressStore
import com.loopline.puzzle.game.SettingsStore
import com.loopline.puzzle.game.SoundPlayer
import com.loopline.puzzle.ui.components.GradientText
import com.loopline.puzzle.ui.components.IconChipButton
import com.loopline.puzzle.ui.components.MetallicButton
import com.loopline.puzzle.ui.components.StreakChip
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
import com.loopline.puzzle.ui.theme.goldBrush
import com.loopline.puzzle.ui.theme.metallicBevel
import com.loopline.puzzle.ui.theme.shake
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

// How long the tile-shrink/pop-out + success sound beat plays before the
// next level loads itself - the entire reward moment now, since there's
// no stars dialog or "Next level" button to tap anymore. ~500ms is enough
// to read as a deliberate celebration without the flow ever stalling.
private const val SUCCESS_ANIMATION_MILLIS = 500L

/** Unifies GameSession.RestoredProgress and ModeSession.RestoredProgress,
 * which carry identical fields but are separate types since the two
 * session objects don't otherwise depend on each other. */
private data class RestoredSnapshot(val path: List<Cell>, val elapsedSeconds: Int, val hintsUsed: Int)

/**
 * Precomputed, per-cell draw geometry for the puzzle grid - built once per
 * (level, cellPx) via [buildCellGeometry]/`remember` instead of on every
 * single frame.
 *
 * Perf note: the grid's flowing-spark and glow-pulse animations keep the
 * whole Canvas redrawing at ~60fps for as long as the level is on screen.
 * Before this, every one of those frames called `drawMetallicBevel` for
 * every cell, which built a brand-new Path (via RoundRect) and a new
 * linear-gradient Brush from scratch each time - for an outline that never
 * actually changes once a cell's position and size are fixed. On a grid
 * with ~60-100 cells that's thousands of extra allocations a second doing
 * nothing but generating garbage-collector pauses, which is the most
 * likely source of drawing-related jank. Idle and filled tiles use
 * slightly different inset/stroke widths, so each cell keeps one cached
 * Path+Brush pair per state rather than one shared pair.
 */
private class CellGeometry(
    val topLeft: Offset,
    val size: Size,
    val corner: CornerRadius,
    val idleBevelPath: Path,
    val idleBevelBrush: Brush,
    val filledBevelPath: Path,
    val filledBevelBrush: Brush
)

private fun bevelPath(topLeft: Offset, size: Size, cornerRadiusPx: Float, strokeWidthPx: Float): Path {
    val inset = strokeWidthPx / 2f
    val rect = RoundRect(
        left = topLeft.x + inset,
        top = topLeft.y + inset,
        right = topLeft.x + size.width - inset,
        bottom = topLeft.y + size.height - inset,
        cornerRadius = CornerRadius(cornerRadiusPx)
    )
    return Path().apply { addRoundRect(rect) }
}

private fun bevelBrush(topLeft: Offset, size: Size, highlight: Color, shadow: Color): Brush =
    Brush.linearGradient(
        colors = listOf(highlight, Color.Transparent, shadow),
        start = Offset(topLeft.x, topLeft.y),
        end = Offset(topLeft.x + size.width, topLeft.y + size.height)
    )

private fun buildCellGeometry(level: Level, cellPx: Float, stridePx: Float): Map<Cell, CellGeometry> =
    level.cells.associateWith { cell ->
        val topLeft = Offset(cell.col * stridePx, cell.row * stridePx)
        val cellSize = Size(cellPx, cellPx)
        val corner = CornerRadius(cellPx * 0.22f)
        CellGeometry(
            topLeft = topLeft,
            size = cellSize,
            corner = corner,
            idleBevelPath = bevelPath(topLeft, cellSize, corner.x, cellPx * 0.035f),
            idleBevelBrush = bevelBrush(topLeft, cellSize, Color.White.copy(alpha = 0.55f), TileIdleShade.copy(alpha = 0.7f)),
            filledBevelPath = bevelPath(topLeft, cellSize, corner.x, cellPx * 0.045f),
            filledBevelBrush = bevelBrush(topLeft, cellSize, Color.White.copy(alpha = 0.4f), Color.Black.copy(alpha = 0.3f))
        )
    }

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

    // Invalid-move feedback: dragging onto a non-adjacent/already-passed
    // tile turns the stroke red for a beat and gives the whole grid a
    // quick shake, instead of just silently doing nothing. invalidFlash
    // drives the red color blend on the stroke (1 = full red, fading back
    // to the normal accent); shakeTrigger is an ever-incrementing key so
    // Modifier.shake replays even on back-to-back wrong touches;
    // lastInvalidCandidate debounces so dragging around on the *same*
    // wrong tile doesn't refire the shake/sound every pointer-move frame.
    val invalidFlash = remember(levelId) { Animatable(0f) }
    var shakeTrigger by remember(levelId) { mutableStateOf(0) }
    var lastInvalidCandidate by remember(levelId) { mutableStateOf<Cell?>(null) }
    // Counts actual wrong-tile touches this attempt (not debounced, unlike
    // shakeTrigger's dedupe) - a level only counts as "perfect" for the
    // streak below if this stays 0 and no hint was used.
    var wrongMoveCount by remember(levelId) { mutableStateOf(0) }

    // Drives the "level complete" tile shrink/pop-out: every filled tile
    // scales down toward 0 together, timed alongside the success sound,
    // right before the next level loads itself.
    val tileExitProgress = remember(levelId) { Animatable(0f) }

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

    // Perfect-solve streak: loaded once (not per-levelId, so it survives
    // across the auto-advance to the next level rather than resetting every
    // time this composable re-keys) and bumped in the completion effect
    // below. streakJustBumped/showPerfectBadge are the ephemeral "reward
    // moment" flags read by the celebration overlay - true only for the
    // ~500ms success window, then cleared by the next level's fresh state.
    var streakCount by remember { mutableStateOf(ProgressStore.currentStreak(context)) }
    var showPerfectBadge by remember(levelId) { mutableStateOf(false) }
    var streakMilestoneText by remember(levelId) { mutableStateOf<String?>(null) }

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
        wrongMoveCount = 0
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
            // Navigating to the next level (or back to Home after Daily
            // Challenge) pops this screen's nav backstack entry, and that
            // entry's own lifecycle fires ON_STOP the moment it's no longer
            // the topmost destination - even though the app itself never
            // left the foreground. Without the isComplete guard, that
            // false-positive ON_STOP set isPaused = true a split second
            // before this screen was disposed, flashing the Pause dialog
            // over the confetti during every level transition.
            if (event == Lifecycle.Event.ON_STOP && !isComplete) {
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
                if (playMode == null) ProgressStore.recordHintUsed(context, GameSession.difficulty)
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

    // The whole "you solved it" moment is now just this: record the result,
    // buzz once, let the confetti/flash play, then move on by itself - no
    // stars, no "Next level" button, nothing for the player to tap. Daily
    // Challenge has no "next level" to advance to, so it returns to Home
    // once the animation's had its moment instead.
    LaunchedEffect(isComplete) {
        if (isComplete) {
            completionSeconds = elapsedSeconds

            // Streak bookkeeping happens for every mode/level - a "perfect"
            // solve (no wrong touches, no hints) extends it, anything else
            // resets it to 0. Milestones (3, then every 5) get their own
            // callout text so the streak reads as a series of small wins
            // building up, not just a number that quietly changes.
            val perfect = wrongMoveCount == 0 && hintsUsed == 0
            val newStreak = ProgressStore.recordStreakResult(context, perfect)
            streakCount = newStreak
            showPerfectBadge = perfect
            streakMilestoneText = when {
                newStreak == 3 -> "3 in a row!"
                newStreak > 0 && newStreak % 5 == 0 -> "$newStreak in a row! \uD83D\uDD25"
                else -> null
            }

            if (isDailyChallenge) {
                DailyChallengeStore.recordCompletion(context, completionSeconds)
            } else if (sessionLevel != null) {
                ProgressStore.recordLevelReached(context, GameSession.difficulty, GameSession.levelNumber)
                ProgressStore.recordLevelCompletion(context, GameSession.difficulty)
                ProgressStore.recordSolveTime(context, GameSession.difficulty, completionSeconds)
                ProgressStore.recordSolveDuration(context, GameSession.difficulty, completionSeconds)
            }
            if (SettingsStore.vibrationEnabled(context)) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            if (SettingsStore.soundEnabled(context)) {
                soundPlayer.playSuccess()
            }
            // Fired on its own coroutine (not awaited here) so every tile's
            // shrink/pop-out plays *simultaneously* with the success sound
            // above and the confetti burst, rather than sequentially.
            coroutineScope.launch {
                tileExitProgress.animateTo(1f, animationSpec = tween(durationMillis = 420, easing = LinearEasing))
            }
            delay(if (streakMilestoneText != null) SUCCESS_ANIMATION_MILLIS + 500L else SUCCESS_ANIMATION_MILLIS)
            when {
                isDailyChallenge -> onBack()
                playMode != null -> onNavigateToLevel(ModeSession.next(context, playMode).id)
                else -> onNavigateToLevel(GameSession.next(context).id)
            }
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
    // does, so back never has to be handled twice. It's disabled during the
    // brief auto-advance window after a solve, so Pause can't pop up over
    // the confetti right before the next level loads itself.
    BackHandler(enabled = !isPaused && !isComplete) {
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
                // A real move landed - a fresh wrong touch after this one
                // should be able to shake/flash again.
                lastInvalidCandidate = null
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
            // Any other tap inside the grid is an invalid move: a cell
            // that isn't adjacent to the stroke's current end. Debounced
            // via lastInvalidCandidate so a finger resting on the same
            // wrong tile during a drag doesn't refire the shake/sound on
            // every pointer-move callback - only a *new* wrong tile does.
            else -> {
                if (candidate != lastInvalidCandidate) {
                    lastInvalidCandidate = candidate
                    shakeTrigger += 1
                    wrongMoveCount += 1
                    if (SettingsStore.vibrationEnabled(context)) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    if (SettingsStore.soundEnabled(context)) {
                        soundPlayer.playWrongMove()
                    }
                    coroutineScope.launch {
                        invalidFlash.snapTo(1f)
                        invalidFlash.animateTo(0f, animationSpec = tween(durationMillis = 400, easing = LinearEasing))
                    }
                }
            }
        }
    }

    // Perf: these gradients don't depend on anything that changes per
    // recomposition, but backgroundBrush()/cardSurfaceBrush() are plain
    // functions - called unremembered, they allocated a brand new Brush
    // object on every recomposition (which happens at least once a second
    // from the timer tick, and again on every tile connect). remember
    // builds each one exactly once for this screen's lifetime.
    val background = remember { backgroundBrush() }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background is its own layer (not just a Column modifier) so the
        // ambient particles below can sit on top of the gradient but
        // underneath the header/grid content, instead of a flat fill.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
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
            }
            if (streakCount > 0) {
                StreakChip(streak = streakCount)
            }
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
        Spacer(modifier = Modifier.height(6.dp))
        FillProgressBar(
            fraction = path.size.toFloat() / level.cellCount.toFloat(),
            accentBrush = accentBrush,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
        )

        // weight(1f) claims all the vertical space left over after the
        // header/stats row above, and contentAlignment centers the grid
        // inside it - so the puzzle sits perfectly centered both ways on
        // any screen height instead of top-aligning and leaving a dead gap
        // at the bottom. Replaces a fixed-height spacer that only
        // approximated centering on one specific screen size.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                val availableWidthPx = with(density) { maxWidth.toPx() }
                // Bug fix: this used to size cells from availableWidthPx
                // alone. That's fine for a square-ish grid, but the
                // generator crops every level to its own walk's bounding
                // box (see LevelGenerator.generateCore), so rows and cols
                // are frequently *not* equal - a long, narrow walk can crop
                // to something like 3 cols x 11 rows. Sizing purely off
                // width let a cell size that fit 3 columns perfectly blow
                // the *height* (11 rows at that size) far past the space
                // actually available, so the grid overflowed its box and
                // rendered as an oversized, cropped-looking "zoomed in"
                // mess - most visible on Zen/Timed since every level there
                // is procedurally generated (Classic's 3 handcrafted levels
                // are all a fixed 5x5, which happens to hide this). Now the
                // available height is measured too, and cellPx is capped by
                // whichever dimension (width or height) is tighter for
                // *this* level's actual shape.
                val availableHeightPx = with(density) { maxHeight.toPx() }
                val gapPx = with(density) { CELL_GAP.toPx() }
                val maxCellPx = with(density) { MAX_CELL_SIZE.toPx() }
                val minCellPx = with(density) { MIN_CELL_SIZE.toPx() }

                val cellPxForWidth = (availableWidthPx - gapPx * (level.cols - 1)) / level.cols
                val cellPxForHeight = (availableHeightPx - gapPx * (level.rows - 1)) / level.rows
                val cellPx = minOf(cellPxForWidth, cellPxForHeight)
                    .coerceAtMost(maxCellPx)
                    .coerceAtLeast(minCellPx)
                val stridePx = cellPx + gapPx

                val gridWidthDp = with(density) { (cellPx * level.cols + gapPx * (level.cols - 1)).toDp() }
                val gridHeightDp = with(density) { (cellPx * level.rows + gapPx * (level.rows - 1)).toDp() }

                // Built once per level/size, reused every frame - see
                // CellGeometry's doc comment for why this matters for perf.
                val cellGeometry = remember(level, cellPx) { buildCellGeometry(level, cellPx, stridePx) }

                Canvas(
                    modifier = Modifier
                        .width(gridWidthDp)
                        .height(gridHeightDp)
                        .shake(shakeTrigger)
                        .pointerInput(levelId) {
                            detectDragGestures(
                                onDragStart = { offset -> handleTouch(offset, cellPx, stridePx) },
                                onDrag = { change, _ -> handleTouch(change.position, cellPx, stridePx) },
                                onDragEnd = { lastInvalidCandidate = null },
                                onDragCancel = { lastInvalidCandidate = null }
                            )
                        }
                ) {
                    level.cells.forEach { cell ->
                        val geo = cellGeometry.getValue(cell)
                        val inPath = cell in path
                        if (inPath) {
                            // The tile that was *just* connected bounces in
                            // (spring overshoot on connectProgress carries
                            // it slightly past 1.0 before settling) instead
                            // of simply appearing filled.
                            val connectScale = if (cell == justConnectedCell) {
                                0.6f + connectProgress.value * 0.4f
                            } else {
                                1f
                            }
                            // Every filled tile shrinks toward 0 together
                            // once the level is solved - the "pop out"
                            // beat that plays alongside the success sound
                            // right before the next level loads.
                            val popScale = connectScale * (1f - tileExitProgress.value)
                            val tileCenter = Offset(geo.topLeft.x + cellPx / 2f, geo.topLeft.y + cellPx / 2f)
                            scale(scale = popScale, pivot = tileCenter) {
                                drawRoundRect(brush = accentBrush, topLeft = geo.topLeft, size = geo.size, cornerRadius = geo.corner)
                                drawPath(
                                    path = geo.filledBevelPath,
                                    brush = geo.filledBevelBrush,
                                    style = Stroke(width = cellPx * 0.045f)
                                )
                                // The "glow" micro-interaction: a bright
                                // white overlay flashes in at full
                                // strength the instant the tile connects
                                // (burstProgress = 0) and fades out over
                                // the same ~450ms as the bounce, on top of
                                // the scale-up/scale-down above - a quick
                                // brightness pop rather than a flat fill.
                                if (cell == justConnectedCell && burstProgress.value < 1f) {
                                    drawRoundRect(
                                        color = Color.White.copy(alpha = (1f - burstProgress.value) * 0.55f),
                                        topLeft = geo.topLeft,
                                        size = geo.size,
                                        cornerRadius = geo.corner
                                    )
                                }
                            }
                        } else {
                            drawRoundRect(color = TileIdle, topLeft = geo.topLeft, size = geo.size, cornerRadius = geo.corner)
                            drawPath(
                                path = geo.idleBevelPath,
                                brush = geo.idleBevelBrush,
                                style = Stroke(width = cellPx * 0.035f)
                            )
                        }
                        if (cell == hintCell) {
                            drawRoundRect(
                                color = Gold,
                                topLeft = geo.topLeft,
                                size = geo.size,
                                cornerRadius = geo.corner,
                                style = Stroke(width = cellPx * 0.08f)
                            )
                        }
                    }

                    // Blends the stroke's three tones toward red while
                    // invalidFlash is > 0, then eases back to the normal
                    // accent as it decays to 0 - the "line instantly turns
                    // red" feedback for a wrong move.
                    val invalidColor = Color(0xFFE0453B)
                    val strokeAccent = lerpColor(accent, invalidColor, invalidFlash.value)
                    val strokeAccentDeep = lerpColor(accentDeep, invalidColor.copy(alpha = 0.85f), invalidFlash.value)
                    val strokeAccentHighlight = lerpColor(accentHighlight, Color.White, invalidFlash.value * 0.3f)

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
                                color = strokeAccent.copy(alpha = 0.20f + 0.14f * glowPulse),
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
                            // All three blend toward red on an invalid move.
                            drawLine(
                                color = strokeAccentDeep,
                                start = centerA,
                                end = drawnEnd,
                                strokeWidth = cellPx * 0.18f,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = strokeAccent,
                                start = centerA,
                                end = drawnEnd,
                                strokeWidth = cellPx * 0.14f,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = strokeAccentHighlight.copy(alpha = 0.6f),
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
                    CompletionCallout(
                        showPerfect = showPerfectBadge,
                        milestoneText = streakMilestoneText,
                        accent = accent,
                        modifier = Modifier.width(gridWidthDp).height(gridHeightDp)
                    )
                }
            }
        }

        // Hint / Restart now live down here instead of as small header
        // icons - this is the space that used to sit empty below the
        // centered grid on most screen heights. Full-width, equal-weight
        // MetallicButtons read as deliberate primary actions rather than
        // an afterthought tucked into the top bar.
        //
        // The app draws edge-to-edge (see MainActivity's enableEdgeToEdge),
        // so without navigationBarsPadding() here this row sat flush
        // against - or partly behind - the 3-button/gesture nav bar on
        // real devices, even though it looked fine in a preview with no
        // system bars. navigationBarsPadding() adds exactly however much
        // inset that specific device needs; the 20dp below it is the
        // row's own visual breathing room on top of that.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 20.dp)
        ) {
            val hintsLeft = MAX_HINTS_PER_LEVEL - hintsUsed
            MetallicButton(
                text = if (isSolvingHint) "Solving\u2026" else "Hint \u00b7 $hintsLeft left",
                onClick = { requestHint() },
                accentKey = "gold",
                enabled = !isComplete && !isSolvingHint && hintsLeft > 0,
                textColor = Color(0xFFFDFDFD),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(14.dp))
            MetallicButton(
                text = "Restart",
                onClick = {
                    resetAttempt()
                    if (!isDailyChallenge) persistProgress()
                    if (SettingsStore.vibrationEnabled(context)) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    if (SettingsStore.soundEnabled(context)) {
                        soundPlayer.playReset()
                    }
                },
                accentKey = "copper",
                enabled = !isComplete,
                // Restart plays its own distinct paper-rip cue above
                // instead of the generic shared button tap.
                playTapSound = false,
                textColor = Color(0xFFFDFDFD),
                modifier = Modifier.weight(1f)
            )
        }
        }

        AnimatedVisibility(
            visible = showNeedHelp,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
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
    val cardBrush = remember { cardSurfaceBrush() }
    val goldBrushRemembered = remember { goldBrush() }
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
            .background(cardBrush)
            .border(width = 1.dp, color = Gold.copy(alpha = 0.3f), shape = LoopLineShapes.card)
            .clickable(onClick = onUseHint)
            .padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(goldBrushRemembered),
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
        List(44) {
            ConfettiParticle(
                angle = Random.nextFloat() * 2f * Math.PI.toFloat(),
                distance = 60f + Random.nextFloat() * 130f,
                color = palette.random(),
                isDiamond = Random.nextBoolean(),
                sizeScale = 0.75f + Random.nextFloat() * 0.7f
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

/**
 * A thin, accent-filled bar tracking `path.size / cellCount`. Animates with
 * a spring rather than a linear tween so each tile connected gives the bar
 * a tiny satisfying overshoot-then-settle instead of a robotic fill - the
 * same "juice" language as the tile bounce, just applied to progress as a
 * whole rather than one cell at a time.
 */
@Composable
private fun FillProgressBar(
    fraction: Float,
    accentBrush: Brush,
    modifier: Modifier = Modifier
) {
    val animatedFraction by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "fillProgress"
    )
    Box(
        modifier = modifier
            .height(6.dp)
            .clip(LoopLineShapes.chip)
            .background(Color.White.copy(alpha = 0.08f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedFraction)
                .clip(LoopLineShapes.chip)
                .background(accentBrush)
        )
    }
}

/**
 * The text half of the completion moment, layered over the grid alongside
 * [ConfettiBurst]: a bouncy "Perfect!" stamp when the attempt had zero
 * wrong touches and zero hints, and/or a streak-milestone line underneath
 * it. Either, both, or neither can show depending on how the level went -
 * a merely-completed level (imperfect, no milestone) still gets its
 * confetti but no text, so this doesn't cheapen itself by firing on every
 * single solve.
 */
@Composable
private fun CompletionCallout(
    showPerfect: Boolean,
    milestoneText: String?,
    accent: Color,
    modifier: Modifier = Modifier
) {
    if (!showPerfect && milestoneText == null) return
    val pop = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        pop.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.scale(pop.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showPerfect) {
                Text(
                    text = "PERFECT",
                    style = MaterialTheme.typography.headlineMedium,
                    color = accent
                )
            }
            if (milestoneText != null) {
                Text(
                    text = milestoneText,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }
        }
    }
}

/**
 * Continue resumes exactly where the player left off (the timer never
 * moved while this was up); Go to Home exits to mode selection. Tapping
 * the scrim or pressing system back both count as "Continue" via
 * onDismissRequest, since a real Dialog window intercepts back itself -
 * the BackHandler in GameScreen only needs to handle the *unpaused* case.
 *
 * Redesigned as a full-screen overlay instead of a boxed AlertDialog: a
 * fully opaque scrim over the whole screen (so the paused grid is
 * completely hidden, not just dimmed behind a small card) with a
 * translucent, bevel-edged "glass" panel floating in the center, and both
 * actions rendered as real MetallicButtons in the gold/bronze accents
 * instead of one metallic button next to a plain text link - matching the
 * rest of the app's premium metal-on-dark language instead of standing out
 * as a generic system dialog. `usePlatformDefaultWidth = false` is what
 * lets the Dialog's content claim the full screen instead of Android's
 * default dialog-sized box.
 *
 * Note: this fakes "glass" via a translucent gradient + a soft bevel edge
 * rather than a true gaussian blur of the grid behind it - genuine
 * backdrop blur needs either minSdk 31's RenderEffect or a small blur
 * library (e.g. Haze), neither of which this project currently pulls in.
 */
@Composable
private fun PauseMenuDialog(
    onContinue: () -> Unit,
    onGoHome: () -> Unit
) {
    Dialog(
        onDismissRequest = onContinue,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Fully opaque - the paused grid underneath must not be
                // readable at all, not just dimmed. Uses the app's own
                // near-black background tone (not flat Color.Black) so it
                // reads as "this screen" rather than a generic system
                // scrim, while still completely hiding the grid.
                .background(Color(0xFF0B0805))
                // Tapping anywhere on the scrim (outside the panel)
                // behaves like tapping outside an AlertDialog used to -
                // it resumes play. No ripple, since this is a full-bleed
                // scrim rather than a discrete tappable element.
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onContinue
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 340.dp)
                    .fillMaxWidth(0.86f)
                    // Swallow taps on the panel itself so they don't fall
                    // through to the scrim's onContinue behind it.
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .shadow(
                        elevation = 28.dp,
                        shape = LoopLineShapes.dialog,
                        ambientColor = Gold.copy(alpha = 0.25f),
                        spotColor = Gold.copy(alpha = 0.3f)
                    )
                    .clip(LoopLineShapes.dialog)
                    // The "glass": a mostly-transparent light-to-dark
                    // gradient over the dark scrim reads as a frosted pane
                    // even without a real blur behind it.
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.10f),
                                SurfaceCardElevated.copy(alpha = 0.78f)
                            )
                        )
                    )
                    .metallicBevel(
                        cornerDp = LoopLineShapes.dialogCornerDp,
                        highlight = Color.White.copy(alpha = 0.5f),
                        shadow = Color.Black.copy(alpha = 0.4f),
                        strokeWidthDp = 1.5.dp
                    )
                    .padding(horizontal = 28.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GradientText(
                    text = "Paused",
                    brush = goldBrush(),
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "The clock's stopped \u2014 take your time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(28.dp))
                MetallicButton(
                    text = "Continue",
                    onClick = onContinue,
                    accentKey = "gold",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                MetallicButton(
                    text = "Go to Home",
                    onClick = onGoHome,
                    accentKey = "copper",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
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
