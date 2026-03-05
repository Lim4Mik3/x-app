package com.example.app.android.screens

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.app.android.components.BottomTabBar
import com.example.app.android.components.CreatePostFab
import com.example.app.android.components.Feed
import com.example.app.android.components.Header
import com.example.app.android.components.Tab
import com.example.app.android.components.mockPosts
import com.example.app.android.components.rememberScrollAwareState
import com.example.app.android.navigation.OverlayRoute
import com.example.app.android.navigation.navigateOnce
import com.example.app.android.theme.AppTheme
import androidx.compose.ui.res.stringResource
import android.net.Uri
import com.example.app.android.R
import kotlin.math.roundToInt

@Composable
fun MainScreen() {
    val colors = AppTheme.colors
    var selectedLocation by remember { mutableStateOf("Osasco") }
    var selectedTab by remember { mutableStateOf(Tab.Home) }
    var draftPostText by remember { mutableStateOf("") }
    var draftLocation by remember { mutableStateOf("Osasco") }
    val feedListState = rememberLazyListState()
    val scrollAwareState = rememberScrollAwareState()
    val density = LocalDensity.current
    var draftMediaUri by remember { mutableStateOf<Uri?>(null) }
    val navController = rememberNavController()

    LaunchedEffect(selectedTab) {
        scrollAwareState.reset()
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        // Header overlay
        if (selectedTab == Tab.Home) {
            Box(
                modifier = Modifier
                    .zIndex(2f)
                    .offset {
                        IntOffset(0, scrollAwareState.topBarOffsetPx.roundToInt())
                    }
                    .onGloballyPositioned {
                        scrollAwareState.topBarHeightPx = it.size.height.toFloat()
                    }
                    .background(colors.surface)
            ) {
                Header(
                    locationName = selectedLocation,
                    timeAgo = stringResource(R.string.time_ago),
                    statusLabel = stringResource(R.string.mood_calm),
                    onLocationClick = { navController.navigateOnce(OverlayRoute.LocationPicker.route) },
                    onMoodClick = { navController.navigateOnce(OverlayRoute.MoodDetail.route) }
                )
            }

            // Persistent divider at status bar level
            Box(
                modifier = Modifier
                    .zIndex(1f)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .height(0.5.dp)
                    .background(colors.divider)
            )
        }

        // Content — feed scrolls behind the bars
        when (selectedTab) {
            Tab.Home -> Feed(
                posts = mockPosts,
                listState = feedListState,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollAwareState.nestedScrollConnection),
                contentPadding = PaddingValues(
                    top = with(density) { scrollAwareState.topBarHeightPx.toDp() },
                    bottom = with(density) { scrollAwareState.bottomBarHeightPx.toDp() }
                )
            )
            Tab.MyPosts -> MyPostsScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = with(density) { scrollAwareState.bottomBarHeightPx.toDp() })
            )
            Tab.Profile -> ProfileScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = with(density) { scrollAwareState.bottomBarHeightPx.toDp() })
            )
        }

        // FAB — create new post
        if (selectedTab == Tab.Home) {
            CreatePostFab(
                onClick = { navController.navigateOnce(OverlayRoute.CreatePost.route) },
                scrollAwareState = scrollAwareState,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .zIndex(3f)
            )
        }

        // Bottom bar overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(2f)
                .offset {
                    IntOffset(0, -scrollAwareState.bottomBarOffsetPx.roundToInt())
                }
                .onGloballyPositioned {
                    scrollAwareState.bottomBarHeightPx = it.size.height.toFloat()
                }
                .background(colors.surface)
        ) {
            BottomTabBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }

        // Overlay navigation
        Box(modifier = Modifier.fillMaxSize().zIndex(10f)) {
            NavHost(
                navController = navController,
                startDestination = "empty"
            ) {
                composable("empty") {}

                composable(
                    OverlayRoute.CreatePost.route,
                    enterTransition = { slideInHorizontally(tween(250)) { it } },
                    exitTransition = {
                        // When Map is pushed on top, stay in place (no animation)
                        if (targetState.destination.route == OverlayRoute.Map.route) {
                            ExitTransition.None
                        } else {
                            slideOutHorizontally(tween(250)) { it }
                        }
                    },
                    popEnterTransition = {
                        // When returning from Map, already visible underneath
                        if (initialState.destination.route == OverlayRoute.Map.route) {
                            EnterTransition.None
                        } else {
                            slideInHorizontally(tween(250)) { -it }
                        }
                    },
                    popExitTransition = { slideOutHorizontally(tween(250)) { it } }
                ) {
                    CreatePostScreen(
                        text = draftPostText,
                        onTextChanged = { draftPostText = it },
                        onDismiss = {
                            draftPostText = ""
                            navController.popBackStack("empty", inclusive = false)
                        },
                        onPublish = {
                            navController.navigateOnce(OverlayRoute.Map.route)
                        }
                    )
                }

                composable(
                    OverlayRoute.Map.route,
                    enterTransition = { slideInHorizontally(tween(250)) { it } },
                    exitTransition = { slideOutHorizontally(tween(250)) { -it } },
                    popEnterTransition = { slideInHorizontally(tween(250)) { -it } },
                    popExitTransition = { slideOutHorizontally(tween(250)) { it } }
                ) {
                    MapScreen(
                        onDismiss = {
                            navController.popBackStack()
                        },
                        onLocationConfirmed = {
                            draftLocation = "Osasco"
                            navController.navigateOnce(OverlayRoute.Media.route)
                        }
                    )
                }

                composable(
                    OverlayRoute.Media.route,
                    enterTransition = { slideInHorizontally(tween(250)) { it } },
                    exitTransition = { slideOutHorizontally(tween(250)) { -it } },
                    popEnterTransition = { slideInHorizontally(tween(250)) { -it } },
                    popExitTransition = { slideOutHorizontally(tween(250)) { it } }
                ) {
                    CameraScreen(
                        visible = true,
                        onDismiss = {
                            navController.popBackStack()
                        },
                        onMediaCaptured = { uri ->
                            draftMediaUri = uri
                            navController.navigateOnce(OverlayRoute.ReviewPost.route)
                        },
                        onSkip = {
                            draftMediaUri = null
                            navController.navigateOnce(OverlayRoute.ReviewPost.route)
                        }
                    )
                }

                composable(
                    OverlayRoute.ReviewPost.route,
                    enterTransition = { slideInHorizontally(tween(250)) { it } },
                    exitTransition = { slideOutHorizontally(tween(250)) { it } },
                    popEnterTransition = { slideInHorizontally(tween(250)) { -it } },
                    popExitTransition = { slideOutHorizontally(tween(250)) { it } }
                ) {
                    ReviewPostScreen(
                        postText = draftPostText,
                        location = draftLocation,
                        mediaUri = draftMediaUri,
                        onDismiss = {
                            draftPostText = ""
                            draftMediaUri = null
                            navController.popBackStack("empty", inclusive = false)
                        },
                        onConfirm = {
                            draftPostText = ""
                            draftMediaUri = null
                            navController.popBackStack("empty", inclusive = false)
                        }
                    )
                }

                composable(
                    OverlayRoute.LocationPicker.route,
                    enterTransition = { slideInVertically(tween(150)) { it } },
                    exitTransition = { fadeOut(tween(50)) },
                    popExitTransition = { fadeOut(tween(50)) }
                ) {
                    LocationPickerScreen(
                        onDismiss = {
                            navController.popBackStack()
                        },
                        onLocationSelected = { location ->
                            selectedLocation = location
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    OverlayRoute.MoodDetail.route,
                    enterTransition = { slideInVertically(tween(150)) { it } },
                    exitTransition = { fadeOut(tween(50)) },
                    popExitTransition = { fadeOut(tween(50)) }
                ) {
                    MoodDetailScreen(
                        onDismiss = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
