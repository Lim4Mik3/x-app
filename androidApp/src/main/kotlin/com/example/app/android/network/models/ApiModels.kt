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
    val name: String?,
    val displayName: String?,
    val email: String?,
    val phone: String?,
    val avatarUrl: String?,
    val addressCity: String?,
    val addressState: String?,
    val preferredLocale: String?,
    val addresses: List<UserAddress>,
    val accounts: List<UserAccount>
) {
    val initials: String get() {
        val parts = (name ?: displayName ?: "").split(" ")
        return if (parts.size >= 2) "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
        else (name ?: displayName ?: "?").take(2).uppercase()
    }

    fun isProviderConnected(provider: String): Boolean =
        accounts.any { it.provider == provider }

    companion object {
        fun fromJson(json: JSONObject): User {
            val addrsArr = json.optJSONArray("addresses") ?: JSONArray()
            val addrs = (0 until addrsArr.length()).mapNotNull {
                UserAddress.fromJson(addrsArr.getJSONObject(it))
            }
            val accsArr = json.optJSONArray("accounts") ?: JSONArray()
            val accs = (0 until accsArr.length()).mapNotNull {
                UserAccount.fromJson(accsArr.getJSONObject(it))
            }
            return User(
                id = json.getString("id"),
                name = json.optString("name", null),
                displayName = json.optString("display_name", null) ?: json.optString("name", null),
                email = json.optString("email", null),
                phone = json.optString("phone", null),
                avatarUrl = json.optString("avatar_url", null),
                addressCity = json.optString("address_city", null),
                addressState = json.optString("address_state", null),
                preferredLocale = json.optString("preferred_locale", null),
                addresses = addrs,
                accounts = accs
            )
        }
    }
}

data class UserAddress(
    val label: String,
    val street: String,
    val number: String,
    val complement: String?,
    val neighborhood: String,
    val city: String,
    val state: String,
    val country: String,
    val zipCode: String,
    val isPrimary: Boolean
) {
    val formatted: String get() = "$street, $number"
    val location: String get() = "$city, $state"

    companion object {
        fun fromJson(json: JSONObject) = UserAddress(
            label = json.optString("label", ""),
            street = json.optString("street", ""),
            number = json.optString("number", ""),
            complement = json.optString("complement", null),
            neighborhood = json.optString("neighborhood", ""),
            city = json.optString("city", ""),
            state = json.optString("state", ""),
            country = json.optString("country", ""),
            zipCode = json.optString("zip_code", ""),
            isPrimary = json.optBoolean("is_primary", false)
        )
    }
}

