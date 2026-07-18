package com.loopline.puzzle.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.loopline.puzzle.R

/**
 * The LoopLine mark - the ornate gold circular badge (single-stroke path
 * through a 3x3 tile grid, framed in a Celtic-knot-style ring) supplied as
 * the app's brand image, used on Splash and Home.
 *
 * This used to be entirely code-drawn (Canvas). Swapped for the bundled
 * image because the filigree ring is a hand-designed illustration, not
 * something worth faithfully reproducing in draw calls.
 */
@Composable
fun LoopLineLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.loopline_logo),
        contentDescription = "LoopLine",
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(CircleShape)
    )
}
