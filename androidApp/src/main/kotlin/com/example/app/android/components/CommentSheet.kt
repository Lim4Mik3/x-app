package com.example.app.android.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import com.example.app.android.network.TokenManager
import com.example.app.android.network.models.Comment
import com.example.app.android.theme.AppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentSheet(
    postId: String,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var commentText by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<Comment?>(null) }
    var isSending by remember { mutableStateOf(false) }

    LaunchedEffect(postId) {
        isLoading = true
        ApiClient.getComments(postId).fold(
            onSuccess = { comments = it; isLoading = false },
            onFailure = { isLoading = false }
        )
    }

    fun refreshComments() {
        scope.launch {
            ApiClient.getComments(postId).onSuccess { comments = it }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.85f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                    text = "Coment\u00e1rios",
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

            // Content
            if (isLoading) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.accent, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                }
            } else if (comments.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Nenhum coment\u00e1rio ainda", fontSize = 14.sp, color = colors.textSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(comments, key = { it.id }) { comment ->
                        CommentItem(
                            comment = comment,
                            onReply = { replyingTo = it },
                            onVote = { id, type ->
                                scope.launch {
                                    ApiClient.voteComment(id, type)
                                    refreshComments()
                                }
                            },
                            onDelete = { id ->
                                scope.launch {
                                    ApiClient.deleteComment(id)
                                    refreshComments()
                                }
                            }
                        )
                    }
                }
            }

            // Reply indicator
            if (replyingTo != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surface)
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Respondendo a ${replyingTo!!.authorName}",
                        fontSize = 12.sp,
                        color = colors.accent,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { replyingTo = null },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Default.Close, null, tint = colors.textSecondary, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // Input
            Box(Modifier.fillMaxWidth().height(0.5.dp).background(colors.divider))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surface)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    if (commentText.isEmpty()) {
                        Text("Adicionar coment\u00e1rio...", fontSize = 14.sp, color = colors.textSecondary)
                    }
                    BasicTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        textStyle = TextStyle(fontSize = 14.sp, color = colors.textPrimary),
                        cursorBrush = SolidColor(colors.accent),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (commentText.isNotBlank() && !isSending) {
                            val text = commentText
                            val parentId = replyingTo?.id
                            commentText = ""
                            replyingTo = null
                            isSending = true
                            scope.launch {
                                ApiClient.addComment(
                                    postId = postId,
                                    text = text,
                                    authorName = TokenManager.displayName ?: "An\u00f4nimo",
                                    parentId = parentId
                                )
                                refreshComments()
                                isSending = false
                            }
                        }
                    },
                    enabled = commentText.isNotBlank() && !isSending,
                    modifier = Modifier.size(36.dp)
                ) {
                    if (isSending) {
                        CircularProgressIndicator(color = colors.accent, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    } else {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            "Enviar",
                            tint = if (commentText.isNotBlank()) colors.accent else colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentItem(
    comment: Comment,
    onReply: (Comment) -> Unit,
    onVote: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    isReply: Boolean = false
) {
    val colors = AppTheme.colors
    val currentUserId = TokenManager.userId

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isReply) 40.dp else 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = if (comment.replies.isEmpty() && !isReply) 12.dp else 4.dp
            )
    ) {
        // Author + time
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = comment.authorName.ifBlank { "An\u00f4nimo" },
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
            Spacer(Modifier.width(6.dp))
            Text("\u00B7", fontSize = 12.sp, color = colors.textSecondary)
            Spacer(Modifier.width(6.dp))
            Text(comment.timeAgo, fontSize = 11.sp, color = colors.textSecondary)
        }

        Spacer(Modifier.height(4.dp))

        // Text
        Text(
            text = comment.text,
            fontSize = 14.sp,
            color = colors.textPrimary,
            lineHeight = 19.sp
        )

        Spacer(Modifier.height(6.dp))

        // Actions
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Vote up
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onVote(comment.id, "up") }
                    .padding(4.dp)
            ) {
                Icon(Icons.Default.KeyboardArrowUp, "Votar positivo", modifier = Modifier.size(16.dp), tint = colors.textSecondary)
                if (comment.upvotes > 0) {
                    Text("${comment.upvotes}", fontSize = 11.sp, color = colors.textSecondary)
                }
            }

            Spacer(Modifier.width(4.dp))

            // Vote down
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onVote(comment.id, "down") }
                    .padding(4.dp)
            ) {
                Icon(Icons.Default.KeyboardArrowDown, "Votar negativo", modifier = Modifier.size(16.dp), tint = colors.textSecondary)
                if (comment.downvotes > 0) {
                    Text("${comment.downvotes}", fontSize = 11.sp, color = colors.textSecondary)
                }
            }

            if (!isReply) {
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Responder",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onReply(comment) }
                        .padding(4.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            // Delete (if own comment - simplified check)
            IconButton(
                onClick = { onDelete(comment.id) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Delete, "Excluir", modifier = Modifier.size(14.dp), tint = colors.textSecondary.copy(alpha = 0.5f))
            }
        }

        // Replies
        if (comment.replies.isNotEmpty()) {
            Column(modifier = Modifier.animateContentSize()) {
                comment.replies.forEach { reply ->
                    CommentItem(
                        comment = reply,
                        onReply = onReply,
                        onVote = onVote,
                        onDelete = onDelete,
                        isReply = true
                    )
                }
            }
        }

        if (!isReply) {
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(0.5.dp).background(colors.divider.copy(alpha = 0.5f)))
        }
    }
}