data class UserAccount(
    val provider: String,
    val connectedAt: String
) {
    companion object {
        fun fromJson(json: JSONObject): UserAccount? {
            val provider = json.optString("provider", null) ?: return null
            return UserAccount(
                provider = provider,
                connectedAt = json.optString("connected_at", "")
            )
        }
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
    val content: String,
    val type: String,
    val typeKey: String,
    val typeColor: String?,
    val categories: List<String>,
    val postedAt: String,
    var signalsCount: Int,
    val commentsCount: Int,
    val distance: String?
) {
    val timeAgo: String get() = formatTimeAgo(postedAt)

    companion object {
        fun fromJson(json: JSONObject): FeedPost {
            val cats = json.optJSONArray("categories") ?: JSONArray()
            return FeedPost(
                id = json.getString("id"),
                content = json.optString("content", ""),
                type = json.optString("type", ""),
                typeKey = json.optString("type_key", ""),
                typeColor = json.optString("type_color", null),
                categories = (0 until cats.length()).map { cats.getString(it) },
                postedAt = json.optString("posted_at", ""),
                signalsCount = json.optInt("signals_count", 0),
                commentsCount = json.optInt("comments_count", 0),
                distance = json.optString("distance", null)
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
    val opposite: String?
) {
    companion object {
        fun fromJson(json: JSONObject) = SignalKey(
            key = json.getString("key"),
            label = json.optString("label", ""),
            category = json.optString("category", ""),
            opposite = json.optString("opposite", null)
        )
    }
}

data class SignalPair(
    val left: SignalKey,
    val right: SignalKey
)

data class SignalGroup(
    val category: String,
    val label: String,
    val pairs: List<SignalPair>
) {
    companion object {
        fun groupFromKeys(keys: List<SignalKey>): List<SignalGroup> {
            val byCategory = keys.groupBy { it.category }
            val categoryOrder = listOf("verification", "reaction")
            val categoryLabels = mapOf("verification" to "Verificação", "reaction" to "Reação")

            return categoryOrder.mapNotNull { cat ->
                val signals = byCategory[cat] ?: return@mapNotNull null
                if (signals.isEmpty()) return@mapNotNull null
                val pairs = mutableListOf<SignalPair>()
                val used = mutableSetOf<String>()
                for (signal in signals) {
                    if (signal.key in used) continue
                    val opp = signal.opposite?.let { oppKey -> signals.find { it.key == oppKey } }
                    if (opp != null) {
                        pairs.add(SignalPair(left = signal, right = opp))
                        used.add(signal.key)
                        used.add(opp.key)
                    } else {
                        pairs.add(SignalPair(left = signal, right = signal))
                        used.add(signal.key)
                    }
                }
                SignalGroup(category = cat, label = categoryLabels[cat] ?: cat.replaceFirstChar { it.uppercase() }, pairs = pairs)
            }
        }
    }
}

data class PostSignals(
    val signals: Map<String, Int>,
    val mySignals: List<String>
) {
    companion object {
        fun fromJson(json: JSONObject): PostSignals {
            val signalsObj = json.optJSONObject("signals") ?: JSONObject()
            val userArr = json.optJSONArray("my_signals") ?: json.optJSONArray("user_signals") ?: JSONArray()
            val map = mutableMapOf<String, Int>()
            signalsObj.keys().forEach { key -> map[key] = signalsObj.optInt(key, 0) }
            val userList = (0 until userArr.length()).map { userArr.getString(it) }
            return PostSignals(signals = map, mySignals = userList)
        }
    }
}

data class SyncSignalsResponse(
    val added: List<String>,
    val removed: List<String>,
    val signals: Map<String, Int>,
    val mySignals: List<String>
) {
    companion object {
        fun fromJson(json: JSONObject): SyncSignalsResponse {
            val addedArr = json.optJSONArray("added") ?: JSONArray()
            val removedArr = json.optJSONArray("removed") ?: JSONArray()
            val signalsObj = json.optJSONObject("signals") ?: JSONObject()
            val myArr = json.optJSONArray("my_signals") ?: JSONArray()
            val map = mutableMapOf<String, Int>()
            signalsObj.keys().forEach { key -> map[key] = signalsObj.optInt(key, 0) }
            return SyncSignalsResponse(
                added = (0 until addedArr.length()).map { addedArr.getString(it) },
                removed = (0 until removedArr.length()).map { removedArr.getString(it) },
                signals = map,
                mySignals = (0 until myArr.length()).map { myArr.getString(it) }
            )
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

fun toHashtag(text: String): String {
    val camel = text.split(" ").joinToString("") { word ->
        word.replaceFirstChar { it.uppercase() }
    }
    return "#$camel"
}

fun formatCompactNumber(value: Int): String = when {
    value < 1_000 -> "$value"
    value < 10_000 -> {
        val k = value / 100
        if (k % 10 == 0) "${k / 10}k" else "${k / 10}.${k % 10}k"
    }
    value < 1_000_000 -> "${value / 1_000}k"
    value < 10_000_000 -> {
        val m = value / 100_000
        if (m % 10 == 0) "${m / 10}M" else "${m / 10}.${m % 10}M"
    }
    else -> "${value / 1_000_000}M"
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
