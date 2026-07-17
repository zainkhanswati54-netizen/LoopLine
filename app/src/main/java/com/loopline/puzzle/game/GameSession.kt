package com.loopline.puzzle.game

/**
 * Holds the state for one "endless" play session: which difficulty the
 * player picked, how many levels they've generated so far, and the actual
 * generated Level objects (keyed by id, since Compose Navigation route
 * arguments only carry simple types like Int, not full objects).
 */
object GameSession {
    private var idCounter = 0
    private val cache = mutableMapOf<Int, Level>()
    private val accentCycle = listOf("blue", "orange", "green")

    var difficulty: Difficulty = Difficulty.NORMAL
        private set

    var levelNumber: Int = 0
        private set

    var currentLevel: Level? = null
        private set

    val hasActiveSession: Boolean get() = currentLevel != null

    /**
     * Picking the difficulty the player is already mid-session on resumes
     * their current puzzle instead of throwing progress away. Picking a
     * different difficulty (or having no session yet) starts fresh.
     */
    fun start(newDifficulty: Difficulty): Level {
        val existing = currentLevel
        if (newDifficulty == difficulty && existing != null) {
            return existing
        }
        return forceRestart(newDifficulty)
    }

    /** Always starts a brand new session at level 1, even if one was active. */
    fun forceRestart(newDifficulty: Difficulty): Level {
        difficulty = newDifficulty
        levelNumber = 1
        return generateAndStore()
    }

    fun next(): Level {
        levelNumber += 1
        return generateAndStore()
    }

    fun lookup(id: Int): Level? = cache[id]

    private fun generateAndStore(): Level {
        val config = difficulty.scaledConfig(levelNumber)
        val accentKey = accentCycle[(levelNumber - 1).mod(accentCycle.size)]
        val generated = LevelGenerator.generate(config, levelNumber, accentKey)
        idCounter += 1
        val stored = generated.copy(id = idCounter)
        cache[stored.id] = stored
        currentLevel = stored
        return stored
    }
}
