package com.example.app.android.navigation

import androidx.navigation.NavController

sealed class OverlayRoute(val route: String) {
    data object CreatePost : OverlayRoute("createPost")
}

fun NavController.navigateOnce(route: String) {
    if (currentBackStackEntry?.destination?.route != route) {
        navigate(route)
    }
}
