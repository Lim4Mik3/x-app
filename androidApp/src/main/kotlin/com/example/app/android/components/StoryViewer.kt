package com.example.app.android.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

enum class StoryMediaType { PHOTO, VIDEO }

data class StoryContent(
    val id: String,
    val text: String,
    val timeAgo: String,
    val mediaType: StoryMediaType = StoryMediaType.PHOTO,
    val durationMs: Long? = null
) {
    val displayDurationMs: Long
        get() = when (mediaType) {
            StoryMediaType.PHOTO -> 8000L
            StoryMediaType.VIDEO -> durationMs ?: 8000L
        }
}

@Composable
fun StoryViewer(
    stories: List<StoryItem>,
    initialStoryIndex: Int,
    contentsForStory: (StoryItem) -> List<StoryContent>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStoryIndex by remember { mutableIntStateOf(initialStoryIndex) }
    var currentContentIndex by remember { mutableIntStateOf(0) }
    val progress = remember { Animatable(0f) }
    val dragOffset = remember { Animatable(0f) }
    var screenWidth by remember { mutableFloatStateOf(1f) }
    val scope = rememberCoroutineScope()
    var isDragging by remember { mutableStateOf(false) }

    val story = stories[currentStoryIndex]
    val contents = remember(currentStoryIndex) { contentsForStory(stories[currentStoryIndex]) }
    val canGoNextStory = currentStoryIndex < stories.lastIndex
    val canGoPrevStory = currentStoryIndex > 0
    val swipeThreshold = 0.3f
    val swipeProgress = if (screenWidth > 0) dragOffset.value / screenWidth else 0f

    // Animate progress for current content
    LaunchedEffect(currentStoryIndex, currentContentIndex, isDragging) {
        if (isDragging) return@LaunchedEffect
        dragOffset.snapTo(0f)
        val duration = contents.getOrNull(currentContentIndex)?.displayDurationMs ?: 8000L
        progress.snapTo(0f)
        progress.animateTo(1f, tween(duration.toInt(), easing = LinearEasing))
        // Auto-advance
        if (currentContentIndex < contents.lastIndex) {
            currentContentIndex++
        } else if (currentStoryIndex < stories.lastIndex) {
            currentStoryIndex++
            currentContentIndex = 0
        } else {
            onDismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { screenWidth = it.width.toFloat() }
            .windowInsetsPadding(WindowInsets.statusBars)
            .pointerInput(currentStoryIndex) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    var dragX = 0f

                    val drag = awaitHorizontalTouchSlopOrCancellation(down.id) { change, over ->
                        dragX = over
                        change.consume()
                    }

                    if (drag == null) {
                        // Tap
                        val tapX = down.position.x
                        if (tapX < size.width / 2) {
                            if (currentContentIndex > 0) {
                                currentContentIndex--
                            } else if (canGoPrevStory) {
                                currentStoryIndex--
                                currentContentIndex = 0
                            }
                        } else {
                            if (currentContentIndex < contents.lastIndex) {
                                currentContentIndex++
                            } else if (canGoNextStory) {
                                currentStoryIndex++
                                currentContentIndex = 0
                            } else {
                                onDismiss()
                            }
                        }
                        return@awaitEachGesture
                    }

                    // Drag started
                    isDragging = true
                    scope.launch { dragOffset.snapTo(dragX) }

                    horizontalDrag(drag.id) { change ->
                        val delta = change.positionChange().x
                        dragX += delta
                        change.consume()
                        // Block movement at boundaries (no rubber band)
                        val clamped = when {
                            dragX > 0 && !canGoPrevStory -> 0f
                            dragX < 0 && !canGoNextStory -> 0f
                            else -> dragX
                        }
                        scope.launch { dragOffset.snapTo(clamped) }
                    }

                    // Drag ended
                    val normalized = dragOffset.value / screenWidth
                    scope.launch {
                        when {
                            normalized < -swipeThreshold && canGoNextStory -> {
                                // Animate out, then switch
                                dragOffset.animateTo(-screenWidth, tween(180))
                                // Reset offset BEFORE changing index to avoid flicker
                                dragOffset.snapTo(0f)
                                progress.snapTo(0f)
                                currentStoryIndex++
                                currentContentIndex = 0
                            }
                            normalized > swipeThreshold && canGoPrevStory -> {
                                dragOffset.animateTo(screenWidth, tween(180))
                                dragOffset.snapTo(0f)
                                progress.snapTo(0f)
                                currentStoryIndex--
                                currentContentIndex = 0
                            }
                            else -> dragOffset.animateTo(0f, tween(200))
                        }
                        isDragging = false
                    }
                }
            }
    ) {
        // Previous story (behind, when swiping right)
        if (canGoPrevStory && dragOffset.value > 0) {
            val bp = kotlin.math.abs(swipeProgress)
            StoryPage(
                story = stories[currentStoryIndex - 1],
                contents = contentsForStory(stories[currentStoryIndex - 1]),
                contentIndex = 0,
                progress = 0f,
                onDismiss = onDismiss,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = 0.4f + bp * 0.6f
                    }
            )
        }

        // Next story (behind, when swiping left)
        if (canGoNextStory && dragOffset.value < 0) {
            val bp = kotlin.math.abs(swipeProgress)
            StoryPage(
                story = stories[currentStoryIndex + 1],
                contents = contentsForStory(stories[currentStoryIndex + 1]),
                contentIndex = 0,
                progress = 0f,
                onDismiss = onDismiss,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = 0.4f + bp * 0.6f
                    }
            )
        }

        // Current story (on top, slides + tilts away)
        StoryPage(
            story = story,
            contents = contents,
            contentIndex = currentContentIndex,
            progress = progress.value,
            onDismiss = onDismiss,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (dragOffset.value != 0f) {
                        translationX = dragOffset.value
                        rotationZ = swipeProgress * -3f
                    }
                }
        )
    }
}

@Composable
private fun StoryPage(
    story: StoryItem,
    contents: List<StoryContent>,
    contentIndex: Int,
    progress: Float,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentContent = contents.getOrNull(contentIndex)

    Box(modifier = modifier.background(Color(0xFF1A1A2E))) {
        // Top section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Progress bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                contents.forEachIndexed { index, _ ->
                    val barProgress = when {
                        index < contentIndex -> 1f
                        index == contentIndex -> progress
                        else -> 0f
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(barProgress)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(story.color.copy(alpha = 0.25f))
                )

                Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                    Text(
                        text = "Acontecimentos de",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = story.label,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Content
        if (currentContent != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 32.dp)
            ) {
                Text(
                    text = currentContent.text,
                    fontSize = 18.sp,
                    color = Color.White,
                    lineHeight = 26.sp
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = currentContent.timeAgo,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}
