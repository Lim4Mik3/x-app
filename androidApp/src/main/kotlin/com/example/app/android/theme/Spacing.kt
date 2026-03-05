package com.example.app.android.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AppSpacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 6.dp,
    val md: Dp = 8.dp,
    val lg: Dp = 12.dp,
    val xl: Dp = 14.dp,
    val xxl: Dp = 16.dp,
    val xxxl: Dp = 20.dp,
    val huge: Dp = 24.dp,

    val divider: Dp = 0.5.dp,
    val avatarSize: Dp = 40.dp,
    val iconSmall: Dp = 20.dp,
    val iconMedium: Dp = 24.dp,
    val iconButton: Dp = 32.dp,
    val feedDividerStart: Dp = 72.dp,
    val badgeCorner: Dp = 10.dp,
    val cornerSmall: Dp = 8.dp
)

val LocalSpacing = staticCompositionLocalOf { AppSpacing() }
