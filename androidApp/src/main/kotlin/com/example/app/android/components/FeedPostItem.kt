package com.example.app.android.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.app.android.R
import com.example.app.android.theme.AppTheme

data class FeedPost(
    val id: Int,
    val title: String,
    val location: String,
    val timeAgo: String,
    val distanceMeters: Int,
    val category: String,
    val subcategory: String? = null,
    val content: String,
    val mediaUrls: List<String> = emptyList(),
    val mediaType: MediaType = MediaType.NONE,
    val signalCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    val viewCount: Int = 0
)

enum class MediaType {
    NONE, PHOTO, VIDEO
}

@Composable
fun FeedPostItem(
    post: FeedPost,
    onSignalClick: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

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
            // -- Header: titulo + meta info --
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = post.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "\u00B7",
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = post.timeAgo,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // -- Linha: local + distancia + categoria --
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = post.location,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "\u00B7",
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formatDistance(post.distanceMeters),
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                CategoryBadge(
                    category = post.category,
                    subcategory = post.subcategory
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // -- Conteudo do post --
            Text(
                text = post.content,
                fontSize = 15.sp,
                color = colors.textPrimary,
                lineHeight = 21.sp,
                letterSpacing = 0.1.sp
            )

            // -- Midia (fotos/videos) --
            if (post.mediaUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                PostMedia(
                    urls = post.mediaUrls,
                    mediaType = post.mediaType
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // -- Barra de interacoes: sinal, comentar, compartilhar --
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InteractionButton(
                    icon = ImageVector.vectorResource(R.drawable.ic_signal),
                    label = if (post.signalCount > 0) formatCount(post.signalCount) else "Sinal",
                    tint = if (post.signalCount > 0) colors.accent else colors.textSecondary,
                    onClick = onSignalClick
                )
                Spacer(modifier = Modifier.width(20.dp))
                InteractionButton(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    label = if (post.commentCount > 0) formatCount(post.commentCount) else "Comentar",
                    tint = colors.textSecondary,
                    onClick = onCommentClick
                )
                Spacer(modifier = Modifier.width(20.dp))
                InteractionButton(
                    icon = Icons.Outlined.Share,
                    label = if (post.shareCount > 0) formatCount(post.shareCount) else "Compartilhar",
                    tint = colors.textSecondary,
                    onClick = onShareClick
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // -- Stats: views, compartilhamentos, sinais --
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = colors.textSecondary.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${formatCount(post.viewCount)} visualizações",
                    fontSize = 11.sp,
                    color = colors.textSecondary.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${formatCount(post.shareCount)} compartilhamentos",
                    fontSize = 11.sp,
                    color = colors.textSecondary.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${formatCount(post.signalCount)} sinais",
                    fontSize = 11.sp,
                    color = colors.textSecondary.copy(alpha = 0.6f)
                )
            }
        }

        // -- Divider --
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(colors.divider.copy(alpha = 0.4f))
        )
    }
}

@Composable
private fun CategoryBadge(
    category: String,
    subcategory: String?
) {
    val colors = AppTheme.colors
    val label = if (subcategory != null) "$category - $subcategory" else category

    val badgeColor = when (category.lowercase()) {
        "ocorrência", "ocorrencia" -> Color(0xFFFF3B30).copy(alpha = 0.12f)
        "alerta" -> Color(0xFFFF9500).copy(alpha = 0.12f)
        "comunidade" -> Color(0xFF34C759).copy(alpha = 0.12f)
        "informação", "informacao" -> Color(0xFF007AFF).copy(alpha = 0.12f)
        "comércio", "comercio" -> Color(0xFFAF52DE).copy(alpha = 0.12f)
        else -> colors.avatarBackground
    }

    val textColor = when (category.lowercase()) {
        "ocorrência", "ocorrencia" -> Color(0xFFFF3B30)
        "alerta" -> Color(0xFFFF9500)
        "comunidade" -> Color(0xFF34C759)
        "informação", "informacao" -> Color(0xFF007AFF)
        "comércio", "comercio" -> Color(0xFFAF52DE)
        else -> colors.textSecondary
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(badgeColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
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
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = tint,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PostMedia(
    urls: List<String>,
    mediaType: MediaType
) {
    val colors = AppTheme.colors

    if (urls.size == 1) {
        AsyncImage(
            model = urls.first(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.avatarBackground),
            contentScale = ContentScale.Crop
        )
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            urls.take(3).forEachIndexed { index, url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.avatarBackground),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

private fun formatDistance(meters: Int): String {
    return if (meters >= 1000) {
        val km = meters / 1000.0
        if (km % 1.0 == 0.0) "${km.toInt()}km" else "${"%.1f".format(km)}km"
    } else {
        "${meters}m"
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> "${"%.1f".format(count / 1_000_000.0)}M"
        count >= 1_000 -> "${"%.1f".format(count / 1_000.0)}k"
        else -> count.toString()
    }
}
