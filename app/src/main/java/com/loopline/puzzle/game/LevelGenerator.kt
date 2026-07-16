package com.loopline.puzzle.game

import kotlin.random.Random

object LevelGenerator {

    private val accentCycle = listOf("blue", "orange", "green")
    private val directions = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)

    /**
     * Generates a level for the given difficulty. The returned level's [Level.id]
     * is a placeholder (0) - callers (GameSession) assign the real id.
     *
     * How solvability is guaranteed: instead of drawing a shape first and then
     * checking whether it *has* a solution, we generate the solution path first
     * (a random self-avoiding walk on the grid) and the walk's cells *become*
     * the level's shape. There's no separate validation step needed - the walk
     * that produced the shape is itself a valid one-stroke answer.
     */
    fun generate(difficulty: Difficulty, levelNumber: Int, attempts: Int = 300): Level {
        val config = difficulty.config
        val rnd = Random(System.nanoTime() xor levelNumber.toLong())

        var best: List<Cell> = emptyList()
        for (i in 0 until attempts) {
            val path = randomSelfAvoidingWalk(config.rows, config.cols, rnd)
            if (path.size in config.minCells..config.maxCells) {
                best = path
                break
            }
            if (path.size > best.size) best = path
        }

        // Safety net: if 300 attempts never landed in range (rare on small grids),
        // fall back to whatever the longest walk found was, as long as it's not trivial.
        val finalPath = if (best.size >= 6) best else randomSelfAvoidingWalk(config.rows, config.cols, rnd)

        return Level(
            id = 0,
            title = "Level $levelNumber",
            rows = config.rows,
            cols = config.cols,
            cells = finalPath.toSet(),
            start = finalPath.first(),
            accentKey = accentCycle[(levelNumber - 1).mod(accentCycle.size)]
        )
    }

    private fun randomSelfAvoidingWalk(rows: Int, cols: Int, rnd: Random): List<Cell> {
        val start = Cell(rnd.nextInt(rows), rnd.nextInt(cols))
        val visited = mutableSetOf(start)
        val path = mutableListOf(start)

        while (true) {
            val current = path.last()
            val options = directions
                .map { (dr, dc) -> Cell(current.row + dr, current.col + dc) }
                .filter { it.row in 0 until rows && it.col in 0 until cols && it !in visited }
            if (options.isEmpty()) break
            val next = options.random(rnd)
            visited.add(next)
            path.add(next)
        }
        return path
    }
}
