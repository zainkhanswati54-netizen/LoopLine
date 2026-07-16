package com.loopline.puzzle.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * The reference app uses a rounded, geometric sans-serif — visually close to
 * Poppins, Nunito, or DM Sans. To match it exactly:
 *   1. Download the .ttf files from https://fonts.google.com
 *   2. Drop them into app/src/main/res/font/
 *   3. Build a FontFamily from those files and swap it in below
 *
 * Using the system default for now so the project builds and runs with zero
 * extra downloads. Swapping fonts later is a one-line change.
 */
val LoopLineFontFamily = FontFamily.Default

val LoopLineTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = LoopLineFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = LoopLineFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = LoopLineFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = LoopLineFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = LoopLineFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    labelLarge = TextStyle(
        fontFamily = LoopLineFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp
    ),
)
