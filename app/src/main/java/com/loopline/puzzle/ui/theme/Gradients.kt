package com.loopline.puzzle.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * LoopLine's signature visual motif: every metal accent is drawn as a
 * diagonal highlight -> core -> shadow ramp, the way light catches a
 * brushed metal panel, rather than a flat fill. These three brushes (one
 * per accent metal) are reused everywhere an accent needs to read as
 * "metallic": buttons, active tiles, the stroke path, the logo mark.
 */
fun goldBrush(): Brush = Brush.linearGradient(
    colors = listOf(GoldHighlight, GoldLight, Gold, GoldDeep)
)

fun copperBrush(): Brush = Brush.linearGradient(
    colors = listOf(CopperHighlight, CopperLight, Copper, CopperDeep)
)

fun roseGoldBrush(): Brush = Brush.linearGradient(
    colors = listOf(RoseGoldHighlight, RoseGoldLight, RoseGold, RoseGoldDeep)
)

/** Brush lookup that mirrors accentColorFor's key -> metal mapping. */
fun accentBrushFor(key: String): Brush = when (key) {
    "gold" -> goldBrush()
    "copper" -> copperBrush()
    "rosegold" -> roseGoldBrush()
    else -> goldBrush()
}

/** The app's dark canvas: a soft top-lit vignette rather than a flat fill. */
fun backgroundBrush(): Brush = Brush.verticalGradient(
    colors = listOf(BackgroundElevated, BackgroundBase)
)

/** Subtle depth on resting card surfaces — barely-there, not a metal ramp. */
fun cardSurfaceBrush(): Brush = Brush.verticalGradient(
    colors = listOf(SurfaceCardElevated, SurfaceCard)
)

/**
 * Draws a soft diagonal bevel stroke inside [shape]: a light rim on the
 * top-left edge fading through the accent color to a darker rim on the
 * bottom-right edge, evoking an engraved/brushed metal plate. This is
 * LoopLine's signature detail, used consistently on tiles, cards, chips,
 * and buttons so the whole app reads as one material.
 *
 * [cornerDp] should match the corner radius used by the shape/clip this
 * modifier is paired with.
 */
fun Modifier.metallicBevel(
    cornerDp: Dp,
    highlight: Color = Color.White.copy(alpha = 0.35f),
    shadow: Color = Color.Black.copy(alpha = 0.35f),
    strokeWidthDp: Dp = 1.2.dp
): Modifier = this.drawBehind {
    val corner = CornerRadius(cornerDp.toPx())
    val strokeWidth = strokeWidthDp.toPx()
    val inset = strokeWidth / 2f
    val rect = RoundRect(
        left = inset,
        top = inset,
        right = size.width - inset,
        bottom = size.height - inset,
        cornerRadius = corner
    )
    val path = Path().apply { addRoundRect(rect) }
    drawPath(
        path = path,
        brush = Brush.linearGradient(
            colors = listOf(highlight, Color.Transparent, shadow),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height)
        ),
        style = Stroke(width = strokeWidth)
    )
}

/**
 * Same signature bevel as [metallicBevel], but as a direct DrawScope call
 * for use inside a raw Canvas (e.g. per-tile drawing in GameScreen) where
 * a Modifier isn't available per-element.
 */
fun DrawScope.drawMetallicBevel(
    topLeft: Offset,
    boxSize: Size,
    cornerRadiusPx: Float,
    highlight: Color = Color.White.copy(alpha = 0.35f),
    shadow: Color = Color.Black.copy(alpha = 0.35f),
    strokeWidthPx: Float
) {
    val inset = strokeWidthPx / 2f
    val rect = RoundRect(
        left = topLeft.x + inset,
        top = topLeft.y + inset,
        right = topLeft.x + boxSize.width - inset,
        bottom = topLeft.y + boxSize.height - inset,
        cornerRadius = CornerRadius(cornerRadiusPx)
    )
    val path = Path().apply { addRoundRect(rect) }
    drawPath(
        path = path,
        brush = Brush.linearGradient(
            colors = listOf(highlight, Color.Transparent, shadow),
            start = Offset(topLeft.x, topLeft.y),
            end = Offset(topLeft.x + boxSize.width, topLeft.y + boxSize.height)
        ),
        style = Stroke(width = strokeWidthPx)
    )
}
