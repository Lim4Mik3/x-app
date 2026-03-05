package com.example.app.android.network.models

import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// ── Auth ──

data class AuthResponse(
    val user: User,
    val accessToken: String,
    val refreshToken: String,
    val isNewUser: Boolean
) {
    companion object {
        fun fromJson(json: JSONObject) = AuthResponse(
            user = User.fromJson(json.getJSONObject("user")),
            accessToken = json.getString("access_token"),
            refreshToken = json.getString("refresh_token"),
            isNewUser = json.optBoolean("is_new_user", false)
        )
    }
}

data class TokenPair(
    val accessToken: String,
    val refreshToken: String
) {
    companion object {
        fun fromJson(json: JSONObject) = TokenPair(
            accessToken = json.getString("access_token"),
            refreshToken = json.getString("refresh_token")
        )
    }
}

data class User(
    val id: String,
    val displayName: String?,
    val email: String?,
    val phone: String?,
    val avatarUrl: String?,
    val addressCity: String?,
    val addressState: String?,
    val preferredLocale: String?
) {
    companion object {
        fun fromJson(json: JSONObject) = User(
            id = json.getString("id"),
            displayName = json.optString("display_name", null),
            email = json.optString("email", null),
            phone = json.optString("phone", null),
            avatarUrl = json.optString("avatar_url", null),
            addressCity = json.optString("address_city", null),
            addressState = json.optString("address_state", null),
            preferredLocale = json.optString("preferred_locale", null)
        )
    }
}

// ── Feed ──

data class FeedResponse(
    val posts: List<FeedPost>,
    val total: Int,
    val nextCursor: String?,
    val hasMore: Boolean
) {
    companion object {
        fun fromJson(json: JSONObject): FeedResponse {
            val postsArray = json.getJSONArray("posts")
            val posts = (0 until postsArray.length()).map {
                FeedPost.fromJson(postsArray.getJSONObject(it))
            }
            return FeedResponse(
                posts = posts,
                total = json.optInt("total", 0),
                nextCursor = json.optString("next_cursor", null),
                hasMore = json.optBoolean("has_more", false)
            )
        }
    }
}

data class FeedPost(
    val id: String,
    val originalText: String,
    val normalizedText: String,
    val type: String,
    val typeConfidence: Double,
    val categories: List<String>,
    val createdAt: String,
    val latitude: Double?,
    val longitude: Double?
) {
    val timeAgo: String get() = formatTimeAgo(createdAt)

    val typeLabel: String
        get() = when (type.uppercase()) {
            "NARRAR" -> "Relato"
            "ALERTAR" -> "Alerta"
            "VENDER" -> "Com\u00e9rcio"
            "INFORMAR" -> "Informa\u00e7\u00e3o"
            "PEDIR" -> "Pedido"
            "RECLAMAR" -> "Reclama\u00e7\u00e3o"
            "DENUNCIAR" -> "Den\u00fancia"
            else -> type
        }

    val categoryLabel: String?
        get() = categories.firstOrNull()?.let { cat ->
            when (cat.uppercase()) {
                "SEGURANCA_CRIME" -> "Crime"
                "INFRAESTRUTURA" -> "Infraestrutura"
                "TRANSITO" -> "Tr\u00e2nsito"
                "CLIMA" -> "Clima"
                "SAUDE" -> "Sa\u00fade"
                "COMUNIDADE" -> "Comunidade"
                "COMERCIO" -> "Com\u00e9rcio"
                "EVENTO" -> "Evento"
                "ANIMAL" -> "Animais"
                "ESPORTE" -> "Esporte"
                "TRANSPORTE" -> "Transporte"
                else -> cat.replace("_", " ").lowercase()
                    .replaceFirstChar { it.uppercase() }
            }
        }

    companion object {
        fun fromJson(json: JSONObject): FeedPost {
            val cats = json.optJSONArray("categories") ?: JSONArray()
            return FeedPost(
                id = json.getString("id"),
                originalText = json.optString("original_text", ""),
                normalizedText = json.optString("normalized_text", ""),
                type = json.optString("type", ""),
                typeConfidence = json.optDouble("type_confidence", 0.0),
                categories = (0 until cats.length()).map { cats.getString(it) },
                createdAt = json.optString("created_at", ""),
                latitude = if (json.isNull("latitude")) null else json.optDouble("latitude"),
                longitude = if (json.isNull("longitude")) null else json.optDouble("longitude")
            )
        }
    }
}

// ── Post Creation ──

