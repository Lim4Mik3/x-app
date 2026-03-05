package com.example.app.android.model

data class UserProfile(
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val location: String,
    val initials: String,
    val preferences: List<UserPreference>
)

data class UserPreference(
    val key: String,
    val label: String,
    val enabled: Boolean
)
