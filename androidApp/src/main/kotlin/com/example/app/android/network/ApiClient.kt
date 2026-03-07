package com.example.app.android.network

import com.example.app.android.network.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object ApiClient {

    // ── Change this to your API base URL ──
    const val BASE_URL = "http://10.0.2.2:8080/api"

    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor())
            .authenticator(TokenRefreshAuthenticator())
            .build()
    }

    // ── Auth ──

    suspend fun socialLogin(provider: String, token: String): Result<AuthResponse> = post(
        "/auth/social",
        JSONObject().put("provider", provider).put("token", token)
    ) { AuthResponse.fromJson(it) }

    suspend fun refreshTokens(): Result<TokenPair> = post(
        "/auth/refresh",
        JSONObject().put("refresh_token", TokenManager.refreshToken)
    ) { TokenPair.fromJson(it) }

    suspend fun logout(): Result<Unit> = post(
        "/auth/logout",
        JSONObject().put("refresh_token", TokenManager.refreshToken)
    ) { }

    suspend fun getMe(): Result<User> = get("/auth/me") { User.fromJson(it) }

    suspend fun updateMe(fields: Map<String, String?>): Result<User> {
        val body = JSONObject()
        fields.forEach { (k, v) -> body.put(k, v ?: JSONObject.NULL) }
        return put("/auth/me", body) { User.fromJson(it) }
    }

    // ── Feed ──

    suspend fun getFeed(
        lat: Double,
        lng: Double,
        limit: Int = 20,
        cursor: String? = null
    ): Result<FeedResponse> {
        val params = buildString {
            append("lat=$lat&lng=$lng&limit=$limit")
            if (cursor != null) append("&c=$cursor")
        }
        return get("/feed?$params") { FeedResponse.fromJson(it) }
    }

    // ── Post ──

    suspend fun createPost(text: String, lat: Double, lng: Double): Result<CreatedPost> = post(
        "/post",
        JSONObject().put("post", text).put("lat", lat).put("lng", lng)
    ) { CreatedPost.fromJson(it) }

    suspend fun getMyPosts(): Result<List<FeedPost>> = get("/posts/me") { json ->
        val arr = json.optJSONArray("posts") ?: JSONArray()
        (0 until arr.length()).map { FeedPost.fromJson(arr.getJSONObject(it)) }
    }

    // ── Comments ──

    suspend fun getComments(postId: String): Result<List<Comment>> = get(
        "/post/$postId/comments"
    ) { json ->
        val arr = json.optJSONArray("comments") ?: json.optJSONArray("data") ?: run {
            // Response might be a raw array - try parsing the body directly
            Comment.listFromJson(JSONArray(json.toString()))
        }
        if (arr is JSONArray) Comment.listFromJson(arr) else emptyList()
    }

    suspend fun addComment(
        postId: String,
        text: String,
        authorName: String,
        parentId: String? = null
    ): Result<Comment> {
        val body = JSONObject()
            .put("text", text)
            .put("author_name", authorName)
        if (parentId != null) body.put("parent_id", parentId)
        return post("/post/$postId/comment", body) { Comment.fromJson(it) }
    }

    suspend fun voteComment(commentId: String, voteType: String): Result<Unit> = post(
        "/comment/$commentId/vote/$voteType", JSONObject()
    ) { }

    suspend fun deleteComment(commentId: String): Result<Unit> = delete("/comment/$commentId")

    // ── Signals ──

    suspend fun getAllSignalKeys(): Result<Map<String, List<SignalKey>>> {
        val lang = java.util.Locale.getDefault().toLanguageTag()
        return request("GET", "/signal/keys/all", null, { json ->
            val result = mutableMapOf<String, List<SignalKey>>()
            json.keys().forEach { typeKey ->
                val arr = json.optJSONArray(typeKey) ?: return@forEach
                result[typeKey] = (0 until arr.length()).map { SignalKey.fromJson(arr.getJSONObject(it)) }
            }
            result
        }, extraHeaders = mapOf("Accept-Language" to lang))
    }

    suspend fun getSignalKeys(type: String? = null): Result<List<SignalKey>> {
        val params = if (type != null) "?type=$type" else ""
        return get("/signal/keys$params") { json ->
            val arr = json.optJSONArray("signals") ?: JSONArray()
            (0 until arr.length()).map { SignalKey.fromJson(arr.getJSONObject(it)) }
        }
    }

    suspend fun getPostSignals(postId: String): Result<PostSignals> = get(
        "/post/$postId/signals"
    ) { PostSignals.fromJson(it) }

    suspend fun syncSignals(postId: String, signalKeys: List<String>): Result<SyncSignalsResponse> {
        val body = JSONObject()
        body.put("signal_keys", JSONArray(signalKeys))
        return put("/post/$postId/signals", body) { SyncSignalsResponse.fromJson(it) }
    }

    // ── Reports ──

    suspend fun getReportReasons(): Result<List<ReportReason>> = get(
        "/report/reasons"
    ) { json ->
        val arr = json.optJSONArray("reasons") ?: JSONArray()
        (0 until arr.length()).map { ReportReason.fromJson(arr.getJSONObject(it)) }
    }

    suspend fun reportPost(
        postId: String,
        reason: String,
        detail: String? = null
    ): Result<ReportResult> {
        val body = JSONObject().put("reason", reason)
        if (detail != null) body.put("detail", detail)
        return post("/post/$postId/report", body) { ReportResult.fromJson(it) }
    }

    // ── Internal helpers ──

    private suspend fun <T> get(
        path: String,
        parse: (JSONObject) -> T
    ): Result<T> = request("GET", path, null, parse)

    private suspend fun <T> post(
        path: String,
        body: JSONObject,
        parse: (JSONObject) -> T
    ): Result<T> = request("POST", path, body, parse)

    private suspend fun <T> put(
        path: String,
        body: JSONObject,
        parse: (JSONObject) -> T
    ): Result<T> = request("PUT", path, body, parse)

    private suspend fun delete(path: String): Result<Unit> =
        request("DELETE", path, null, { })

    private suspend fun <T> request(
        method: String,
        path: String,
        body: JSONObject?,
        parse: (JSONObject) -> T,
        extraHeaders: Map<String, String>? = null
    ): Result<T> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL$path"
            val requestBody = body?.toString()?.toRequestBody(JSON_MEDIA)

            val request = Request.Builder().url(url).apply {
                when (method) {
                    "GET" -> get()
                    "POST" -> post(requestBody ?: "".toRequestBody(JSON_MEDIA))
                    "PUT" -> put(requestBody ?: "".toRequestBody(JSON_MEDIA))
                    "DELETE" -> if (requestBody != null) delete(requestBody) else delete()
                }
                extraHeaders?.forEach { (key, value) -> header(key, value) }
            }.build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            android.util.Log.d("ApiClient", "[$method] $url → ${response.code}")
            android.util.Log.d("ApiClient", "Response: $responseBody")

            if (!response.isSuccessful) {
                val errorMsg = responseBody?.let {
                    try { JSONObject(it).optString("error", it) } catch (_: Exception) { it }
                } ?: "HTTP ${response.code}"
                return@withContext Result.failure(ApiException(response.code, errorMsg))
            }

            if (response.code == 204 || responseBody.isNullOrBlank()) {
                @Suppress("UNCHECKED_CAST")
                return@withContext Result.success(Unit as T)
            }

            val json = try {
                JSONObject(responseBody)
            } catch (_: Exception) {
                // If response is a JSON array, wrap it
                JSONObject().put("data", JSONArray(responseBody))
            }

            Result.success(parse(json))
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class ApiException(val code: Int, message: String) : Exception(message)