data class CreatedPost(
    val id: String,
    val originalText: String,
    val normalizedText: String,
    val type: String,
    val categories: List<String>,
    val location: PostLocation?
) {
    companion object {
        fun fromJson(json: JSONObject): CreatedPost {
            val cats = json.optJSONArray("categories")?.let { arr ->
                (0 until arr.length()).map {
                    val item = arr.get(it)
                    if (item is JSONObject) item.optString("label", "") else item.toString()
                }
            } ?: emptyList()
            return CreatedPost(
                id = json.getString("id"),
                originalText = json.optString("original_text", ""),
                normalizedText = json.optString("normalized_text", ""),
                type = json.optJSONObject("type")?.optString("label", "") ?: json.optString("type", ""),
                categories = cats,
                location = json.optJSONObject("location")?.let { PostLocation.fromJson(it) }
            )
        }
    }
}

data class PostLocation(
    val neighborhood: String?,
    val city: String?,
    val state: String?,
    val displayName: String?
) {
    companion object {
        fun fromJson(json: JSONObject) = PostLocation(
            neighborhood = json.optString("neighborhood", null),
            city = json.optString("city", null),
            state = json.optString("state", null),
            displayName = json.optString("display_name", null)
        )
    }
}

// ── Comments ──

data class Comment(
    val id: String,
    val postId: String,
    val parentId: String?,
    val authorName: String,
    val text: String,
    val upvotes: Int,
    val downvotes: Int,
    val createdAt: String,
    val replies: List<Comment>
) {
    val timeAgo: String get() = formatTimeAgo(createdAt)

    companion object {
        fun fromJson(json: JSONObject): Comment {
            val repliesArr = json.optJSONArray("replies") ?: JSONArray()
            return Comment(
                id = json.getString("id"),
                postId = json.optString("post_id", ""),
                parentId = json.optString("parent_id", null),
                authorName = json.optString("author_name", ""),
                text = json.optString("text", ""),
                upvotes = json.optInt("upvotes", 0),
                downvotes = json.optInt("downvotes", 0),
                createdAt = json.optString("created_at", ""),
                replies = (0 until repliesArr.length()).map {
                    fromJson(repliesArr.getJSONObject(it))
                }
            )
        }

        fun listFromJson(arr: JSONArray): List<Comment> =
            (0 until arr.length()).map { fromJson(arr.getJSONObject(it)) }
    }
}

// ── Signals ──

data class SignalKey(
    val key: String,
    val label: String,
    val category: String,
    val polarity: String
) {
    companion object {
        fun fromJson(json: JSONObject) = SignalKey(
            key = json.getString("key"),
            label = json.optString("label", ""),
            category = json.optString("category", ""),
            polarity = json.optString("polarity", "")
        )
    }
}

data class PostSignals(
    val signals: Map<String, Int>,
    val userSignals: List<String>
) {
    companion object {
        fun fromJson(json: JSONObject): PostSignals {
            val signalsObj = json.optJSONObject("signals") ?: JSONObject()
            val userArr = json.optJSONArray("user_signals") ?: JSONArray()
            val map = mutableMapOf<String, Int>()
            signalsObj.keys().forEach { key -> map[key] = signalsObj.optInt(key, 0) }
            val userList = (0 until userArr.length()).map { userArr.getString(it) }
            return PostSignals(signals = map, userSignals = userList)
        }
    }
}

// ── Reports ──

data class ReportReason(
    val key: String,
    val label: String
) {
    companion object {
        fun fromJson(json: JSONObject) = ReportReason(
            key = json.getString("key"),
            label = json.optString("label", "")
        )
    }
}

data class ReportResult(
    val reported: Boolean,
    val reportId: String?,
    val totalReports: Int,
    val postHidden: Boolean
) {
    companion object {
        fun fromJson(json: JSONObject) = ReportResult(
            reported = json.optBoolean("reported", false),
            reportId = json.optString("report_id", null),
            totalReports = json.optInt("total_reports", 0),
            postHidden = json.optBoolean("post_hidden", false)
        )
    }
}

// ── Utils ──

private val isoFormat = ThreadLocal.withInitial {
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
}

fun formatTimeAgo(isoDate: String): String {
    if (isoDate.isBlank()) return ""
    val date = try {
        isoFormat.get()!!.parse(isoDate.substringBefore('.').substringBefore('Z'))
    } catch (_: Exception) {
        return ""
    } ?: return ""
    val diffMs = System.currentTimeMillis() - date.time
    val minutes = diffMs / 60_000
    return when {
        minutes < 1 -> "agora"
        minutes < 60 -> "${minutes}min"
        minutes < 1440 -> "${minutes / 60}h"
        minutes < 43200 -> "${minutes / 1440}d"
        else -> "${minutes / 43200}m"
    }
}
