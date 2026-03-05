package com.example.app.android.navigation

import androidx.navigation.NavController

sealed class OverlayRoute(val route: String) {
    data object CreatePost : OverlayRoute("createPost")
    data object Map : OverlayRoute("map")
    data object LocationPicker : OverlayRoute("locationPicker")
    data object MoodDetail : OverlayRoute("moodDetail")
    data object Media : OverlayRoute("media")
    data object ReviewPost : OverlayRoute("reviewPost")
}

fun NavController.navigateOnce(route: String) {
    if (currentBackStackEntry?.destination?.route != route) {
        navigate(route)
    }
}
