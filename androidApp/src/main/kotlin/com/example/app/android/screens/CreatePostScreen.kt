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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.android.components.SafeScreen
import com.example.app.android.network.ApiClient
import com.example.app.android.services.LocationService
import com.example.app.android.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CreatePostScreen(
    onDismiss: () -> Unit,
    onPostCreated: () -> Unit = {}
) {
    SafeScreen(
        backgroundColor = AppTheme.colors.background,
        modifier = Modifier.clipToBounds()
    ) {
        val colors = AppTheme.colors
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        var text by remember { mutableStateOf("") }
        var isPublishing by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var isRecording by remember { mutableStateOf(false) }
        var recordingSeconds by remember { mutableIntStateOf(0) }

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
                    text = "Novo post",
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
                        contentDescription = "Fechar",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Box(Modifier.fillMaxWidth().height(0.5.dp).background(colors.divider))

            // Text field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = "No que voc\u00ea est\u00e1 pensando?",
                        fontSize = 16.sp,
                        color = colors.textSecondary
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = TextStyle(fontSize = 16.sp, color = colors.textPrimary),
                    cursorBrush = SolidColor(colors.accent)
                )
            }

            // Error
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    fontSize = 12.sp,
                    color = colors.destructive,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }

            // Bottom bar
            Box(Modifier.fillMaxWidth().height(0.5.dp).background(colors.divider))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                            ) {
                                IconButton(
                                    onClick = { isRecording = false },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(Icons.Default.Close, "Cancelar", tint = colors.textSecondary, modifier = Modifier.size(20.dp))
                                }
                                AudioWaveBars(barColor = colors.accent, modifier = Modifier.weight(1f))
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
                            onClick = {
                                if (text.isBlank()) return@Button
                                isPublishing = true
                                errorMessage = null
                                scope.launch {
                                    val locationService = LocationService.getInstance(context)
                                    locationService.fetch()
                                    // Wait briefly for location
                                    delay(500)
                                    val loc = locationService.cachedLocation
                                    if (loc == null) {
                                        errorMessage = "Localiza\u00e7\u00e3o n\u00e3o dispon\u00edvel. Verifique as permiss\u00f5es."
                                        isPublishing = false
                                        return@launch
                                    }
                                    ApiClient.createPost(text, loc.latitude, loc.longitude).fold(
                                        onSuccess = {
                                            isPublishing = false
                                            onPostCreated()
                                            onDismiss()
                                        },
                                        onFailure = { e ->
                                            isPublishing = false
                                            errorMessage = e.message ?: "Erro ao publicar"
                                        }
                                    )
                                }
                            },
                            enabled = text.isNotBlank() && !isPublishing,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accent,
                                contentColor = colors.onAccent,
                                disabledContainerColor = colors.accent.copy(alpha = 0.3f),
                                disabledContentColor = colors.onAccent.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (isPublishing) {
                                CircularProgressIndicator(
                                    color = colors.onAccent,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Publicando...", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            } else {
                                Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Publicar", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(Modifier.width(10.dp))

                AnimatedContent(
                    targetState = isRecording,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                    label = "micSendTransition"
                ) { recording ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (recording) colors.accent else colors.surface)
                            .clickable {
                                if (isRecording) {
                                    isRecording = false
                                } else {
                                    isRecording = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (recording) {
                            Icon(Icons.AutoMirrored.Filled.Send, "Enviar \u00e1udio", tint = colors.onAccent, modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Default.Mic, "Gravar \u00e1udio", tint = colors.textSecondary, modifier = Modifier.size(20.dp))
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
