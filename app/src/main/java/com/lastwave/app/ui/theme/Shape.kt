package com.lastwave.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Apple-style redesign: soft, continuous rounded corners everywhere,
 * replacing the earlier Nothing OS flat-rectangle look. This file is the
 * single place that controls corner treatment app-wide, so every screen
 * that already references [LastWaveShapes] or these named constants picks
 * up the new rounding without per-screen edits.
 */
val LastWaveShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

val ExpressiveHeroShape = RoundedCornerShape(20.dp)
val ExpressivePillShape = RoundedCornerShape(50)
val HeroInnerShape = RoundedCornerShape(14.dp)
val StatPillShape = RoundedCornerShape(50)
val ListContainerShape = RoundedCornerShape(16.dp)
val BadgePillShape = RoundedCornerShape(50)
val NowPlayingCardShape = RoundedCornerShape(14.dp)
val TrackRowShape = RoundedCornerShape(12.dp)
val ArtworkShape = RoundedCornerShape(8.dp)
