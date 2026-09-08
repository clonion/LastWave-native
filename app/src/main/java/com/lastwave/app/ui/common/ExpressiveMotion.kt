package com.lastwave.app.ui.common

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

/**
 * Nothing OS redesign: transitions read as a device responding, not a
 * surface animating for its own sake — short (150-200ms), ease-out, no
 * spring/bounce, no scale morphing (that reads as "liquid," which this
 * flat/mechanical look explicitly avoids). Object name and function
 * signatures are unchanged from the previous "Expressive" motion set so
 * every call site elsewhere in the app picks this up automatically.
 */
object ExpressiveMotion {
    const val Quick = 120
    const val Standard = 180
    const val Emphasized = 200

    /** No longer bouncy — critically damped (DampingRatioNoBouncy) — kept
     *  for the handful of gesture-driven call sites (player drag, lyrics
     *  scroll) that still need a spring's velocity-continuity rather than
     *  a fixed-duration tween, just without any overshoot. */
    fun <T> spatialSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh,
    )

    /**
     * The one press-scale feel for every tappable icon button in the
     * player (like, lyrics, prev/next, play/pause) — was previously three
     * different hand-tuned springs across these buttons (this file had
     * spatialSpring's no-bounce for prev/next/like/lyrics while play/pause
     * used its own bouncy spring inline), so identical taps felt
     * different depending on which button you hit. This is the play
     * button's original spring — the most "primary CTA" feeling of the
     * three — promoted to the shared default so every player control
     * settles the same way.
     */
    fun <T> pressSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    fun <T> smoothSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh,
    )

    fun forwardEnter(): EnterTransition =
        fadeIn(tween(Standard, easing = LinearOutSlowInEasing)) +
            slideInHorizontally(tween(Standard, easing = LinearOutSlowInEasing)) { it / 16 }

    fun forwardExit(): ExitTransition =
        fadeOut(tween(Quick))

    fun backEnter(): EnterTransition =
        fadeIn(tween(Standard, easing = LinearOutSlowInEasing)) +
            slideInHorizontally(tween(Standard, easing = LinearOutSlowInEasing)) { -it / 16 }

    fun backExit(): ExitTransition =
        fadeOut(tween(Quick)) +
            slideOutHorizontally(tween(Quick, easing = LinearOutSlowInEasing)) { it / 16 }
}
