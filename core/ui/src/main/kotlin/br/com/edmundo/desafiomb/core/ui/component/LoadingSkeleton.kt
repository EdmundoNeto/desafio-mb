package br.com.edmundo.desafiomb.core.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import br.com.edmundo.desafiomb.core.ui.testing.TestTags
import br.com.edmundo.desafiomb.core.ui.theme.Spacing

private const val SKELETON_MIN_ALPHA = 0.3f
private const val SKELETON_MAX_ALPHA = 0.9f
private const val SKELETON_ANIMATION_DURATION_MS = 700
private const val SKELETON_LINE_WIDTH_FRACTION = 0.6f
private val SkeletonLineHeight = 20.dp
private val SkeletonCornerRadius = 4.dp

@Composable
fun LoadingSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 8,
) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = SKELETON_MIN_ALPHA,
        targetValue = SKELETON_MAX_ALPHA,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = SKELETON_ANIMATION_DURATION_MS, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "skeletonAlpha",
    )
    val baseColor =
        MaterialTheme.colorScheme.onSurface
            .copy(alpha = alpha)
            .compositeOver(MaterialTheme.colorScheme.surface)

    Column(modifier = modifier.testTag(TestTags.LIST_SKELETON)) {
        repeat(itemCount) {
            Column(modifier = Modifier.fillMaxWidth().padding(Spacing.md)) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(SKELETON_LINE_WIDTH_FRACTION)
                            .height(SkeletonLineHeight)
                            .background(baseColor, RoundedCornerShape(SkeletonCornerRadius)),
                )
            }
        }
    }
}
