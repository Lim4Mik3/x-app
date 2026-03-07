package com.example.app.android.screens

import android.Manifest
import android.location.Geocoder
import android.location.Location
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.app.android.components.BottomTabBar
import com.example.app.android.components.CreatePostFab
import com.example.app.android.components.Feed
import com.example.app.android.components.Header
import com.example.app.android.components.ReportSheet
import com.example.app.android.components.SignalSheet
import com.example.app.android.components.Tab
import com.example.app.android.components.ToastOverlay
import com.example.app.android.components.rememberScrollAwareState
import com.example.app.android.network.SignalKeysCache
import com.example.app.android.navigation.OverlayRoute
import com.example.app.android.navigation.navigateOnce
import com.example.app.android.network.ApiClient
import com.example.app.android.network.TokenManager
import com.example.app.android.network.models.FeedPost
import com.example.app.android.services.LocationService
import com.example.app.android.theme.AppTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    isLoggedIn: Boolean = false,
    onLoginSuccess: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }

    // Navigation
    var selectedTab by remember { mutableStateOf(Tab.Home) }
    val navController = rememberNavController()
    val feedListState = rememberLazyListState()
    val scrollAwareState = rememberScrollAwareState()

    // Location
    val locationService = remember { LocationService.getInstance(context) }
    val userLocation by locationService.location.collectAsState()
    var locationName by remember { mutableStateOf("Carregando...") }

    // Feed state
    var feedPosts by remember { mutableStateOf<List<FeedPost>>(emptyList()) }
    var isFeedLoading by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var feedCursor by remember { mutableStateOf<String?>(null) }
    var hasMore by remember { mutableStateOf(true) }

    val effectiveLoggedIn = isLoggedIn || com.example.app.android.DevConfig.BYPASS_AUTH

    // Login overlay
    var showLoginOverlay by remember { mutableStateOf(false) }

    fun requireAuth(action: () -> Unit) {
        if (isLoggedIn || com.example.app.android.DevConfig.BYPASS_AUTH) action() else showLoginOverlay = true
    }

    // Interaction sheets
    var showSignalForPost by remember { mutableStateOf<FeedPost?>(null) }
    var showReportForPost by remember { mutableStateOf<FeedPost?>(null) }
    var showStoryViewer by remember { mutableStateOf<com.example.app.android.components.StoryItem?>(null) }
    var storyTransition by remember { mutableStateOf<com.example.app.android.components.StoryTapInfo?>(null) }
    var storyTransitionExpanded by remember { mutableStateOf(false) }

    // Location permissions
    val locationPermissions = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    )

    // Initialize cache and request permissions on launch
    LaunchedEffect(Unit) {
        SignalKeysCache.init(context)
        SignalKeysCache.refresh()
        // Fetch mock location immediately (no permission needed)
        locationService.fetch(forceRefresh = true)
        if (!locationPermissions.allPermissionsGranted) {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    // Fetch location when permissions granted (for real GPS)
    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            locationService.fetch(forceRefresh = true)
        }
    }

    // Reverse geocode for header
    LaunchedEffect(userLocation) {
        val loc = userLocation ?: return@LaunchedEffect
        try {
            @Suppress("DEPRECATION")
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
            val addr = addresses?.firstOrNull()
            locationName = addr?.subLocality ?: addr?.locality ?: addr?.subAdminArea ?: "Ao seu redor"
        } catch (_: Exception) {
            locationName = "Ao seu redor"
        }
    }

    // Load feed when location available
    fun loadFeed(refresh: Boolean = false) {
        val loc = userLocation ?: return
        if (isFeedLoading) return

        scope.launch {
            isFeedLoading = true
            if (refresh) {
                isRefreshing = true
                feedCursor = null
                hasMore = true
            }

            val cursor = if (refresh) null else feedCursor
            ApiClient.getFeed(loc.latitude, loc.longitude, limit = 20, cursor = cursor).fold(
                onSuccess = { response ->
                    feedPosts = if (refresh) response.posts else feedPosts + response.posts
                    feedCursor = response.nextCursor
                    hasMore = response.hasMore
                },
                onFailure = { /* silently handle */ }
            )
            isFeedLoading = false
            isRefreshing = false
        }
    }

    // Initial feed load when location becomes available
    LaunchedEffect(userLocation) {
        if (userLocation != null && feedPosts.isEmpty()) {
            loadFeed(refresh = true)
        }
    }

    LaunchedEffect(selectedTab) {
        scrollAwareState.reset()
    }

    val contentBlur by animateDpAsState(
        targetValue = if (showLoginOverlay) 240.dp else 0.dp,
        animationSpec = tween(300),
        label = "contentBlur"
    )
    Box(modifier = Modifier.fillMaxSize()) {
    Box(modifier = Modifier.fillMaxSize().background(colors.background).blur(contentBlur)) {
        // Header
        if (selectedTab == Tab.Home) {
            Box(
                modifier = Modifier
                    .zIndex(2f)
                    .offset { IntOffset(0, scrollAwareState.topBarOffsetPx.roundToInt()) }
                    .onGloballyPositioned { scrollAwareState.topBarHeightPx = it.size.height.toFloat() }
                    .background(colors.surface)
            ) {
                Header(locationName = locationName)
            }

            Box(
                modifier = Modifier
                    .zIndex(1f)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .height(0.5.dp)
                    .background(colors.divider)
            )
        }

        // Top gradient (visible when header is hidden)
        if (selectedTab == Tab.Home && scrollAwareState.topBarOffsetPx < 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(1.5f)
                    .height(60.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(colors.background, colors.background.copy(alpha = 0f))
                        )
                    )
            )
        }

        // Content
        when (selectedTab) {
            Tab.Home -> Feed(
                posts = feedPosts,
                listState = feedListState,
                isLoading = isFeedLoading,
                isRefreshing = isRefreshing,
                onSignalClick = { post -> requireAuth { showSignalForPost = post } },
                onCommentClick = { },
                onShareClick = { /* TODO: share intent */ },
                onReportClick = { post -> requireAuth { showReportForPost = post } },
                onStoryClick = { },
                onStoryTap = { tapInfo ->
                    storyTransition = tapInfo
                    storyTransitionExpanded = true
                },
                onLoadMore = {
                    if (hasMore && !isFeedLoading && feedCursor != null) {
                        loadFeed()
                    }
                },
                onRefresh = { loadFeed(refresh = true) },
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollAwareState.nestedScrollConnection),
                contentPadding = PaddingValues(
                    top = with(density) { scrollAwareState.topBarHeightPx.toDp() },
                    bottom = with(density) { scrollAwareState.bottomBarHeightPx.toDp() }
                )
            )
            Tab.MyPosts -> {
                if (effectiveLoggedIn) {
                    MyPostsScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = with(density) { scrollAwareState.bottomBarHeightPx.toDp() })
                    )
                } else {
                    LoginScreen(
                        onLoginSuccess = onLoginSuccess,
                        onDismiss = { selectedTab = Tab.Home }
                    )
                }
            }
            Tab.Profile -> {
                if (effectiveLoggedIn) {
                    ProfileScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = with(density) { scrollAwareState.bottomBarHeightPx.toDp() }),
                        onLogout = {
                            scope.launch {
                                ApiClient.logout()
                                TokenManager.clear()
                                onLogout()
                            }
                        }
                    )
                } else {
                    LoginScreen(
                        onLoginSuccess = onLoginSuccess,
                        onDismiss = { selectedTab = Tab.Home }
                    )
                }
            }
        }

        // FAB
        if (selectedTab == Tab.Home) {
            CreatePostFab(
                onClick = { requireAuth { navController.navigateOnce(OverlayRoute.CreatePost.route) } },
                scrollAwareState = scrollAwareState,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .zIndex(3f)
            )
        }

        // Bottom gradient (visible when bottom bar is hidden or not logged in)
        if (selectedTab == Tab.Home && (!effectiveLoggedIn || scrollAwareState.bottomBarOffsetPx < 0)) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .zIndex(1.5f)
                    .height(60.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(colors.background.copy(alpha = 0f), colors.background)
                        )
                    )
            )
        }

        // Bottom bar (only when logged in)
        if (effectiveLoggedIn) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(2f)
                    .offset { IntOffset(0, -scrollAwareState.bottomBarOffsetPx.roundToInt()) }
                    .onGloballyPositioned { scrollAwareState.bottomBarHeightPx = it.size.height.toFloat() }
                    .background(colors.surface)
            ) {
                BottomTabBar(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
            }
        }

        // Overlay navigation (only CreatePost now)
        Box(modifier = Modifier.fillMaxSize().zIndex(10f)) {
            NavHost(navController = navController, startDestination = "empty") {
                composable("empty") {}

                composable(
                    OverlayRoute.CreatePost.route,
                    enterTransition = { slideInHorizontally(tween(250)) { it } },
                    popExitTransition = { slideOutHorizontally(tween(250)) { it } }
                ) {
                    CreatePostScreen(
                        onDismiss = {
                            navController.popBackStack("empty", inclusive = false)
                        },
                        onPostCreated = {
                            loadFeed(refresh = true)
                        }
                    )
                }
            }
        }

        if (showSignalForPost != null) {
            SignalSheet(
                postId = showSignalForPost!!.id,
                typeKey = showSignalForPost!!.typeKey,
                onSignalsUpdated = { newCount ->
                    val idx = feedPosts.indexOfFirst { it.id == showSignalForPost!!.id }
                    if (idx >= 0) {
                        feedPosts = feedPosts.toMutableList().also { it[idx] = it[idx].copy(signalsCount = newCount) }
                    }
                },
                onDismiss = { showSignalForPost = null }
            )
        }

        if (showReportForPost != null) {
            ReportSheet(
                postId = showReportForPost!!.id,
                onDismiss = { showReportForPost = null }
            )
        }

        // Story expand circle transition
        storyTransition?.let { tap ->
            val expandProgress = remember { androidx.compose.animation.core.Animatable(0f) }

            LaunchedEffect(tap) {
                expandProgress.snapTo(0f)
                expandProgress.animateTo(1f, tween(180))
                showStoryViewer = tap.story
                delay(50)
                storyTransition = null
                storyTransitionExpanded = false
            }

            val maxDx = maxOf(tap.centerX, screenWidth - tap.centerX)
            val maxDy = maxOf(tap.centerY, screenHeight - tap.centerY)
            val expandedSize = kotlin.math.sqrt(maxDx * maxDx + maxDy * maxDy) * 2f
            val currentSize = tap.size + (expandedSize - tap.size) * expandProgress.value

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(14f)
            ) {
                drawCircle(
                    color = Color(0xFF1A1A2E),
                    radius = currentSize / 2f,
                    center = Offset(tap.centerX, tap.centerY)
                )
            }
        }

        // Story viewer
        if (showStoryViewer != null) {
            Box(modifier = Modifier.fillMaxSize().zIndex(15f)) {
                val initialIndex = com.example.app.android.components.mockStories
                    .indexOfFirst { it.id == showStoryViewer!!.id }
                    .coerceAtLeast(0)
                com.example.app.android.components.StoryViewer(
                    stories = com.example.app.android.components.mockStories,
                    initialStoryIndex = initialIndex,
                    contentsForStory = { story -> mockStoryContents(story.label) },
                    onDismiss = { showStoryViewer = null }
                )
            }
        }

        // Toast overlay
        Box(modifier = Modifier.fillMaxSize().zIndex(25f)) {
            ToastOverlay()
        }
    } // end blurred content Box

        // Login overlay (triggered by interactions when not logged in) — outside blur
        AnimatedVisibility(
            visible = showLoginOverlay,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.fillMaxSize().zIndex(20f)
        ) {
            LoginScreen(
                onLoginSuccess = {
                    onLoginSuccess()
                    showLoginOverlay = false
                },
                onDismiss = { showLoginOverlay = false }
            )
        }
    } // end root Box
}

private fun mockStoryContents(label: String) = listOf(
    com.example.app.android.components.StoryContent("1", "Buraco grande na rua principal, cuidado ao passar!", "2h"),
    com.example.app.android.components.StoryContent("2", "Semáforo quebrado no cruzamento da Av. Brasil", "4h"),
    com.example.app.android.components.StoryContent("3", "Festa de rua acontecendo neste fim de semana", "6h"),
)
