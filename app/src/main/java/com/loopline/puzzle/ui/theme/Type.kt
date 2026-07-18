package com.loopline.puzzle.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.loopline.puzzle.R

/**
 * Poppins, bundled as real static-weight .ttf files (not the system
 * default, and not a variable font) so every weight renders pixel-correct
 * on every supported Android version (minSdk 24) with zero fallback risk.
 * A clean geometric sans keeps the brief's "clean, modern typography"
 * promise; the luxury feel comes from the metal palette, generous
 * tracking on eyebrow/display text, and restrained weight contrast here
 * rather than from an ornate typeface.
 */
val LoopLineFontFamily = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
    Font(R.font.poppins_extrabold, FontWeight.ExtraBold),
)

val LoopLineTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = LoopLineFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = LoopLineFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp,
        letterSpacing = (-0.3).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = LoopLineFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 23.sp,
        letterSpacing = (-0.2).sp
    ),
    titleLarge = TextStyle(
        fontFamily = LoopLineFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),
    titleMedium = TextStyle(
        fontFamily = LoopLineFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = LoopLineFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = LoopLineFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = LoopLineFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp
    ),
    labelMedium = TextStyle(
        fontFamily = LoopLineFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 1.6.sp
    ),
    labelSmall = TextStyle(
        fontFamily = LoopLineFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp
    ),
)
