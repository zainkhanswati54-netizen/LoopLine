package com.loopline.puzzle.game

import kotlin.random.Random

object LevelGenerator {

    private val directions = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)

    /**
     * Generates a level for [difficulty] at [levelNumber] - grid size and
     * target cell count both come from [Difficulty.scaledConfig] /
     * [Difficulty.targetCellCount], so the same call always knows exactly
     * how big and how full this specific level should be.
     * The returned level's [Level.id] is a placeholder (0) - GameSession
     * assigns the real id.
     *
     * How solvability is guaranteed: instead of drawing a shape first and then
     * checking whether it *has* a solution, we generate the solution path first
     * (a random self-avoiding walk on the grid) and the walk's cells *become*
     * the level's shape. There's no separate validation step needed - the walk
     * that produced the shape is itself a valid one-stroke answer.
     */
    fun generate(difficulty: Difficulty, levelNumber: Int, accentKey: String, attempts: Int = 300): Level {
        val config = difficulty.scaledConfig(levelNumber)
        val rnd = Random(System.nanoTime() xor levelNumber.toLong())
        val target = difficulty.targetCellCount(levelNumber)
        return generateCore(config, rnd, target, "Level $levelNumber", accentKey, attempts)
    }

    /**
     * The Daily Challenge's puzzle: deterministic from [seed] (the calendar
     * date, as an epoch-day long) so every player gets the exact same
     * layout on a given day, and it stays identical if they revisit it
     * later that same day. Uses Normal's grid size, fixed (not scaled by a
     * level number, since there's no level progression here - just today's
     * one puzzle).
     */
    fun generateDaily(seed: Long, accentKey: String = "gold", attempts: Int = 300): Level {
        val config = Difficulty.NORMAL.config
        val rnd = Random(seed)
        val target = (config.minCells + config.maxCells) / 2
        return generateCore(config, rnd, target, "Daily Challenge", accentKey, attempts)
    }

    private fun generateCore(
        config: GridConfig,
        rnd: Random,
        target: Int,
        title: String,
        accentKey: String,
        attempts: Int
    ): Level {
        var best: List<Cell> = emptyList()
        var bestDistance = Int.MAX_VALUE
        for (i in 0 until attempts) {
            val path = randomSelfAvoidingWalk(config.rows, config.cols, rnd)
            if (path.size !in config.minCells..config.maxCells) {
                if (path.size > best.size && bestDistance == Int.MAX_VALUE) best = path
                continue
            }
            val distance = kotlin.math.abs(path.size - target)
            if (distance < bestDistance) {
                best = path
                bestDistance = distance
                if (distance == 0) break
            }
        }

        // Safety net: if attempts never landed in range (rare), fall back to
        // whatever the longest walk found was, as long as it's not trivial.
        val finalPath = if (best.size >= 6) best else randomSelfAvoidingWalk(config.rows, config.cols, rnd)

        // Crop to the walk's own bounding box instead of leaving it placed
        // inside the full config.rows x config.cols canvas. A walk rarely
        // touches every edge of that canvas, so rendering it at the full
        // size left dead, unused rows/columns hugging one side of the grid
        // (looked like a lopsided "gap" next to the puzzle instead of the
        // puzzle sitting centered on its own).
        val minRow = finalPath.minOf { it.row }
        val minCol = finalPath.minOf { it.col }
        val maxRow = finalPath.maxOf { it.row }
        val maxCol = finalPath.maxOf { it.col }
        val cropped = finalPath.map { Cell(it.row - minRow, it.col - minCol) }

        return Level(
            id = 0,
            title = title,
            rows = maxRow - minRow + 1,
            cols = maxCol - minCol + 1,
            cells = cropped.toSet(),
            start = cropped.first(),
            accentKey = accentKey
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
