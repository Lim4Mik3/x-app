package com.example.app.android.network

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREFS_NAME = "x_auth"
    private const val KEY_ACCESS = "access_token"
    private const val KEY_REFRESH = "refresh_token"
    private const val KEY_DISPLAY_NAME = "display_name"
    private const val KEY_USER_ID = "user_id"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS, null)
        set(value) = prefs.edit().putString(KEY_ACCESS, value).apply()

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH, null)
        set(value) = prefs.edit().putString(KEY_REFRESH, value).apply()

    var displayName: String?
        get() = prefs.getString(KEY_DISPLAY_NAME, null)
        set(value) = prefs.edit().putString(KEY_DISPLAY_NAME, value).apply()

    var userId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()

    val isLoggedIn: Boolean get() = accessToken != null

    fun saveAuth(accessToken: String, refreshToken: String) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
