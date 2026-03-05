package com.example.app.android.services

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

class LocationService private constructor(context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location.asStateFlow()

    private var lastFetchTime = 0L
    private var ttlMs = DEFAULT_TTL_MS

    val cachedLocation: Location? get() = _location.value

    val isCacheValid: Boolean
        get() = _location.value != null && (System.currentTimeMillis() - lastFetchTime) < ttlMs

    /**
     * Returns cached location if still valid, otherwise fetches fresh.
     * Call from a coroutine — this is a fire-and-forget fetch.
     */
    @SuppressLint("MissingPermission")
    fun fetch(forceRefresh: Boolean = false) {
        if (!forceRefresh && isCacheValid) return

        fusedClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                _location.value = location
                lastFetchTime = System.currentTimeMillis()
            } else {
                // lastLocation can be null if no recent location — request a fresh one
                requestFresh()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestFresh() {
        val cts = CancellationTokenSource()
        fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    _location.value = location
                    lastFetchTime = System.currentTimeMillis()
                }
            }
    }

    fun setTtl(ms: Long) {
        ttlMs = ms
    }

    companion object {
        private const val DEFAULT_TTL_MS = 60_000L // 60 seconds (1 minute)

        @Volatile
        private var instance: LocationService? = null

        fun getInstance(context: Context): LocationService {
            return instance ?: synchronized(this) {
                instance ?: LocationService(context.applicationContext).also { instance = it }
            }
        }

        /**
         * Calculate distance in meters between two points.
         */
        fun distanceBetween(
            lat1: Double, lng1: Double,
            lat2: Double, lng2: Double
        ): Float {
            val results = FloatArray(1)
            Location.distanceBetween(lat1, lng1, lat2, lng2, results)
            return results[0]
        }

        /**
         * Format distance as user-friendly string.
         */
        fun formatDistance(meters: Float): String {
            return when {
                meters < 10f -> "Menos de 10 metros do ocorrido"
                meters < 1000f -> "A ${meters.roundToInt()} metros do ocorrido"
                else -> "A %.1f km do ocorrido".format(meters / 1000f)
            }
        }
    }
}
