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

    var difficulty: Difficulty = Difficulty.NORMAL
        private set

    var levelNumber: Int = 0
        private set

    fun start(newDifficulty: Difficulty): Level {
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
        val generated = LevelGenerator.generate(difficulty, levelNumber)
        idCounter += 1
        val stored = generated.copy(id = idCounter)
        cache[stored.id] = stored
        return stored
    }
}
