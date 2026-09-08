package com.lastwave.app.ui.home

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Cinematic dark palette for the Home screen redesign — deliberately scoped
 * here rather than added to the app-wide theme system (ui/theme/Theme.kt),
 * which currently drives a light "Apple-style" scheme for the rest of the
 * app. Every Home composable already reads MaterialTheme.colorScheme
 * tokens rather than hardcoded colors, so wrapping HomeScreen's content in
 * this scheme (see HomeScreen.kt) re-skins it for free with zero changes
 * needed to StatsCard/TrackRow/MixHeader/etc.
 *
 * If this direction gets approved for the rest of the app, promote this
 * into Md3SchemeBuilder as a proper dark variant instead of duplicating it.
 */
val LastWaveCinematicColorScheme = darkColorScheme(
    background = Color(0xFF080B10),
    onBackground = Color(0xFFF5F7FA),

    surface = Color(0xFF11161D),
    onSurface = Color(0xFFF5F7FA),
    surfaceVariant = Color(0xFF181F28),
    onSurfaceVariant = Color(0xFF9BA5B1),

    surfaceContainer = Color(0xFF11161D),
    surfaceContainerLow = Color(0xFF0D1116),
    surfaceContainerHigh = Color(0xFF181F28),
    surfaceContainerHighest = Color(0xFF1E2731),

    primary = Color(0xFF1597FF),
    onPrimary = Color(0xFF04121F),
    primaryContainer = Color(0xFF163A56),
    onPrimaryContainer = Color(0xFF1597FF),

    outline = Color(0xFF2A323D),
    outlineVariant = Color(0xFF1E2630),

    error = Color(0xFFFF6B6B),
    onError = Color(0xFF1A0000),
)
