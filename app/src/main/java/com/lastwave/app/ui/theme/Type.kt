package com.lastwave.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.lastwave.app.R

/**
 * SF Pro Display — BitChord's signature face, ported over wholesale as part
 * of the redesign. Only the weights the type scale actually asks for are
 * bundled (Compose synthesises nothing, so a missing weight would silently
 * fall back to the nearest one shipped).
 */
val SFProDisplay = FontFamily(
    Font(R.font.sf_pro_display_regular, FontWeight.W400),
    Font(R.font.sf_pro_display_medium, FontWeight.W500),
    Font(R.font.sf_pro_display_semibold, FontWeight.W600),
    Font(R.font.sf_pro_display_bold, FontWeight.W700),
    Font(R.font.sf_pro_display_heavy, FontWeight.W800),
)

/**
 * Heavy, tight typography — negative tracking, high weights, the backbone
 * of BitChord's Apple-Music-inspired look. Replaces the old Google Sans
 * Flex variable-font scale. Sizes/line-heights kept from the previous
 * scale so layouts don't reflow; only weight/tracking/family changed.
 */
val LastWaveTypography = Typography(
    displayLarge = TextStyle(fontFamily = SFProDisplay, fontWeight = FontWeight.W800, fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-1.2).sp),
    displayMedium = TextStyle(fontFamily = SFProDisplay, fontWeight = FontWeight.W800, fontSize = 45.sp, lineHeight = 52.sp, letterSpacing = (-1.0).sp),
    displaySmall = TextStyle(fontFamily = SFProDisplay, fontWeight = FontWeight.W800, fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = (-0.9).sp),
    headlineLarge = TextStyle(fontFamily = SFProDisplay, fontWeight = FontWeight.W800, fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = (-0.8).sp),
    headlineMedium = TextStyle(fontFamily = SFProDisplay, fontWeight = FontWeight.W800, fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = (-0.7).sp),
    headlineSmall = TextStyle(fontFamily = SFProDisplay, fontWeight = FontWeight.W700, fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = (-0.5).sp),
    titleLarge = TextStyle(fontFamily = SFProDisplay, fontWeight = FontWeight.W700, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.4).sp),
    titleMedium = TextStyle(fontFamily = SFProDisplay, fontWeight = FontWeight.W600, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = (-0.2).sp),
    titleSmall = TextStyle(fontFamily = SFProDisplay, fontWeight = FontWeight.W600, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = (-0.1).sp),
    bodyLarge = TextStyle(fontFamily = SFProDisplay, fontWeight = FontWeight.W400, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = SFProDisplay, fontWeight = FontWeight.W400, fontSize = 15.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontFamily = SFProDisplay, fontWeight = FontWeight.W400, fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = SFProDisplay, fontWeight = FontWeight.W600, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = (-0.1).sp),
    labelMedium = TextStyle(fontFamily = SFProDisplay, fontWeight = FontWeight.W600, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = SFProDisplay, fontWeight = FontWeight.W600, fontSize = 11.sp, lineHeight = 16.sp),
)

/**
 * The "Use Application Font" toggle's off-state: plain platform default
 * (FontFamily.Default), same sizes/weights as [LastWaveTypography] so
 * turning the toggle off changes ONLY the typeface, not the whole scale.
 */
val SystemTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 57.sp, lineHeight = 64.sp),
    displayMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
)
