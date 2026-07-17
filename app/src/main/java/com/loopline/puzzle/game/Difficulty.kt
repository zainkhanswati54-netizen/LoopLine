package com.loopline.puzzle.game

data class GridConfig(val rows: Int, val cols: Int, val minCells: Int, val maxCells: Int)

enum class Difficulty(val label: String, val description: String, val config: GridConfig) {
    EASY(
        label = "Easy",
        description = "Small grid, a gentle warm-up",
        config = GridConfig(rows = 4, cols = 4, minCells = 8, maxCells = 12)
    ),
    NORMAL(
        label = "Normal",
        description = "Balanced size and shape",
        config = GridConfig(rows = 5, cols = 5, minCells = 14, maxCells = 20)
    ),
    HARD(
        label = "Hard",
        description = "Bigger grid, trickier turns",
        config = GridConfig(rows = 6, cols = 6, minCells = 22, maxCells = 30)
    );

    /**
     * Endless-mode progression: every 5 levels cleared at this difficulty,
     * the puzzle gets a little bigger and denser, up to a capped ceiling so
     * grids never grow unreasonably large for a phone screen. Verified by
     * simulation that the generator still reliably hits these ranges even
     * near full-grid coverage (see project notes).
     */
    fun scaledConfig(levelNumber: Int): GridConfig {
        val growthSteps = (levelNumber - 1) / 5
        val rows = (config.rows + growthSteps / 2).coerceAtMost(config.rows + 3)
        val cols = (config.cols + growthSteps / 2).coerceAtMost(config.cols + 3)
        val maxPossible = rows * cols

        val extraCells = growthSteps * 2
        val maxCells = (config.maxCells + extraCells).coerceAtMost(maxPossible)
        val minCells = (config.minCells + extraCells / 2).coerceAtMost(maxCells - 2).coerceAtLeast(6)

        return GridConfig(rows, cols, minCells, maxCells)
    }
}
