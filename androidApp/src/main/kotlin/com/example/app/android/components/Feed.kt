package com.example.app.android.components

import android.location.Location
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.android.network.models.FeedPost
import com.example.app.android.services.LocationService
import com.example.app.android.theme.AppTheme

@Composable
fun Feed(
    posts: List<FeedPost>,
    listState: LazyListState,
    isLoading: Boolean,
    userLocation: Location?,
    onSignalClick: (FeedPost) -> Unit,
    onCommentClick: (FeedPost) -> Unit,
    onShareClick: (FeedPost) -> Unit,
    onReportClick: (FeedPost) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    val colors = AppTheme.colors

    if (isLoading && posts.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                color = colors.accent,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
            )
        }
        return
    }

    if (!isLoading && posts.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Nenhuma publica\u00e7\u00e3o por perto",
                fontSize = 14.sp,
                color = colors.textSecondary
            )
        }
        return
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        items(posts, key = { it.id }) { post ->
            val distanceText = remember(post, userLocation) {
                if (userLocation != null && post.latitude != null && post.longitude != null) {
                    val dist = LocationService.distanceBetween(
                        userLocation.latitude, userLocation.longitude,
                        post.latitude, post.longitude
                    )
                    formatDistance(dist)
                } else null
            }

            FeedPostItem(
                post = post,
                distanceText = distanceText,
                onSignalClick = { onSignalClick(post) },
                onCommentClick = { onCommentClick(post) },
                onShareClick = { onShareClick(post) },
                onReportClick = { onReportClick(post) }
            )
        }

        // Load more trigger
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = colors.accent,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            // Trigger load more when reaching bottom
            onLoadMore()
        }
    }
}

private fun formatDistance(meters: Float): String {
    return when {
        meters < 100f -> "A menos de 100m"
        meters < 1000f -> "A ${meters.toInt()}m"
        else -> "A %.1fkm".format(meters / 1000f)
    }
}
