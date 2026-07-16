package com.loopline.puzzle.game

/**
 * Handcrafted levels. Every shape below was verified with a backtracking
 * Hamiltonian-path search before being added here, so each one is
 * guaranteed solvable from its listed start cell.
 */
object LevelRepository {

    val levels: List<Level> = listOf(
        Level(
            id = 1,
            title = "Level 1",
            rows = 5,
            cols = 5,
            cells = setOf(
                Cell(0, 2), Cell(0, 3), Cell(0, 4),
                Cell(1, 2), Cell(1, 3), Cell(1, 4),
                Cell(2, 0), Cell(2, 1), Cell(2, 2), Cell(2, 3), Cell(2, 4),
                Cell(3, 0), Cell(3, 1), Cell(3, 2), Cell(3, 3), Cell(3, 4),
                Cell(4, 0), Cell(4, 1), Cell(4, 2), Cell(4, 3),
            ),
            start = Cell(4, 0),
            accentKey = "green"
        ),
        Level(
            id = 2,
            title = "Level 2",
            rows = 5,
            cols = 5,
            cells = setOf(
                Cell(0, 1), Cell(0, 2), Cell(0, 3), Cell(0, 4),
                Cell(1, 0), Cell(1, 1), Cell(1, 2), Cell(1, 3),
                Cell(2, 0), Cell(2, 1), Cell(2, 2), Cell(2, 3),
                Cell(3, 0), Cell(3, 1), Cell(3, 2), Cell(3, 3),
                Cell(4, 0), Cell(4, 1), Cell(4, 2), Cell(4, 3),
            ),
            start = Cell(0, 4),
            accentKey = "blue"
        ),
        Level(
            id = 3,
            title = "Level 3",
            rows = 5,
            cols = 5,
            cells = setOf(
                Cell(0, 2), Cell(0, 3), Cell(0, 4),
                Cell(1, 0), Cell(1, 1), Cell(1, 2), Cell(1, 4),
                Cell(2, 0), Cell(2, 1), Cell(2, 2), Cell(2, 4),
                Cell(3, 0), Cell(3, 1), Cell(3, 2), Cell(3, 4),
                Cell(4, 0), Cell(4, 1), Cell(4, 2), Cell(4, 3), Cell(4, 4),
            ),
            start = Cell(0, 4),
            accentKey = "orange"
        ),
    )

    fun byId(id: Int): Level? = levels.find { it.id == id }

    fun nextIdAfter(id: Int): Int? {
        val idx = levels.indexOfFirst { it.id == id }
        if (idx == -1 || idx == levels.lastIndex) return null
        return levels[idx + 1].id
    }
}
