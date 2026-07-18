package com.loopline.puzzle.game

/**
 * Holds the state for "endless" play — but crucially, one independent
 * session **per difficulty**, not a single global one.
 *
 * Bug this fixes: the previous version of this object stored only one
 * `difficulty` / `levelNumber` / `currentLevel` at a time. Picking Hard while
 * you had progress on Easy silently overwrote that progress, because there
 * was nowhere else for it to live — so coming back to Easy afterwards looked
 * "reset" to Level 1, even though `ProgressStore`'s best-level record (a
 * separate, always-per-difficulty store) still correctly remembered you'd
 * reached Level 4. Now each [Difficulty] keeps its own [Session], so
 * switching between them is non-destructive in both directions.
 */
object GameSession {
    private var idCounter = 0
    private val cache = mutableMapOf<Int, Level>()
    private val accentCycle = listOf("gold", "copper", "rosegold")

    private class Session(var levelNumber: Int, var currentLevel: Level)
    private val sessions = mutableMapOf<Difficulty, Session>()

    /** Whichever difficulty's puzzle is currently active/on screen. */
    var difficulty: Difficulty = Difficulty.NORMAL
        private set

    val levelNumber: Int get() = sessions[difficulty]?.levelNumber ?: 0
    val currentLevel: Level? get() = sessions[difficulty]?.currentLevel
    val hasActiveSession: Boolean get() = currentLevel != null

    /** True if [target] has an in-progress session waiting to be resumed. */
    fun hasSession(target: Difficulty): Boolean = sessions[target]?.currentLevel != null

    /** The level number the player would resume at for [target] (0 = none yet). */
    fun levelNumberFor(target: Difficulty): Int = sessions[target]?.levelNumber ?: 0

    /**
     * Switches the active difficulty to [target]. Resumes its existing
     * session if it has one (this is what makes switching difficulties
     * safe); otherwise starts a fresh Level 1 session, same as [restart].
     */
    fun resume(target: Difficulty): Level {
        difficulty = target
        val existing = sessions[target]?.currentLevel
        if (existing != null) return existing
        return restart(target)
    }

    /**
     * Explicitly throws away any in-progress session on [target] and starts
     * over at Level 1. This is a deliberate, separate action (a "restart"
     * button the player has to confirm) rather than something that happens
     * as a side effect of merely switching difficulties.
     */
    fun restart(target: Difficulty): Level {
        difficulty = target
        val level = generateAndStore(target, 1)
        sessions[target] = Session(1, level)
        return level
    }

    fun next(): Level {
        val session = sessions[difficulty] ?: return restart(difficulty)
        val newLevelNumber = session.levelNumber + 1
        val level = generateAndStore(difficulty, newLevelNumber)
        session.levelNumber = newLevelNumber
        session.currentLevel = level
        return level
    }

    fun lookup(id: Int): Level? = cache[id]

    private fun generateAndStore(target: Difficulty, levelNumber: Int): Level {
        val accentKey = accentCycle[(levelNumber - 1).mod(accentCycle.size)]
        val generated = LevelGenerator.generate(target, levelNumber, accentKey)
        idCounter += 1
        val stored = generated.copy(id = idCounter)
        cache[stored.id] = stored
        return stored
    }
}
