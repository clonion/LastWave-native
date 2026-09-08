package com.lastwave.app.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.random.Random

/**
 * A left-to-right shimmer sweep, implemented natively with Compose's own
 * animation APIs (no third-party shimmer library) so skeleton loading
 * states read as "almost loaded" instead of a blank spinner.
 */
@Composable
fun Modifier.shimmerSweep(): Modifier = composedShimmer()

@Composable
private fun Modifier.composedShimmer(): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmerSweep")
    val translateAnim by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing, delayMillis = 200),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerTranslate",
    )
    val base = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val highlight = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
    return this.background(
        Brush.linearGradient(
            colors = listOf(base, highlight, base),
            start = Offset(translateAnim * 600f - 300f, 0f),
            end = Offset(translateAnim * 600f + 300f, 600f),
        ),
    )
}

@Composable
fun ShimmerHost(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
        modifier = modifier,
        content = content,
    )
}

@Composable
fun TextLinePlaceholder(
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 16.dp,
    widthFraction: Float? = null,
    shape: CornerBasedShape = RoundedCornerShape(6.dp),
) {
    val fraction = widthFraction ?: remember { 0.35f + Random.nextFloat() * 0.4f }
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .padding(vertical = 4.dp)
            .height(height)
            .fillMaxWidth(fraction)
            .clip(shape)
            .shimmerSweep(),
    )
}

@Composable
fun GridTilePlaceholder(modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 142.dp) {
    Column(modifier = modifier.width(size)) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .shimmerSweep(),
        )
        Spacer(Modifier.height(8.dp))
        TextLinePlaceholder(height = 14.dp, widthFraction = 0.8f)
        TextLinePlaceholder(height = 12.dp, widthFraction = 0.5f)
    }
}

/** Skeleton shown while Home's stats + quick picks are loading, shaped to
 *  roughly match the real layout so the loading state doesn't feel like a
 *  jarring blank-then-pop-in switch. */
@Composable
fun HomeLoadingShimmer(modifier: Modifier = Modifier) {
    ShimmerHost(modifier = modifier.fillMaxWidth().padding(top = 8.dp)) {
        TextLinePlaceholder(height = 34.dp, widthFraction = 0.55f, modifier = Modifier.padding(bottom = 8.dp))
        TextLinePlaceholder(height = 16.dp, widthFraction = 0.3f, modifier = Modifier.padding(bottom = 20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            repeat(3) {
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .shimmerSweep(),
                )
            }
        }
        TextLinePlaceholder(height = 22.dp, widthFraction = 0.4f, modifier = Modifier.padding(bottom = 12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            repeat(3) { GridTilePlaceholder() }
        }
    }
}
