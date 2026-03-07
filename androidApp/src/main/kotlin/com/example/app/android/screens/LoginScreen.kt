package com.example.app.android.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp as colorLerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.android.R
import com.example.app.android.network.ApiClient
import com.example.app.android.network.TokenManager
import com.example.app.android.theme.AppTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

// TODO: Replace with your Google Cloud web client ID
private const val GOOGLE_WEB_CLIENT_ID = "YOUR_GOOGLE_WEB_CLIENT_ID.apps.googleusercontent.com"

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onDismiss: (() -> Unit)? = null
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(GOOGLE_WEB_CLIENT_ID)
            .requestEmail()
            .build()
    }

    val googleClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    .getResult(ApiException::class.java)
                val idToken = account.idToken

                if (idToken != null) {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        val apiResult = ApiClient.socialLogin("google", idToken)
                        apiResult.fold(
                            onSuccess = { auth ->
                                TokenManager.saveAuth(auth.accessToken, auth.refreshToken)
                                TokenManager.userId = auth.user.id
                                TokenManager.displayName = auth.user.displayName ?: auth.user.email
                                isLoading = false
                                onLoginSuccess()
                            },
                            onFailure = { e ->
                                isLoading = false
                                errorMessage = e.message ?: "Erro ao entrar"
                            }
                        )
                    }
                } else {
                    errorMessage = "Token n\u00e3o recebido do Google"
                }
            } catch (e: Exception) {
                errorMessage = "Erro na autentica\u00e7\u00e3o do Google"
            }
        }
    }

    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF2A2A2E).copy(alpha = 0.45f)
                  else Color(0xFFE0E0E5).copy(alpha = 0.30f)

    val infiniteTransition = rememberInfiniteTransition(label = "bg")

    // Movement animations — faster, more central paths
    val move1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5000), RepeatMode.Reverse),
        label = "m1"
    )
    val move2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6500), RepeatMode.Reverse),
        label = "m2"
    )
    val move3 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(7500), RepeatMode.Reverse),
        label = "m3"
    )

    // Size pulsing — significant variation
    val size1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Reverse),
        label = "s1"
    )
    val size2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5500), RepeatMode.Reverse),
        label = "s2"
    )
    val size3 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3500), RepeatMode.Reverse),
        label = "s3"
    )

    // Color cycling — each blob shifts between the 3 colors at different speeds
    val colorAnim1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(8000), RepeatMode.Reverse),
        label = "c1"
    )
    val colorAnim2 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(10000), RepeatMode.Reverse),
        label = "c2"
    )
    val colorAnim3 by infiniteTransition.animateFloat(
        initialValue = 2f, targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(7000), RepeatMode.Reverse),
        label = "c3"
    )

    val blue = Color(0xFF2563EB)
    val purple = Color(0xFF9333EA)
    val pink = Color(0xFFEC4899)
    val palette = listOf(blue, purple, pink)

    fun cycleColor(anim: Float, alpha: Float): Color {
        val wrapped = anim % 3f
        val idx = wrapped.toInt() % 3
        val frac = wrapped - wrapped.toInt()
        val from = palette[idx]
        val to = palette[(idx + 1) % 3]
        return colorLerp(from, to, frac).copy(alpha = alpha)
    }

    // Full-screen box (behind status bar) — consumes all touches, tap outside card dismisses
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss?.invoke() }
    ) {
        // Animated blobs with radial gradient (works on all API levels)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Blob 1: blue — center ↔ top-right
            val c1 = Offset(lerp(w * 0.4f, w * 0.85f, move1), lerp(h * 0.4f, h * 0.1f, move1))
            val r1 = lerp(350f, 550f, size1)
            drawCircle(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(cycleColor(colorAnim1, 0.45f), Color.Transparent),
                    center = c1, radius = r1
                ),
                radius = r1, center = c1
            )

            // Blob 2: purple — center ↔ bottom-left
            val c2 = Offset(lerp(w * 0.55f, w * 0.1f, move2), lerp(h * 0.45f, h * 0.8f, move2))
            val r2 = lerp(320f, 520f, size2)
            drawCircle(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(cycleColor(colorAnim2, 0.40f), Color.Transparent),
                    center = c2, radius = r2
                ),
                radius = r2, center = c2
            )

            // Blob 3: pink — center ↔ bottom-right
            val c3 = Offset(lerp(w * 0.5f, w * 0.9f, move3), lerp(h * 0.5f, h * 0.85f, move3))
            val r3 = lerp(300f, 500f, size3)
            drawCircle(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(cycleColor(colorAnim3, 0.35f), Color.Transparent),
                    center = c3, radius = r3
                ),
                radius = r3, center = c3
            )
        }

        // Content with safe area padding
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {

            // Card — clickable to consume touches (prevent dismiss when tapping card)
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surface)
                    .border(0.5.dp, colors.divider, RoundedCornerShape(20.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* consume touch */ }
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(R.string.login_title),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.login_subtitle),
                    fontSize = 16.sp,
                    color = colors.textSecondary
                )

                HorizontalDivider(
                    modifier = Modifier.padding(top = 24.dp),
                    thickness = 0.5.dp,
                    color = colors.divider
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.login_header),
                    fontSize = 14.sp,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Google button
                SocialButton(
                    label = stringResource(R.string.login_google),
                    isLoading = isLoading,
                    onClick = {
                        // TODO: restore real auth when tokens are integrated
                        onLoginSuccess()
                    }
                )

                // Error message
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 13.sp,
                        color = colors.destructive,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(top = 20.dp),
                    thickness = 0.5.dp,
                    color = colors.divider
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = stringResource(R.string.login_community),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary.copy(alpha = if (isDark) 0.6f else 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.login_terms),
                    fontSize = 11.sp,
                    color = colors.textSecondary.copy(alpha = if (isDark) 0.5f else 0.75f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Loading overlay
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colors.accent,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        } // end safe-area Box
    } // end fullscreen Box
}

private fun lerp(start: Float, end: Float, fraction: Float): Float {
    return start + (end - start) * fraction
}

@Composable
private fun SocialButton(
    label: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    val isDark = isSystemInDarkTheme()
    val btnBg = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.04f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(0.5.dp, colors.divider, RoundedCornerShape(12.dp))
            .background(btnBg)
            .clickable(enabled = !isLoading, onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = colors.textSecondary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary
        )
    }
}