private class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()

        TokenManager.accessToken?.let {
            builder.header("Authorization", "Bearer $it")
        }

        if (com.example.app.android.DevConfig.BYPASS_API_AUTH) {
            builder.header(
                com.example.app.android.DevConfig.DEV_BYPASS_HEADER,
                com.example.app.android.DevConfig.DEV_BYPASS_VALUE
            )
        }

        return chain.proceed(builder.build())
    }
}

private class TokenRefreshAuthenticator : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code != 401) return null

        val refreshToken = TokenManager.refreshToken ?: return null

        // Avoid infinite loops
        if (response.request.url.encodedPath.contains("/auth/refresh")) return null

        synchronized(this) {
            // Try refreshing
            val refreshBody = JSONObject()
                .put("refresh_token", refreshToken)
                .toString()
                .toRequestBody("application/json".toMediaType())

            val refreshRequest = Request.Builder()
                .url("${ApiClient.BASE_URL}/auth/refresh")
                .post(refreshBody)
                .build()

            val client = OkHttpClient()
            val refreshResponse = client.newCall(refreshRequest).execute()
            val body = refreshResponse.body?.string()

            if (refreshResponse.isSuccessful && body != null) {
                val tokens = TokenPair.fromJson(JSONObject(body))
                TokenManager.saveAuth(tokens.accessToken, tokens.refreshToken)

                return response.request.newBuilder()
                    .header("Authorization", "Bearer ${tokens.accessToken}")
                    .build()
            }

            // Refresh failed — clear tokens
            TokenManager.clear()
            return null
        }
    }
}
