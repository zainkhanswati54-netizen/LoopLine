package com.loopline.puzzle.game

import android.content.Context

enum class PlayMode(val storeKey: String) {
    ZEN("zen"),
    TIMED("timed")
}

/**
 * Endless progress for Zen and Timed. Structurally a sibling of
 * [GameSession] (same hydrate/resume/restart/next/saveProgress shape,
 * mirrored so both persist and resume the same way after an app restart),
 * but simpler: neither mode exposes a difficulty picker, so there's just
 * one track per mode instead of one per difficulty. Both use
 * [Difficulty.NORMAL]'s grid-size scaling as their baseline.
 *
 * Ids handed out here live in their own negative range (well below
 * [DAILY_CHALLENGE_LEVEL_ID]) so they can never collide with GameSession's
 * positive ids or the Daily Challenge's reserved id.
 */
object ModeSession {
    private var idCounter = -1000
    private val cache = mutableMapOf<Int, Level>()
    private val modeForId = mutableMapOf<Int, PlayMode>()
    private val accentCycle = listOf("rosegold", "gold", "copper")

    private class Session(var levelNumber: Int, var currentLevel: Level)
    private val sessions = mutableMapOf<PlayMode, Session>()
    private var hydrated = false

    data class RestoredProgress(val forLevelId: Int, val path: List<Cell>, val elapsedSeconds: Int, val hintsUsed: Int)
    private var pendingRestoredProgress: RestoredProgress? = null

    fun consumeRestoredProgress(forLevelId: Int): RestoredProgress? {
        val progress = pendingRestoredProgress
        pendingRestoredProgress = null
        return progress?.takeIf { it.forLevelId == forLevelId }
    }

    fun modeFor(levelId: Int): PlayMode? = modeForId[levelId]

    fun hydrate(context: Context) {
        if (hydrated) return
        hydrated = true
        for (mode in PlayMode.entries) {
            val saved = ProgressStore.loadSessionByKey(context, mode.storeKey) ?: continue
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
            modeForId[level.id] = mode
            sessions[mode] = Session(saved.levelNumber, level)
        }
    }

    fun hasSession(mode: PlayMode): Boolean = sessions[mode] != null
    fun levelNumberFor(mode: PlayMode): Int = sessions[mode]?.levelNumber ?: 0

    fun resume(context: Context, mode: PlayMode): Level {
        hydrate(context)
        val session = sessions[mode]
        if (session != null) {
            val saved = ProgressStore.loadSessionByKey(context, mode.storeKey)
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
        return restart(context, mode)
    }

    fun restart(context: Context, mode: PlayMode): Level {
        val level = generateAndStore(mode, 1)
        sessions[mode] = Session(1, level)
        persistFreshLevel(context, mode, 1, level)
        return level
    }

    fun next(context: Context, mode: PlayMode): Level {
        val session = sessions[mode] ?: return restart(context, mode)
        val newLevelNumber = session.levelNumber + 1
        val level = generateAndStore(mode, newLevelNumber)
        session.levelNumber = newLevelNumber
        session.currentLevel = level
        persistFreshLevel(context, mode, newLevelNumber, level)
        return level
    }

    fun saveProgress(context: Context, mode: PlayMode, path: List<Cell>, elapsedSeconds: Int, hintsUsed: Int) {
        val level = sessions[mode]?.currentLevel ?: return
        ProgressStore.saveSessionByKey(
            context,
            mode.storeKey,
            ProgressStore.SavedSession(
                levelNumber = levelNumberFor(mode),
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

    /** See [GameSession.resetInMemory] - same bug, same fix, for Zen/Timed's
     * sessions. */
    fun resetInMemory() {
        sessions.clear()
        cache.clear()
        modeForId.clear()
        hydrated = false
        pendingRestoredProgress = null
    }

    private fun persistFreshLevel(context: Context, mode: PlayMode, levelNumber: Int, level: Level) {
        ProgressStore.saveSessionByKey(
            context,
            mode.storeKey,
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
        idCounter -= 1
        return idCounter
    }

    private fun generateAndStore(mode: PlayMode, levelNumber: Int): Level {
        val accentKey = accentCycle[(levelNumber - 1).mod(accentCycle.size)]
        val generated = LevelGenerator.generate(Difficulty.NORMAL, levelNumber, accentKey)
        val id = nextId()
        val stored = generated.copy(id = id)
        cache[id] = stored
        modeForId[id] = mode
        return stored
    }
}
