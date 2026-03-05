package com.example.app.android.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.android.theme.AppTheme

data class StoryItem(
    val id: String,
    val label: String,
    val color: Color,
    val hasUnread: Boolean = true
)

@Composable
fun StoriesRow(
    stories: List<StoryItem>,
    onStoryClick: (StoryItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    val infiniteTransition = rememberInfiniteTransition(label = "stories")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(stories, key = { it.id }) { story ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(62.dp)
                    .clickable { onStoryClick(story) }
            ) {
                val borderBrush = Brush.sweepGradient(
                    listOf(
                        colors.accent,
                        colors.accent.copy(alpha = 0.7f),
                        colors.accent.copy(alpha = 0.7f),
                        colors.accent
                    )
                )

                Box(
                    modifier = Modifier
                        .size(55.dp)
                        .then(
                            if (story.hasUnread) {
                                Modifier.drawBehind {
                                    rotate(rotation) {
                                        drawCircle(
                                            brush = borderBrush,
                                            style = Stroke(width = 3.dp.toPx())
                                        )
                                    }
                                }
                            } else {
                                Modifier.drawBehind {
                                    drawCircle(
                                        color = colors.divider,
                                        style = Stroke(width = 1.dp.toPx())
                                    )
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(47.dp)
                            .clip(CircleShape)
                            .background(story.color.copy(alpha = 0.25f))
                    )
                }

                Spacer(Modifier.height(5.dp))

                Text(
                    text = story.label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
