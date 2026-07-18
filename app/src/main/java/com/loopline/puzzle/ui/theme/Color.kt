package com.loopline.puzzle.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * LoopLine's palette: a warm near-black canvas with three metallic accent
 * families — gold, copper, and rose gold (literally an alloy of the other
 * two), which keeps every accent in the puzzle on-theme with the brief
 * instead of reaching for an unrelated hue for the third gameplay color.
 *
 * Each metal is expressed as a 4-5 stop ramp (highlight -> core -> deep ->
 * shadow) so it can be drawn as a brushed-metal gradient, not just a flat
 * fill. See Gradients.kt for the Brush objects built from these stops.
 */

// ---- Background & surfaces --------------------------------------------
val BackgroundBase = Color(0xFF110C09)
val BackgroundElevated = Color(0xFF1D1510)

// Compatibility alias: LevelSelectScreen.kt (not part of the files shared
// with me — see README/chat) still references the old pre-redesign name.
// Keeping this avoids breaking that file sight-unseen; safe to delete once
// that screen is updated to use BackgroundBase / backgroundBrush() directly.
val BackgroundDark = BackgroundBase

val SurfaceCard = Color(0xFF1F160F)
val SurfaceCardElevated = Color(0xFF2B2015)
val SurfaceCardBorder = Color(0xFFD9B26A)

// ---- Gold (primary) ------------------------------------------------------
val GoldHighlight = Color(0xFFF8ECC4)
val GoldLight = Color(0xFFE8CC85)
val Gold = Color(0xFFCBA25A)
val GoldDeep = Color(0xFF9C7A3D)
val GoldShadow = Color(0xFF6B5226)

// ---- Copper (secondary) ---------------------------------------------------
val CopperHighlight = Color(0xFFF3BD8E)
val CopperLight = Color(0xFFE0996A)
val Copper = Color(0xFFC2794C)
val CopperDeep = Color(0xFF94552E)
val CopperShadow = Color(0xFF5E331B)

// ---- Rose gold (tertiary — gold + copper alloy, still on-theme) ----------
val RoseGoldHighlight = Color(0xFFF1CBBB)
val RoseGoldLight = Color(0xFFDFAA97)
val RoseGold = Color(0xFFC48874)
val RoseGoldDeep = Color(0xFF93594A)
val RoseGoldShadow = Color(0xFF5F392F)

// ---- Neutrals --------------------------------------------------------
val TileIdle = Color(0xFFE9E0D1)
val TileIdleShade = Color(0xFFCBBCA0)
val Outline = Color(0xFF3C3022)

val TextPrimary = Color(0xFFF6EFE3)
val TextSecondary = Color(0xFFAB9C86)
val TextTertiary = Color(0xFF6E6353)

/** For text/icons drawn on top of a gold/copper/rose-gold gradient fill — all three metals are light/mid-warm, so one dark ink works on all of them. */
val TextOnMetal = Color(0xFF241A10)

/**
 * Maps a Level's accentKey (plain string, kept Compose-free in the game
 * model) to its core metal color. "gold" is also the fallback for legacy
 * or unrecognized keys.
 */
fun accentColorFor(key: String): Color = when (key) {
    "gold" -> Gold
    "copper" -> Copper
    "rosegold" -> RoseGold
    else -> Gold
}

/** The highlight stop for a given accent key — used for glows and rims. */
fun accentHighlightFor(key: String): Color = when (key) {
    "gold" -> GoldHighlight
    "copper" -> CopperHighlight
    "rosegold" -> RoseGoldHighlight
    else -> GoldHighlight
}

/** The deep/shadow stop for a given accent key — used for bevels. */
fun accentDeepFor(key: String): Color = when (key) {
    "gold" -> GoldDeep
    "copper" -> CopperDeep
    "rosegold" -> RoseGoldDeep
    else -> GoldDeep
}
