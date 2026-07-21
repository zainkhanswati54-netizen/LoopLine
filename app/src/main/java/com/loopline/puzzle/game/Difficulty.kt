package com.loopline.puzzle.game

data class GridConfig(val rows: Int, val cols: Int, val minCells: Int, val maxCells: Int)

/**
 * Grid label/description only - Easy/Normal/Hard are still used to bucket
 * stats, leaderboards, and the in-game header label, and Daily Challenge
 * still borrows [NORMAL]'s fixed [config] as its one-off puzzle size.
 *
 * The actual level-by-level difficulty curve, however, is now global (see
 * the companion object below): grid size and openness are a pure function
 * of the absolute Classic-track level number, not of "levels since this
 * tier began". That fixes a real regression the old per-tier curves had -
 * each tier used to restart its own growth from a small base grid, so the
 * puzzle would visibly *shrink* the instant the player crossed from level
 * 40 (Easy, already grown to 5x5) into level 41 (Normal, reset back down
 * to 3x3). A player climbing the ladder should never feel like they went
 * backwards.
 */
enum class Difficulty(
    val label: String,
    val description: String,
    val config: GridConfig
) {
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
     * Endless-mode progression for [levelNumber] - see [DifficultyCurve]
     * (the companion object below) for how grid size and fill ratio are
     * derived. Deliberately ignores which enum constant this is called on:
     * the curve is one continuous line across the whole Classic track, and
     * [Difficulty.forLevel] is only used to *label* a stretch of it, not to
     * restart it.
     */
    fun scaledConfig(levelNumber: Int): GridConfig = DifficultyCurve.scaledConfig(levelNumber)

    /**
     * The exact cell count [levelNumber] aims for - the midpoint of that
     * level's own [scaledConfig] window.
     */
    fun targetCellCount(levelNumber: Int): Int {
        val cfg = scaledConfig(levelNumber)
        return ((cfg.minCells + cfg.maxCells) / 2f).toInt().coerceIn(cfg.minCells, cfg.maxCells)
    }

    companion object {
        /**
         * Classic mode is a single endless track (no up-front picker), but
         * the puzzle's *label* still steps through all three tiers as the
         * player climbs: Easy for levels 1-40, Normal for 41-70, then Hard
         * from 71 onward with no ceiling. This boundary is purely cosmetic
         * now (stats bucketing, the in-game header text, leaderboard rows)
         * - it no longer resets the grid-size curve, which is why crossing
         * it doesn't shrink the puzzle anymore. See [DifficultyCurve].
         */
        fun forLevel(levelNumber: Int): Difficulty = when {
            levelNumber <= 40 -> EASY
            levelNumber <= 70 -> NORMAL
            else -> HARD
        }
    }
}

/**
 * The single, continuous difficulty curve for the whole Classic track,
 * keyed purely off the absolute level number - no per-tier resets.
 *
 * Two things ramp up together as [levelNumber] grows:
 *
 * 1. **Grid size** ([gridSizeForLevel]): a square grid that grows in steps,
 *    authored via [stages], up to [MAX_GRID_SIZE] (10x10) - the largest
 *    square the puzzle canvas can reliably show without overflowing a
 *    typical phone screen, since it has no scroll or zoom. Size is
 *    monotonic (never resets going forward) and the pace itself speeds up
 *    after level 70 (where the Hard label begins): early stages each last
 *    8-22 levels, but from level 71 on, every stage is a flat 8 levels -
 *    noticeably faster growth, which is the concrete mechanism behind "get
 *    quite challenging after 70". Once size hits its cap, difficulty keeps
 *    climbing anyway via (2) below - the grid just stops getting visually
 *    bigger while staying reliably on-screen.
 *
 * 2. **Fill ratio** ([fillRatioAt]): within a size "plateau" (the run of
 *    levels before the next growth step), the fraction of the grid that
 *    must be covered falls from a high value at the start of the plateau
 *    toward a low value at the end - so the very first level at a new size
 *    is a dense, easy-to-read near-full shape, and by the last level at
 *    that size the same grid has real gaps woven in. Both the high and low
 *    bounds themselves drift downward as [levelNumber] grows (see
 *    [hardnessProgress]), and drift faster past level 70, so even a
 *    same-sized grid gets progressively more open/twisty the deeper into
 *    the run the player is.
 */
private object DifficultyCurve {

    private data class Stage(val startLevel: Int, val size: Int)

