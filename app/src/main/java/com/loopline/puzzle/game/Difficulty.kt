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
    ),
}
