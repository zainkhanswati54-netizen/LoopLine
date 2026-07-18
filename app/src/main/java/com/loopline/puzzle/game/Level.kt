package com.loopline.puzzle.game

/**
 * A single grid coordinate. (0,0) is the top-left of the level's bounding box.
 */
data class Cell(val row: Int, val col: Int) {
    fun isAdjacentTo(other: Cell): Boolean {
        val dr = kotlin.math.abs(row - other.row)
        val dc = kotlin.math.abs(col - other.col)
        return (dr == 1 && dc == 0) || (dr == 0 && dc == 1)
    }
}

/**
 * A puzzle level: a set of playable cells inside a [rows] x [cols] bounding
 * box (cells outside [cells] are simply not rendered, which is what creates
 * the irregular shapes), plus the single starting cell.
 *
 * [accentKey] picks the level's accent color (see ui/theme/Color.kt ->
 * accentColorFor) so game logic stays independent of Compose types.
 */
data class Level(
    val id: Int,
    val title: String,
    val rows: Int,
    val cols: Int,
    val cells: Set<Cell>,
    val start: Cell,
    val accentKey: String
) {
    val cellCount: Int get() = cells.size
}
