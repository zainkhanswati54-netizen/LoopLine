package com.loopline.puzzle.ui.theme

import androidx.compose.ui.graphics.Color

// Core palette, sampled from the reference puzzle grid look:
// deep navy canvas, light-gray idle tiles, three accent colors for
// active/endpoint states.
val BackgroundDark = Color(0xFF14152B)
val SurfaceCard = Color(0xFF1E1F3B)
val TileIdle = Color(0xFFC4C4CC)
val TileOutline = Color(0xFF3A3B5C)

val AccentBlue = Color(0xFF2D9CF0)
val AccentOrange = Color(0xFFF6A623)
val AccentGreen = Color(0xFF4CAF50)

val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFA0A0B8)

/** Maps a Level's accentKey (plain string, kept Compose-free in the game model) to a Color. */
fun accentColorFor(key: String): Color = when (key) {
    "blue" -> AccentBlue
    "orange" -> AccentOrange
    "green" -> AccentGreen
    else -> AccentBlue
}
