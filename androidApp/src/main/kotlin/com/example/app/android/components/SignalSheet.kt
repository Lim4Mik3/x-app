package com.example.app.android.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.android.network.ApiClient
import com.example.app.android.network.models.PostSignals
import com.example.app.android.network.models.SignalKey
import com.example.app.android.theme.AppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignalSheet(
    postId: String,
    postType: String,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    var signalKeys by remember { mutableStateOf<List<SignalKey>>(emptyList()) }
    var postSignals by remember { mutableStateOf<PostSignals?>(null) }
    var selectedKey by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }

    LaunchedEffect(postId) {
        isLoading = true
        // Fetch signal keys and current post signals in parallel
        val keysResult = ApiClient.getSignalKeys(postType.ifBlank { null })
        val signalsResult = ApiClient.getPostSignals(postId)

        keysResult.onSuccess { signalKeys = it }
        signalsResult.onSuccess { ps ->
            postSignals = ps
            // Pre-select user's existing signal
            selectedKey = ps.userSignals.firstOrNull()
        }
        isLoading = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Confirmar sinal",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, "Fechar", tint = colors.textSecondary, modifier = Modifier.size(20.dp))
                }
            }

            Box(Modifier.fillMaxWidth().height(0.5.dp).background(colors.divider))

            if (isLoading) {
                Box(
                    Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.accent, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                }
            } else {
                Spacer(Modifier.height(8.dp))

                // Signal options
                signalKeys.forEach { signal ->
                    val isSelected = selectedKey == signal.key
                    val existingCount = postSignals?.signals?.get(signal.key) ?: 0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedKey = if (isSelected) null else signal.key }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Radio indicator
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) colors.accent else colors.surface)
                                .then(
                                    if (!isSelected) Modifier.background(colors.divider, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, null, tint = colors.onAccent, modifier = Modifier.size(14.dp))
                            }
                        }

                        Spacer(Modifier.width(14.dp))

                        Text(
                            text = signal.label.ifBlank { signal.key.replace("_", " ").replaceFirstChar { it.uppercase() } },
                            fontSize = 15.sp,
                            color = colors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        if (existingCount > 0) {
                            Text(
                                text = "$existingCount",
                                fontSize = 13.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Confirm button
                Button(
                    onClick = {
                        val key = selectedKey ?: return@Button
                        isSending = true
                        scope.launch {
                            val userAlreadySignaled = postSignals?.userSignals?.contains(key) == true
                            if (userAlreadySignaled) {
                                ApiClient.removeSignal(postId, key)
                            } else {
                                // Remove old signal first if exists
                                postSignals?.userSignals?.firstOrNull()?.let {
                                    ApiClient.removeSignal(postId, it)
                                }
                                ApiClient.addSignal(postId, key)
                            }
                            isSending = false
                            onDismiss()
                        }
                    },
                    enabled = selectedKey != null && !isSending,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                        contentColor = colors.onAccent,
                        disabledContainerColor = colors.accent.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 20.dp)
                ) {
                    if (isSending) {
                        CircularProgressIndicator(color = colors.onAccent, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    } else {
                        Text("Confirmar", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
