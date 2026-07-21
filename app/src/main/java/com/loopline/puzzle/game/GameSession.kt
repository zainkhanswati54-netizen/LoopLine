package com.loopline.puzzle.game

import android.content.Context

/**
 * Holds the state for Classic mode's "endless" play — **one single global
 * track**, not one per difficulty. The puzzle's tier (Easy/Normal/Hard)
 * is derived automatically from how far the player has climbed
 * ([Difficulty.forLevel]: 1-40 Easy, 41-70 Normal, 71+ Hard, no ceiling) -
 * there's no up-front picker and no separate progress per tier, just one
 * level counter that keeps climbing.
 *
 * State is mirrored to disk via [ProgressStore] so it survives an actual
 * app restart, not just in-app navigation.
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

    /** Single fixed key - Classic mode is one track, so there's only ever
     * one session to persist (unlike Zen/Timed's per-mode keys). */
    private const val SESSION_KEY = "classic"

    private class Session(var levelNumber: Int, var currentLevel: Level)
    private var session: Session? = null

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

    /** The tier the *current* level belongs to, derived from [levelNumber]
     * (falls back to Easy - level 1's tier - when there's no active
     * session yet, since that's where a fresh session would start). */
    val difficulty: Difficulty get() = Difficulty.forLevel(levelNumber.coerceAtLeast(1))

    val levelNumber: Int get() = session?.levelNumber ?: 0
    val currentLevel: Level? get() = session?.currentLevel
    val hasActiveSession: Boolean get() = currentLevel != null

    /**
     * Loads any persisted in-progress session from disk into memory. Safe
     * to call repeatedly (e.g. from every screen that reads session state);
     * only does real work once per process. Called once, early, from
     * MainActivity so `hasActiveSession`/`levelNumber` are accurate the
     * moment Home first reads them - without needing a Context threaded
     * through every read.
     */
    fun hydrate(context: Context) {
        if (hydrated) return
        hydrated = true
        val saved = ProgressStore.loadSessionByKey(context, SESSION_KEY) ?: return
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
        session = Session(saved.levelNumber, level)
    }

    /**
     * Resumes the in-progress session if there is one (this is what makes
     * resuming after a full app restart work); otherwise starts a fresh
     * Level 1 session, same as [restart].
     */
    fun resume(context: Context): Level {
        hydrate(context)
        val current = session
        if (current != null) {
            val saved = ProgressStore.loadSessionByKey(context, SESSION_KEY)
            if (saved != null) {
                pendingRestoredProgress = RestoredProgress(
                    forLevelId = current.currentLevel.id,
                    path = saved.path,
                    elapsedSeconds = saved.elapsedSeconds,
                    hintsUsed = saved.hintsUsed
                )
            }
            return current.currentLevel
        }
        return restart(context)
    }

    /**
     * Explicitly throws away any in-progress session and starts over at
     * Level 1 (back to the Easy tier). This is a deliberate, separate
     * action (a "restart" button the player has to confirm) rather than
     * something that happens as a side effect of anything else.
     */
    fun restart(context: Context): Level {
        val level = generateAndStore(1)
        session = Session(1, level)
        persistFreshLevel(context, 1, level)
        return level
    }

    fun next(context: Context): Level {
        val current = session ?: return restart(context)
        val newLevelNumber = current.levelNumber + 1
        val level = generateAndStore(newLevelNumber)
        current.levelNumber = newLevelNumber
        current.currentLevel = level
        persistFreshLevel(context, newLevelNumber, level)
        return level
    }

    /**
     * Called from GameScreen every time the stroke changes (a tile connects)
     * and whenever the app backgrounds, so a kill at any moment loses at
     * most the last few tiles' worth of progress instead of the whole level.
     */
    fun saveProgress(context: Context, path: List<Cell>, elapsedSeconds: Int, hintsUsed: Int) {
        val level = currentLevel ?: return
        ProgressStore.saveSessionByKey(
            context,
            SESSION_KEY,
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

    /**
     * Clears all in-memory state (the cached session, the level cache, the
     * hydration flag). Bug this fixes: this object is a process-lifetime
     * singleton, so Settings' "Reset everything" - which only clears
     * SharedPreferences - used to leave the *in-memory* session untouched.
     * The player would tap Reset, then Play, and land right back on
     * whatever level they were on before, because [hydrate] had already
     * run once and [resume] just handed back the still-cached [Session]
     * without ever consulting disk again. The reset only "worked" after a
     * full app kill, which looked like the button did nothing. Called from
     * [SettingsStore.resetAllProgress] right after it wipes the prefs, so
     * the very next [resume] finds nothing cached and starts clean at
     * Level 1.
     */
    fun resetInMemory() {
        session = null
        cache.clear()
        hydrated = false
        pendingRestoredProgress = null
    }

    /** Caches a level built outside the normal Classic-mode flow (the
     * Daily Challenge) under its own id so GameScreen can look it up the
     * same way as any endless-mode level. */
    fun cacheExternal(level: Level) {
        cache[level.id] = level
    }

    private fun persistFreshLevel(context: Context, levelNumber: Int, level: Level) {
        ProgressStore.saveSessionByKey(
            context,
            SESSION_KEY,
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

    /** [levelNumber] is the global Classic-track number; the tier (and the
     * curve math inside [LevelGenerator.generate]) is derived from it via
     * [Difficulty.forLevel], and the level passed to the generator is
     * re-based to "levels since this tier began" via [Difficulty.levelWithinTier]
     * so each tier's curve (authored assuming it starts at 1) still ramps
     * the same way it did when tiers were separate picker tracks. */
    private fun generateAndStore(levelNumber: Int): Level {
        val tier = Difficulty.forLevel(levelNumber)
        val levelInTier = tier.levelWithinTier(levelNumber)
        val accentKey = accentCycle[(levelNumber - 1).mod(accentCycle.size)]
        val generated = LevelGenerator.generate(tier, levelInTier, accentKey)
        val stored = generated.copy(id = nextId(), title = "Level $levelNumber")
        cache[stored.id] = stored
        return stored
    }
}
