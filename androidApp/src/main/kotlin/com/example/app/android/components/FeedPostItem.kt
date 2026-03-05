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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.android.R
import com.example.app.android.network.models.FeedPost
import com.example.app.android.network.models.toHashtag
import com.example.app.android.theme.AppTheme

@Composable
fun FeedPostItem(
    post: FeedPost,
    onSignalClick: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onReportClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header: Type · Distance · Time  ···
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tColor = parseColor(post.typeColor)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(tColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = post.type,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = tColor,
                        maxLines = 1
                    )
                }

                if (post.distance != null) {
                    Spacer(Modifier.width(6.dp))
                    Text("\u00B7", fontSize = 12.sp, color = colors.textSecondary)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = post.distance,
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        maxLines = 1
                    )
                }

                Spacer(Modifier.weight(1f))

                Text(
                    text = post.timeAgo,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )

                // 3-dot menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            "Op\u00e7\u00f5es",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Denunciar",
                                    fontSize = 14.sp,
                                    color = colors.destructive
                                )
                            },
                            onClick = {
                                showMenu = false
                                onReportClick()
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Post content
            Text(
                text = post.content,
                fontSize = 15.sp,
                color = colors.textPrimary,
                lineHeight = 21.sp,
                letterSpacing = 0.1.sp
            )

            Spacer(Modifier.height(16.dp))

            // Categories as hashtags
            if (post.categories.isNotEmpty()) {
                Text(
                    text = post.categories.joinToString("  ") { toHashtag(it) },
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(10.dp))
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(colors.divider)
            )

            Spacer(Modifier.height(10.dp))

            // Interaction buttons (equal width)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onSignalClick)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    InteractionLabel(
                        icon = ImageVector.vectorResource(R.drawable.ic_signal),
                        label = if (post.signalsCount > 0) "${post.signalsCount}" else "Sinal",
                        tint = colors.textSecondary
                    )
                }
                Box(
                    Modifier
                        .width(0.5.dp)
                        .height(14.dp)
                        .background(colors.divider)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onCommentClick)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    InteractionLabel(
                        icon = Icons.Outlined.ChatBubbleOutline,
                        label = if (post.commentsCount > 0) "${post.commentsCount}" else "Comentar",
                        tint = colors.textSecondary
                    )
                }
                Box(
                    Modifier
                        .width(0.5.dp)
                        .height(14.dp)
                        .background(colors.divider)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onShareClick)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    InteractionLabel(
                        icon = Icons.Outlined.Share,
                        label = "Compartilhar",
                        tint = colors.textSecondary
                    )
                }
            }
        }

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(colors.divider.copy(alpha = 0.4f))
        )
    }
}

private val defaultTypeColor = Color(0xFF8E8E93)

private fun parseColor(hex: String?): Color {
    if (hex.isNullOrBlank()) return defaultTypeColor
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        defaultTypeColor
    }
}

@Composable
private fun InteractionLabel(
    icon: ImageVector,
    label: String,
    tint: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(18.dp),
            tint = tint
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = tint,
            fontWeight = FontWeight.Medium
        )
    }
}
