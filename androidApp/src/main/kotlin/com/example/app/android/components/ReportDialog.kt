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
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.android.network.ApiClient
import com.example.app.android.network.models.ReportReason
import com.example.app.android.theme.AppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportSheet(
    postId: String,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    var reasons by remember { mutableStateOf<List<ReportReason>>(emptyList()) }
    var selectedReason by remember { mutableStateOf<String?>(null) }
    var detail by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        ApiClient.getReportReasons().fold(
            onSuccess = { reasons = it; isLoading = false },
            onFailure = { isLoading = false }
        )
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
                    text = "Denunciar publica\u00e7\u00e3o",
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

                // Reason options
                reasons.forEach { reason ->
                    val isSelected = selectedReason == reason.key

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason.key }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) colors.destructive else colors.surface)
                                .then(
                                    if (!isSelected) Modifier.background(colors.divider, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, null, tint = colors.onDestructive, modifier = Modifier.size(14.dp))
                            }
                        }

                        Spacer(Modifier.width(14.dp))

                        Text(
                            text = reason.label.ifBlank { reason.key.replace("_", " ").replaceFirstChar { it.uppercase() } },
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Detail input
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surface)
                        .padding(16.dp)
                ) {
                    if (detail.isEmpty()) {
                        Text("Detalhes (opcional)", fontSize = 14.sp, color = colors.textSecondary)
                    }
                    BasicTextField(
                        value = detail,
                        onValueChange = { detail = it },
                        textStyle = TextStyle(fontSize = 14.sp, color = colors.textPrimary),
                        cursorBrush = SolidColor(colors.accent),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                }

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        fontSize = 12.sp,
                        color = colors.destructive,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Submit button
                Button(
                    onClick = {
                        val reason = selectedReason ?: return@Button
                        isSending = true
                        errorMsg = null
                        scope.launch {
                            ApiClient.reportPost(
                                postId = postId,
                                reason = reason,
                                detail = detail.ifBlank { null }
                            ).fold(
                                onSuccess = {
                                    isSending = false
                                    onDismiss()
                                },
                                onFailure = { e ->
                                    isSending = false
                                    errorMsg = if (e.message?.contains("409") == true)
                                        "Voc\u00ea j\u00e1 denunciou esta publica\u00e7\u00e3o"
                                    else
                                        e.message ?: "Erro ao denunciar"
                                }
                            )
                        }
                    },
                    enabled = selectedReason != null && !isSending,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.destructive,
                        contentColor = colors.onDestructive,
                        disabledContainerColor = colors.destructive.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 20.dp)
                ) {
                    if (isSending) {
                        CircularProgressIndicator(color = colors.onDestructive, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    } else {
                        Text("Denunciar", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
