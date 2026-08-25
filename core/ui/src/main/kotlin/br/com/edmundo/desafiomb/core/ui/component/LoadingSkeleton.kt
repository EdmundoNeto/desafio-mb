package br.com.edmundo.desafiomb.core.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import br.com.edmundo.desafiomb.core.ui.testing.TestTags
import br.com.edmundo.desafiomb.core.ui.theme.Spacing

private const val SKELETON_MIN_ALPHA = 0.3f
private const val SKELETON_MAX_ALPHA = 0.9f
private const val SKELETON_ANIMATION_DURATION_MS = 700
private const val TITLE_LINE_WIDTH_FRACTION = 0.5f
private const val SUBTITLE_LINE_WIDTH_FRACTION = 0.3f
private const val ASSET_NAME_WIDTH_FRACTION = 0.45f
private val LineCornerRadius = 4.dp
private val TitleLineHeight = 16.dp
private val SubtitleLineHeight = 12.dp
private val TrailingValueWidth = 56.dp
private val AssetPriceWidth = 64.dp
private val BadgeWidth = 32.dp
private val BadgeHeight = 20.dp
private val ChevronPlaceholderSize = 20.dp

enum class SkeletonVariant {
    Card,
    DividedRow,
}

@Composable
fun LoadingSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 8,
    variant: SkeletonVariant = SkeletonVariant.Card,
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

    when (variant) {
        SkeletonVariant.Card ->
            Column(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .testTag(TestTags.LIST_SKELETON)
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                repeat(itemCount) { CardSkeletonItem(color = baseColor) }
            }

        SkeletonVariant.DividedRow ->
            Column(modifier = modifier.fillMaxWidth().testTag(TestTags.LIST_SKELETON)) {
                repeat(itemCount) { DividedRowSkeletonItem(color = baseColor) }
            }
    }
}

@Composable
private fun CardSkeletonItem(color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = Spacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShimmerBlock(
                color = color,
                shape = RoundedCornerShape(percent = 50),
                modifier = Modifier.size(width = BadgeWidth, height = BadgeHeight),
            )
            ShimmerBlock(
                color = color,
                shape = CircleShape,
                modifier = Modifier.padding(start = Spacing.sm).size(Spacing.avatarMedium),
            )
            Column(
                modifier = Modifier.padding(start = Spacing.md).weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                ShimmerBlock(
                    color = color,
                    modifier = Modifier.fillMaxWidth(TITLE_LINE_WIDTH_FRACTION).height(TitleLineHeight),
                )
                ShimmerBlock(
                    color = color,
                    modifier = Modifier.fillMaxWidth(SUBTITLE_LINE_WIDTH_FRACTION).height(SubtitleLineHeight),
                )
            }
            ShimmerBlock(
                color = color,
                modifier = Modifier.padding(start = Spacing.md).width(TrailingValueWidth).height(TitleLineHeight),
            )
            ShimmerBlock(
                color = color,
                shape = CircleShape,
                modifier = Modifier.padding(start = Spacing.xs).size(ChevronPlaceholderSize),
            )
        }
    }
}

@Composable
private fun DividedRowSkeletonItem(color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShimmerBlock(color = color, modifier = Modifier.fillMaxWidth(ASSET_NAME_WIDTH_FRACTION).height(TitleLineHeight))
            ShimmerBlock(color = color, modifier = Modifier.width(AssetPriceWidth).height(TitleLineHeight))
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = Spacing.md),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun ShimmerBlock(
    color: Color,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(LineCornerRadius),
) {
    Box(modifier = modifier.background(color, shape))
}
