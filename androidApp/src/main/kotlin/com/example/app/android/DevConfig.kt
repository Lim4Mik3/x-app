package com.example.app.android

object DevConfig {
    // Set to true to bypass login requirement for UI actions (signals, comments, etc.)
    const val BYPASS_AUTH = true

    // Set to true to send X-Dev-Bypass header on all API requests
    const val BYPASS_API_AUTH = true

    // Header sent when BYPASS_API_AUTH is true
    const val DEV_BYPASS_HEADER = "X-Dev-User-ID"
    const val DEV_BYPASS_VALUE = "dev-user-001"
}
