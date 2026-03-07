package com.example.app.android.network

import android.content.Context
import android.content.SharedPreferences
import com.example.app.android.network.models.SignalKey
import org.json.JSONArray
import org.json.JSONObject

object SignalKeysCache {
    private const val PREFS_NAME = "signal_keys_cache"
    private const val KEY_DATA = "cache_data"
    private const val KEY_TIMESTAMP = "cache_timestamp"
    private const val KEY_LOCALE = "cache_locale"
    private const val TTL_MS = 24 * 60 * 60 * 1000L // 24 hours

    private var memoryCache: Map<String, List<SignalKey>>? = null
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    suspend fun getKeys(typeKey: String): List<SignalKey>? {
        // Memory cache
        memoryCache?.let { return it[typeKey] }

        // Disk cache
        val currentLocale = java.util.Locale.getDefault().language
        val disk = loadFromDisk()
        if (disk != null && !disk.isExpired && disk.locale == currentLocale) {
            memoryCache = disk.signals
            return disk.signals[typeKey]
        }

        // Fetch from API
        refresh()
        return memoryCache?.get(typeKey)
    }

    suspend fun refresh() {
        try {
            val result = ApiClient.getAllSignalKeys()
            result.onSuccess { allSignals ->
                memoryCache = allSignals
                saveToDisk(allSignals, java.util.Locale.getDefault().language)
                android.util.Log.d("SignalKeysCache", "Fetched ${allSignals.size} types: ${allSignals.keys}")
            }
            result.onFailure {
                // Fallback: load stale cache
                val disk = loadFromDisk()
                if (disk != null) memoryCache = disk.signals
                android.util.Log.d("SignalKeysCache", "Refresh failed: ${it.message}")
            }
        } catch (e: Exception) {
            val disk = loadFromDisk()
            if (disk != null) memoryCache = disk.signals
        }
    }

    fun invalidate() {
        memoryCache = null
    }

    private data class DiskCache(
        val signals: Map<String, List<SignalKey>>,
        val locale: String,
        val isExpired: Boolean
    )

    private fun saveToDisk(signals: Map<String, List<SignalKey>>, locale: String) {
        val json = JSONObject()
        for ((typeKey, keys) in signals) {
            val arr = JSONArray()
            for (key in keys) {
                arr.put(JSONObject().apply {
                    put("key", key.key)
                    put("label", key.label)
                    put("category", key.category)
                    put("opposite", key.opposite ?: JSONObject.NULL)
                })
            }
            json.put(typeKey, arr)
        }
        prefs?.edit()
            ?.putString(KEY_DATA, json.toString())
            ?.putLong(KEY_TIMESTAMP, System.currentTimeMillis())
            ?.putString(KEY_LOCALE, locale)
            ?.apply()
    }

    private fun loadFromDisk(): DiskCache? {
        val dataStr = prefs?.getString(KEY_DATA, null) ?: return null
        val timestamp = prefs?.getLong(KEY_TIMESTAMP, 0L) ?: 0L
        val locale = prefs?.getString(KEY_LOCALE, "") ?: ""
        val isExpired = System.currentTimeMillis() - timestamp > TTL_MS

        return try {
            val json = JSONObject(dataStr)
            val signals = mutableMapOf<String, List<SignalKey>>()
            json.keys().forEach { typeKey ->
                val arr = json.getJSONArray(typeKey)
                signals[typeKey] = (0 until arr.length()).map { i ->
                    val obj = arr.getJSONObject(i)
                    SignalKey(
                        key = obj.getString("key"),
                        label = obj.optString("label", ""),
                        category = obj.optString("category", ""),
                        opposite = if (obj.isNull("opposite")) null else obj.optString("opposite", null)
                    )
                }
            }
            DiskCache(signals = signals, locale = locale, isExpired = isExpired)
        } catch (_: Exception) {
            null
        }
    }
}
