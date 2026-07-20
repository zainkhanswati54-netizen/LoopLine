package com.loopline.puzzle.game

data class GridConfig(val rows: Int, val cols: Int, val minCells: Int, val maxCells: Int)

/**
 * Defines how a difficulty tier's grid grows across endless-mode levels.
 *
 * The grid itself only grows once every [growthIntervalLevels] levels (not
 * every 5, like the old curve) - so a tier spends a long, flat stretch at
 * each size before stepping up, instead of visibly inflating every few
 * levels. [maxGrowthSteps] caps how many times it's allowed to grow at all,
 * so a tier settles at a final size and stays there rather than climbing
 * forever.
 *
 * Within a single size "plateau" (the stretch of levels before the next
 * growth step), density falls from [highFillRatio] toward [lowFillRatio]:
 * the first level at a new size is nearly a solid, gap-free shape - easy to
 * read, almost no dead ends - and by the last level at that size the same
 * grid has noticeably more empty cells woven in, forcing real navigation.
 * That fall-off is what "slowly introduces missing tiles" - shape openness
 * ramps up gradually within a size before the size itself changes.
 */
data class DifficultyCurve(
    val baseRows: Int,
    val baseCols: Int,
    val growthIntervalLevels: Int,
    val maxGrowthSteps: Int,
    val highFillRatio: Float,
    val lowFillRatio: Float
)

enum class Difficulty(
    val label: String,
    val description: String,
    val config: GridConfig,
    val curve: DifficultyCurve
) {
    // Levels 1-15: flat 3x3, near-solid shapes - just learning that a
    // stroke has to cover every tile without lifting. 4x4 doesn't appear
    // until level 16, 5x5 not until level 31, and it stops growing there.
    EASY(
        label = "Easy",
        description = "Small grid, a gentle warm-up",
        config = GridConfig(rows = 4, cols = 4, minCells = 8, maxCells = 12),
        curve = DifficultyCurve(
            baseRows = 3, baseCols = 3,
            growthIntervalLevels = 15, maxGrowthSteps = 2, // caps at 3+2 = 5x5
            highFillRatio = 0.90f, lowFillRatio = 0.55f
        )
    ),
    // Same gentle 3x3 opening, but climbs a bit sooner and doesn't cap
    // until 6x6 - the "balanced" middle tier between Easy's plateau and
    // Hard's faster climb.
    NORMAL(
        label = "Normal",
        description = "Balanced size and shape",
        config = GridConfig(rows = 5, cols = 5, minCells = 14, maxCells = 20),
        curve = DifficultyCurve(
            baseRows = 3, baseCols = 3,
            growthIntervalLevels = 12, maxGrowthSteps = 3, // caps at 3+3 = 6x6
            highFillRatio = 0.90f, lowFillRatio = 0.50f
        )
    ),
    // Starts a size up from the other two (4x4, not 3x3) so picking Hard
    // still means something from level 1, but it still opens on a flat,
    // near-solid shape rather than throwing a big sparse grid at level 1.
    HARD(
        label = "Hard",
        description = "Bigger grid, trickier turns",
        config = GridConfig(rows = 6, cols = 6, minCells = 22, maxCells = 30),
        curve = DifficultyCurve(
            baseRows = 4, baseCols = 4,
            growthIntervalLevels = 10, maxGrowthSteps = 3, // caps at 4+3 = 7x7
            highFillRatio = 0.90f, lowFillRatio = 0.50f
        )
    );

    /**
     * Endless-mode progression: grid size steps up only every
     * [DifficultyCurve.growthIntervalLevels] levels (see [DifficultyCurve]
     * doc), and within a size, the min/max cell-count window itself tracks
     * how far into that plateau [levelNumber] is - so the range narrows in
     * lockstep with [targetCellCount] instead of staying static while only
     * the target moves inside it.
     */
    fun scaledConfig(levelNumber: Int): GridConfig {
        val growthSteps = ((levelNumber - 1) / curve.growthIntervalLevels).coerceAtMost(curve.maxGrowthSteps)
        val rows = curve.baseRows + growthSteps
        val cols = curve.baseCols + growthSteps
        val maxPossible = rows * cols

        val fillRatio = fillRatioAt(levelNumber)
        val maxCells = (maxPossible * fillRatio).toInt().coerceIn(6, maxPossible)
        // The acceptable window below maxCells - wide enough that the
        // generator's random walks reliably land in range within its
        // attempt budget, narrow enough that levelNumber still meaningfully
        // steers the result via targetCellCount.
        val windowWidth = (maxPossible * 0.15f).toInt().coerceAtLeast(2)
        val minCells = (maxCells - windowWidth).coerceAtLeast(5).coerceAtMost(maxCells - 1)

        return GridConfig(rows, cols, minCells, maxCells)
    }

    /**
     * The exact cell count [levelNumber] aims for - the midpoint of that
     * level's own [scaledConfig] window, which is already level-specific
     * (see [scaledConfig]), so this just centers the aim inside it rather
     * than re-deriving the curve a second time.
     */
    fun targetCellCount(levelNumber: Int): Int {
        val cfg = scaledConfig(levelNumber)
        return ((cfg.minCells + cfg.maxCells) / 2f).toInt().coerceIn(cfg.minCells, cfg.maxCells)
    }

    /** 0..1 progress through the current size plateau, mapped onto
     * [DifficultyCurve.highFillRatio]..[DifficultyCurve.lowFillRatio]. Once
     * growth is capped, this keeps cycling every [DifficultyCurve.growthIntervalLevels]
     * levels - fine, since the grid no longer changes size, it just keeps
     * varying openness for long-term replay variety instead of flatlining. */
    private fun fillRatioAt(levelNumber: Int): Float {
        val levelInPlateau = (levelNumber - 1) % curve.growthIntervalLevels
        val plateauProgress = levelInPlateau / (curve.growthIntervalLevels - 1).coerceAtLeast(1).toFloat()
        return curve.highFillRatio - (curve.highFillRatio - curve.lowFillRatio) * plateauProgress
    }
}
