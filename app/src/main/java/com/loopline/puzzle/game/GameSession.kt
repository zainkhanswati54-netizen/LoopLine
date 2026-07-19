package com.loopline.puzzle.game

import android.content.Context

/**
 * Holds the state for "endless" play — one independent session **per
 * difficulty**, not a single global one — and keeps it mirrored to disk via
 * [ProgressStore] so it survives an actual app restart, not just in-app
 * navigation.
 *
 * Bug this fixes: previously this was pure in-memory state. Force-closing
 * the app (or Android killing the process in the background) lost not just
 * the current stroke but which level the player was even on - "Continue"
 * would silently vanish and the player was back to Level 1 next time,
 * despite `ProgressStore`'s separate best-level record still remembering
 * they'd gone further. Every change here (a new tile connecting, a new
 * level starting) is now flushed to [ProgressStore] straight away, and
 * [hydrate] rebuilds this object's state from disk the first time it's
 * touched in a fresh process.
 */
object GameSession {
    private var idCounter = 0
    private val cache = mutableMapOf<Int, Level>()
    private val accentCycle = listOf("gold", "copper", "rosegold")

    private class Session(var levelNumber: Int, var currentLevel: Level)
    private val sessions = mutableMapOf<Difficulty, Session>()

    private var hydrated = false

    /** Path/elapsed/hints restored from disk for whichever level [resume]
     * or [hydrate] most recently reconstructed - consumed once by
     * GameScreen so a later fresh level (via [next] or [restart]) doesn't
     * accidentally reapply stale progress. */
    data class RestoredProgress(val forLevelId: Int, val path: List<Cell>, val elapsedSeconds: Int, val hintsUsed: Int)
    private var pendingRestoredProgress: RestoredProgress? = null

    fun consumeRestoredProgress(forLevelId: Int): RestoredProgress? {
        val progress = pendingRestoredProgress
        pendingRestoredProgress = null
        return progress?.takeIf { it.forLevelId == forLevelId }
    }

    /** Whichever difficulty's puzzle is currently active/on screen. */
    var difficulty: Difficulty = Difficulty.NORMAL
        private set

    val levelNumber: Int get() = sessions[difficulty]?.levelNumber ?: 0
    val currentLevel: Level? get() = sessions[difficulty]?.currentLevel
    val hasActiveSession: Boolean get() = currentLevel != null

    /**
     * Loads any persisted in-progress sessions from disk into memory. Safe
     * to call repeatedly (e.g. from every screen that reads session state);
     * only does real work once per process. Called once, early, from
     * MainActivity so `hasSession`/`levelNumberFor` are accurate the moment
     * the Difficulty Select screen first reads them - without needing a
     * Context threaded through every read.
     */
    fun hydrate(context: Context) {
        if (hydrated) return
        hydrated = true
        for (target in Difficulty.entries) {
            val saved = ProgressStore.loadSession(context, target) ?: continue
            val level = Level(
                id = nextId(),
                title = "Level ${saved.levelNumber}",
                rows = saved.rows,
                cols = saved.cols,
                cells = saved.cells,
                start = saved.start,
                accentKey = saved.accentKey
            )
            cache[level.id] = level
            sessions[target] = Session(saved.levelNumber, level)
        }
    }

    /** True if [target] has an in-progress session waiting to be resumed. */
    fun hasSession(target: Difficulty): Boolean = sessions[target]?.currentLevel != null

    /** The level number the player would resume at for [target] (0 = none yet). */
    fun levelNumberFor(target: Difficulty): Int = sessions[target]?.levelNumber ?: 0

    /**
     * Switches the active difficulty to [target]. Resumes its existing
     * session if it has one (this is what makes switching difficulties
     * safe, and - since [hydrate] already ran - what makes resuming after
     * a full app restart work too); otherwise starts a fresh Level 1
     * session, same as [restart].
     */
    fun resume(context: Context, target: Difficulty): Level {
        hydrate(context)
        difficulty = target
        val session = sessions[target]
        if (session != null) {
            val saved = ProgressStore.loadSession(context, target)
            if (saved != null) {
                pendingRestoredProgress = RestoredProgress(
                    forLevelId = session.currentLevel.id,
                    path = saved.path,
                    elapsedSeconds = saved.elapsedSeconds,
                    hintsUsed = saved.hintsUsed
                )
            }
            return session.currentLevel
        }
        return restart(context, target)
    }

    /**
     * Explicitly throws away any in-progress session on [target] and starts
     * over at Level 1. This is a deliberate, separate action (a "restart"
     * button the player has to confirm) rather than something that happens
     * as a side effect of merely switching difficulties.
     */
    fun restart(context: Context, target: Difficulty): Level {
        difficulty = target
        val level = generateAndStore(target, 1)
        sessions[target] = Session(1, level)
        persistFreshLevel(context, target, 1, level)
        return level
    }

    fun next(context: Context): Level {
        val session = sessions[difficulty] ?: return restart(context, difficulty)
        val newLevelNumber = session.levelNumber + 1
        val level = generateAndStore(difficulty, newLevelNumber)
        session.levelNumber = newLevelNumber
        session.currentLevel = level
        persistFreshLevel(context, difficulty, newLevelNumber, level)
        return level
    }

    /**
     * Called from GameScreen every time the stroke changes (a tile connects)
     * and whenever the app backgrounds, so a kill at any moment loses at
     * most the last few tiles' worth of progress instead of the whole level.
     */
    fun saveProgress(context: Context, path: List<Cell>, elapsedSeconds: Int, hintsUsed: Int) {
        val level = currentLevel ?: return
        ProgressStore.saveSession(
            context,
            difficulty,
            ProgressStore.SavedSession(
                levelNumber = levelNumber,
                rows = level.rows,
                cols = level.cols,
                cells = level.cells,
                start = level.start,
                accentKey = level.accentKey,
                path = path,
                elapsedSeconds = elapsedSeconds,
                hintsUsed = hintsUsed
            )
        )
    }

    fun lookup(id: Int): Level? = cache[id]

    /** Caches a level built outside the normal per-difficulty flow (the
     * Daily Challenge) under its own id so GameScreen can look it up the
     * same way as any endless-mode level. */
    fun cacheExternal(level: Level) {
        cache[level.id] = level
    }

    private fun persistFreshLevel(context: Context, target: Difficulty, levelNumber: Int, level: Level) {
        ProgressStore.saveSession(
            context,
            target,
            ProgressStore.SavedSession(
                levelNumber = levelNumber,
                rows = level.rows,
                cols = level.cols,
                cells = level.cells,
                start = level.start,
                accentKey = level.accentKey,
                path = listOf(level.start),
                elapsedSeconds = 0,
                hintsUsed = 0
            )
        )
    }

    private fun nextId(): Int {
        idCounter += 1
        return idCounter
    }

    private fun generateAndStore(target: Difficulty, levelNumber: Int): Level {
        val accentKey = accentCycle[(levelNumber - 1).mod(accentCycle.size)]
        val generated = LevelGenerator.generate(target, levelNumber, accentKey)
        val stored = generated.copy(id = nextId())
        cache[stored.id] = stored
        return stored
    }
}
