package com.example.app.android.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class AppColorScheme(
    val textPrimary: Color,
    val textSecondary: Color,
    val background: Color,
    val surface: Color,
    val divider: Color,
    val avatarBackground: Color,
    val badgeBackground: Color,
    val badgeText: Color,
    val accent: Color,
    val destructive: Color,
    val onAccent: Color,
    val onDestructive: Color,
    val tabActive: Color,
    val tabInactive: Color
)

val LightColorScheme = AppColorScheme(
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    background = LightBackground,
    surface = LightSurface,
    divider = LightDivider,
    avatarBackground = LightAvatarBackground,
    badgeBackground = LightBadgeBackground,
    badgeText = LightBadgeText,
    accent = LightAccent,
    destructive = LightDestructive,
    onAccent = LightOnAccent,
    onDestructive = LightOnDestructive,
    tabActive = LightTabActive,
    tabInactive = LightTabInactive
)

val DarkColorScheme = AppColorScheme(
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    divider = DarkDivider,
    avatarBackground = DarkAvatarBackground,
    badgeBackground = DarkBadgeBackground,
    badgeText = DarkBadgeText,
    accent = DarkAccent,
    destructive = DarkDestructive,
    onAccent = DarkOnAccent,
    onDestructive = DarkOnDestructive,
    tabActive = DarkTabActive,
    tabInactive = DarkTabInactive
)

val LocalAppColors = staticCompositionLocalOf { LightColorScheme }

object AppTheme {
    val colors: AppColorScheme
        @Composable
        get() = LocalAppColors.current

    val spacing: AppSpacing
        @Composable
        get() = LocalSpacing.current
}

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalAppColors provides colorScheme,
        LocalSpacing provides AppSpacing(),
        content = content
    )
}
