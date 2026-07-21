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
 * The LoopLine mark, shown on Splash and Home. Uses the same source image
 * as the app's launcher icon (see drawable-nodpi/ic_launcher_photo.png) so
 * the in-app brand mark and the icon on the player's home screen actually
 * match, instead of being two different designs.
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
