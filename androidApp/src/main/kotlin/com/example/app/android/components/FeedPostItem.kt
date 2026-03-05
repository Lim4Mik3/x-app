package com.example.app.android.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import com.example.app.android.theme.AppTheme

@Composable
fun FeedPostItem(
    post: FeedPost,
    distanceText: String?,
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
            // Header row: type badge + time + distance + 3-dot menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type badge
                TypeBadge(type = post.typeLabel)

                if (post.categoryLabel != null) {
                    Spacer(Modifier.width(6.dp))
                    Text("\u00B7", fontSize = 12.sp, color = colors.textSecondary)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = post.categoryLabel!!,
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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

            // Distance row
            if (distanceText != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = distanceText,
                    fontSize = 11.sp,
                    color = colors.textSecondary.copy(alpha = 0.7f)
                )
            }

            Spacer(Modifier.height(10.dp))

            // Post content
            Text(
                text = post.originalText,
                fontSize = 15.sp,
                color = colors.textPrimary,
                lineHeight = 21.sp,
                letterSpacing = 0.1.sp
            )

            Spacer(Modifier.height(12.dp))

            // Interaction buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InteractionButton(
                    icon = ImageVector.vectorResource(R.drawable.ic_signal),
                    label = "Sinal",
                    tint = colors.textSecondary,
                    onClick = onSignalClick
                )
                Spacer(Modifier.width(20.dp))
                InteractionButton(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    label = "Comentar",
                    tint = colors.textSecondary,
                    onClick = onCommentClick
                )
                Spacer(Modifier.width(20.dp))
                InteractionButton(
                    icon = Icons.Outlined.Share,
                    label = "Compartilhar",
                    tint = colors.textSecondary,
                    onClick = onShareClick
                )
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

@Composable
private fun TypeBadge(type: String) {
    val badgeColor = when (type.lowercase()) {
        "alerta" -> Color(0xFFFF9500).copy(alpha = 0.12f)
        "relato" -> Color(0xFF007AFF).copy(alpha = 0.12f)
        "den\u00fancia", "denuncia" -> Color(0xFFFF3B30).copy(alpha = 0.12f)
        "informa\u00e7\u00e3o", "informacao" -> Color(0xFF007AFF).copy(alpha = 0.12f)
        "com\u00e9rcio", "comercio" -> Color(0xFFAF52DE).copy(alpha = 0.12f)
        "pedido" -> Color(0xFF34C759).copy(alpha = 0.12f)
        "reclama\u00e7\u00e3o", "reclamacao" -> Color(0xFFFF9500).copy(alpha = 0.12f)
        else -> Color(0xFF8E8E93).copy(alpha = 0.12f)
    }

    val textColor = when (type.lowercase()) {
        "alerta" -> Color(0xFFFF9500)
        "relato" -> Color(0xFF007AFF)
        "den\u00fancia", "denuncia" -> Color(0xFFFF3B30)
        "informa\u00e7\u00e3o", "informacao" -> Color(0xFF007AFF)
        "com\u00e9rcio", "comercio" -> Color(0xFFAF52DE)
        "pedido" -> Color(0xFF34C759)
        "reclama\u00e7\u00e3o", "reclamacao" -> Color(0xFFFF9500)
        else -> Color(0xFF8E8E93)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(badgeColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = type,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            maxLines = 1
        )
    }
}

@Composable
private fun InteractionButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
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