    // Levels 1-40 (Easy label): 3x3 -> 5x5.
    // Levels 41-70 (Normal label): 6x6 -> 7x7 - starts a size *above* where
    // Easy topped out, so the climb keeps going instead of resetting.
    // Levels 71+ (Hard label): 8x8 upward, one size every 8 levels - a
    // deliberately faster climb than either tier before it.
    private val stages = listOf(
        Stage(startLevel = 1, size = 3),
        Stage(startLevel = 9, size = 4),
        Stage(startLevel = 19, size = 5),
        Stage(startLevel = 41, size = 6),
        Stage(startLevel = 61, size = 7),
        Stage(startLevel = 71, size = 8),
        Stage(startLevel = 79, size = 9),
        Stage(startLevel = 87, size = 10),
        Stage(startLevel = 95, size = 11),
        Stage(startLevel = 103, size = 12),
        Stage(startLevel = 111, size = 13),
        Stage(startLevel = 119, size = 14)
    )

    // Beyond the last authored stage, keep growing - one size bigger every
    // this-many levels - up to MAX_GRID_SIZE.
    private const val EXTENDED_STAGE_INTERVAL = 20

    // The puzzle canvas has no scroll/zoom, so grid size can't climb
    // forever without eventually overflowing the screen - 10x10 is the
    // largest square that reliably fits at [MIN_CELL_SIZE] on a typical
    // phone width. Difficulty keeps climbing past this point anyway, since
    // [fillRatioAt] never stops trending down (see [hardnessProgress]) -
    // the grid just stops growing bigger while staying reliably on-screen.
    private const val MAX_GRID_SIZE = 10

    /** Index of the stage [levelNumber] currently sits in (last stage whose
     * startLevel is <= levelNumber). */
    private fun stageIndex(levelNumber: Int): Int =
        stages.indexOfLast { it.startLevel <= levelNumber }.coerceAtLeast(0)

    /** How many *extra* stages past the authored table [levelNumber] has
     * climbed through, once it's past stage.last() - 0 while still inside
     * the table. */
    private fun extraStagesPastTable(levelNumber: Int): Int {
        val last = stages.last()
        if (levelNumber < last.startLevel) return 0
        return (levelNumber - last.startLevel) / EXTENDED_STAGE_INTERVAL
    }

    fun gridSizeForLevel(levelNumber: Int): Int {
        val idx = stageIndex(levelNumber)
        val base = stages[idx].size
        val uncapped = if (idx == stages.size - 1) base + extraStagesPastTable(levelNumber) else base
        return uncapped.coerceAtMost(MAX_GRID_SIZE)
    }

    /** Inclusive [start, end] level range of the plateau [levelNumber]
     * currently sits in - used to interpolate fill ratio within it. */
    private fun plateauBounds(levelNumber: Int): Pair<Int, Int> {
        val idx = stageIndex(levelNumber)
        return if (idx < stages.size - 1) {
            stages[idx].startLevel to (stages[idx + 1].startLevel - 1)
        } else {
            val last = stages.last()
            val extra = extraStagesPastTable(levelNumber)
            val thisPlateauStart = last.startLevel + extra * EXTENDED_STAGE_INTERVAL
            thisPlateauStart to (thisPlateauStart + EXTENDED_STAGE_INTERVAL - 1)
        }
    }

    /** 0 at level 1, rising to ~0.55 by level 70, then continuing to climb
     * (more slowly) toward 1.0 over the next 160 levels and holding there -
     * the overall "how much harder has the whole game gotten" dial that
     * [fillRatioAt] leans on, separate from grid size. */
    private fun hardnessProgress(levelNumber: Int): Float {
        val progress = if (levelNumber <= 70) {
            (levelNumber - 1) / 70f * 0.55f
        } else {
            val extra = ((levelNumber - 70).coerceAtMost(160)) / 160f
            0.55f + extra * 0.45f
        }
        return progress.coerceIn(0f, 1f)
    }

    private fun highFillRatioAt(levelNumber: Int): Float = 0.92f - 0.12f * hardnessProgress(levelNumber)
    private fun lowFillRatioAt(levelNumber: Int): Float = 0.62f - 0.22f * hardnessProgress(levelNumber)

    private fun fillRatioAt(levelNumber: Int): Float {
        val (plateauStart, plateauEnd) = plateauBounds(levelNumber)
        val span = (plateauEnd - plateauStart).coerceAtLeast(1)
        val plateauProgress = (levelNumber - plateauStart).toFloat() / span
        val high = highFillRatioAt(levelNumber)
        val low = lowFillRatioAt(levelNumber)
        return high - (high - low) * plateauProgress
    }

    /** Mirrors the old per-tier math (see git history), just driven by the
     * global functions above instead of a per-enum base/curve. */
    fun scaledConfig(levelNumber: Int): GridConfig {
        val size = gridSizeForLevel(levelNumber)
        val maxPossible = size * size

        val fillRatio = fillRatioAt(levelNumber)
        val maxCells = (maxPossible * fillRatio).toInt().coerceIn(6, maxPossible)
        val windowWidth = (maxPossible * 0.15f).toInt().coerceAtLeast(2)
        val minCells = (maxCells - windowWidth).coerceAtLeast(5).coerceAtMost(maxCells - 1)

        return GridConfig(size, size, minCells, maxCells)
    }
}
