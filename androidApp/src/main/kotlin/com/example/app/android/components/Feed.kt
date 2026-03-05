package com.example.app.android.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.android.network.models.FeedPost
import com.example.app.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Feed(
    posts: List<FeedPost>,
    listState: LazyListState,
    isLoading: Boolean,
    isRefreshing: Boolean = false,
    onSignalClick: (FeedPost) -> Unit,
    onCommentClick: (FeedPost) -> Unit,
    onShareClick: (FeedPost) -> Unit,
    onReportClick: (FeedPost) -> Unit,
    onStoryClick: (StoryItem) -> Unit = {},
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    val colors = AppTheme.colors

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier
    ) {
        if (isLoading && posts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = colors.accent,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else if (!isLoading && posts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Nenhuma publica\u00e7\u00e3o por perto",
                    fontSize = 14.sp,
                    color = colors.textSecondary
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding
            ) {
                // Stories section
                item(key = "stories") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        colors.accent.copy(alpha = 0.06f),
                                        colors.surface
                                    )
                                )
                            )
                    ) {
                        StoriesRow(
                            stories = mockStories,
                            onStoryClick = onStoryClick,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(colors.divider.copy(alpha = 0.4f))
                    )
                }

                items(posts, key = { it.id }) { post ->
                    FeedPostItem(
                        post = post,
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
    }
}

val mockStories = listOf(
    StoryItem("1", "Centro", Color(0xFFFF9500)),
    StoryItem("2", "Pinheiros", Color(0xFF007AFF)),
    StoryItem("3", "Vila Madalena", Color(0xFFFF3B30)),
    StoryItem("4", "Moema", Color(0xFF34C759)),
    StoryItem("5", "Itaim Bibi", Color(0xFFAF52DE)),
    StoryItem("6", "Consolação", Color(0xFFFF2D55)),
    StoryItem("7", "Liberdade", Color(0xFF5AC8FA)),
    StoryItem("8", "Bela Vista", Color(0xFF4CD964)),
)
