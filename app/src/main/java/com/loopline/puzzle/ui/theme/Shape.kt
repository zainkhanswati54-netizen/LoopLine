package com.loopline.puzzle.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * A single set of corner radii used everywhere, so "softly rounded edges"
 * means the same thing on a mode card as it does on a dialog button.
 */
object LoopLineShapes {
    val card = RoundedCornerShape(24.dp)
    val cardCornerDp = 24.dp

    val dialog = RoundedCornerShape(28.dp)
    val dialogCornerDp = 28.dp

    val button = RoundedCornerShape(18.dp)
    val buttonCornerDp = 18.dp

    val chip = RoundedCornerShape(50)
    val iconChip = RoundedCornerShape(14.dp)
    val iconChipCornerDp = 14.dp
}
