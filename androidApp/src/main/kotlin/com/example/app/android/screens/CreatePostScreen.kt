package com.example.app.android.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.app.android.R
import com.example.app.android.theme.AppTheme
import com.example.app.android.components.SafeScreen
import kotlinx.coroutines.delay

@Composable
fun CreatePostScreen(
    text: String = "",
    onTextChanged: (String) -> Unit = {},
    onDismiss: () -> Unit,
    onPublish: () -> Unit = {}
) {
    SafeScreen(
        backgroundColor = AppTheme.colors.background,
        modifier = Modifier.clipToBounds()
    ) {
            val colors = AppTheme.colors
            var isRecording by remember { mutableStateOf(false) }
            var recordingSeconds by remember { mutableIntStateOf(0) }

            // Timer for recording
            LaunchedEffect(isRecording) {
                if (isRecording) {
                    recordingSeconds = 0
                    while (true) {
                        delay(1000L)
                        recordingSeconds++
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.new_post),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                        letterSpacing = 0.2.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close),
                            tint = colors.textSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(colors.divider)
                )

                // Text field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = stringResource(R.string.post_placeholder),
                            fontSize = 16.sp,
                            color = colors.textSecondary
                        )
                    }
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChanged,
                        modifier = Modifier.fillMaxSize(),
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = colors.textPrimary
                        ),
                        cursorBrush = SolidColor(colors.accent)
                    )
                }

                // Bottom bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(colors.divider)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Main button: Publicar or Recording (X + waves + timer)
                    AnimatedContent(
                        targetState = isRecording,
                        transitionSpec = {
                            fadeIn(tween(300)) togetherWith fadeOut(tween(300)) using SizeTransform(clip = false)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        label = "publishRecordingTransition"
                    ) { recording ->
                        if (recording) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp)
                                ) {
                                    // X cancel button
                                    IconButton(
                                        onClick = { isRecording = false },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(R.string.cancel_recording),
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    // Waves filling the center
                                    AudioWaveBars(
                                        barColor = colors.accent,
                                        modifier = Modifier.weight(1f)
                                    )
                                    // Timer on the right
                                    Text(
                                        text = formatTime(recordingSeconds),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.textSecondary,
                                        modifier = Modifier.padding(start = 12.dp, end = 12.dp)
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = onPublish,
                                enabled = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.accent,
                                    contentColor = colors.onAccent,
                                    disabledContainerColor = colors.accent.copy(alpha = 0.3f),
                                    disabledContentColor = colors.onAccent.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.publish),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))

                    // Mic (start) / Send audio button
                    AnimatedContent(
                        targetState = isRecording,
                        transitionSpec = {
                            fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                        },
                        label = "micSendTransition"
                    ) { recording ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (recording) colors.accent else colors.surface)
                                .clickable {
                                    if (isRecording) {
                                        // TODO: enviar áudio
                                        isRecording = false
                                    } else {
                                        isRecording = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (recording) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = stringResource(R.string.send_audio),
                                    tint = colors.onAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = stringResource(R.string.record_audio),
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
}

@Composable
private fun AudioWaveBars(
    barColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val barCount = 20
    val infiniteTransition = rememberInfiniteTransition(label = "waveBars")

    val barHeights = List(barCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = 6f,
            targetValue = 22f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 400 + ((index % 7) * 80),
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = androidx.compose.animation.core.StartOffset((index % 7) * 120)
            ),
            label = "barHeight$index"
        )
    }

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        barHeights.forEach { heightAnim ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(heightAnim.value.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )
        }
    }
}

private fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "$mins:${secs.toString().padStart(2, '0')}"
}
