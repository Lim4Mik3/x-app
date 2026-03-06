import Foundation

// MARK: - Change this to your API base URL
let API_BASE_URL = "http://localhost:8080/api"

enum ApiError: Error, LocalizedError {
    case http(Int, String)
    case network(Error)
    case decode(String)

    var errorDescription: String? {
        switch self {
        case .http(_, let msg): return msg
        case .network(let err): return err.localizedDescription
        case .decode(let msg): return msg
        }
    }
}

final class ApiClient {
    static let shared = ApiClient()
    private let session: URLSession = {
        let config = URLSessionConfiguration.default
        config.httpAdditionalHeaders = ["Accept-Language": ""]
        return URLSession(configuration: config)
    }()
    private let decoder = JSONDecoder()
    private init() {}

    // MARK: - Auth

    func socialLogin(provider: String, token: String) async throws -> AuthResponse {
        let body: [String: Any] = ["provider": provider, "token": token]
        let data = try await post(path: "/auth/social", body: body, auth: false)
        return try AuthResponse.from(data)
    }

    func refreshTokens() async throws -> TokenPair {
        let body: [String: Any] = ["refresh_token": TokenManager.shared.refreshToken ?? ""]
        let data = try await post(path: "/auth/refresh", body: body, auth: false)
        return try TokenPair.from(data)
    }

    func logout() async throws {
        let body: [String: Any] = ["refresh_token": TokenManager.shared.refreshToken ?? ""]
        _ = try await post(path: "/auth/logout", body: body)
    }

    func getMe() async throws -> ApiUser {
        let data = try await get(path: "/auth/me")
        return ApiUser.from(data)
    }

    // MARK: - Feed

    func getFeed(lat: Double, lng: Double, limit: Int = 20, cursor: String? = nil) async throws -> FeedResponse {
        var params = "lat=\(lat)&lng=\(lng)&limit=\(limit)"
        if let c = cursor { params += "&c=\(c)" }
        let data = try await get(path: "/feed?\(params)")
        return try FeedResponse.from(data)
    }

    // MARK: - Post

    func getMyPosts() async throws -> [ApiFeedPost] {
        let data = try await get(path: "/posts/me")
        guard let arr = data["posts"] as? [[String: Any]] else { return [] }
        return arr.compactMap { ApiFeedPost.from($0) }
    }

    func createPost(text: String, lat: Double, lng: Double) async throws -> [String: Any] {
        let body: [String: Any] = ["post": text, "lat": lat, "lng": lng]
        return try await post(path: "/post", body: body)
    }

    // MARK: - Comments

    func getComments(postId: String) async throws -> [ApiComment] {
        let data = try await get(path: "/post/\(postId)/comments")
        if let arr = data["comments"] as? [[String: Any]] ?? data["data"] as? [[String: Any]] {
            return arr.compactMap { ApiComment.from($0) }
        }
        return []
    }

    func addComment(postId: String, text: String, authorName: String, parentId: String? = nil) async throws -> ApiComment {
        var body: [String: Any] = ["text": text, "author_name": authorName]
        if let pid = parentId { body["parent_id"] = pid }
        let data = try await post(path: "/post/\(postId)/comment", body: body)
        guard let comment = ApiComment.from(data) else { throw ApiError.decode("Invalid comment") }
        return comment
    }

    func voteComment(commentId: String, voteType: String) async throws {
        _ = try await post(path: "/comment/\(commentId)/vote/\(voteType)", body: [:])
    }

    func deleteComment(commentId: String) async throws {
        _ = try await request(method: "DELETE", path: "/comment/\(commentId)", body: nil)
    }

    // MARK: - Signals

    func getSignalKeys(type: String? = nil) async throws -> [SignalKey] {
        let params = type != nil ? "?type=\(type!)" : ""
        let data = try await get(path: "/signal/keys\(params)")
        guard let arr = data["signals"] as? [[String: Any]] else { return [] }
        return arr.compactMap { SignalKey.from($0) }
    }

    func getPostSignals(postId: String) async throws -> PostSignals {
        let data = try await get(path: "/post/\(postId)/signals")
        return PostSignals.from(data)
    }

    func addSignal(postId: String, signalKey: String) async throws {
        _ = try await post(path: "/post/\(postId)/signal", body: ["signal_key": signalKey])
    }

    func removeSignal(postId: String, signalKey: String) async throws {
        _ = try await request(method: "DELETE", path: "/post/\(postId)/signal", body: ["signal_key": signalKey])
    }

    // MARK: - Reports

    func getReportReasons() async throws -> [ReportReason] {
        let data = try await get(path: "/report/reasons")
        guard let arr = data["reasons"] as? [[String: Any]] else { return [] }
        return arr.compactMap { ReportReason.from($0) }
    }

    func reportPost(postId: String, reason: String, detail: String? = nil) async throws -> [String: Any] {
        var body: [String: Any] = ["reason": reason]
        if let d = detail { body["detail"] = d }
        return try await post(path: "/post/\(postId)/report", body: body)
    }

    // MARK: - Internal

    private func get(path: String, auth: Bool = true) async throws -> [String: Any] {
        return try await request(method: "GET", path: path, body: nil, auth: auth)
    }

    private func post(path: String, body: [String: Any], auth: Bool = true) async throws -> [String: Any] {
        return try await request(method: "POST", path: path, body: body, auth: auth)
    }

    private func request(method: String, path: String, body: [String: Any]?, auth: Bool = true) async throws -> [String: Any] {
        guard let url = URL(string: "\(API_BASE_URL)\(path)") else {
            throw ApiError.decode("Invalid URL")
        }

        var req = URLRequest(url: url)
        req.httpMethod = method
        req.setValue("application/json", forHTTPHeaderField: "Content-Type")
        if auth, let token = TokenManager.shared.accessToken {
            req.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        #if DEBUG
        req.setValue("dev-user-001", forHTTPHeaderField: "X-Dev-User-ID")
        #endif

        if let body = body, method != "GET" {
            req.httpBody = try JSONSerialization.data(withJSONObject: body)
        }

        #if DEBUG
        print("📡 [\(method)] \(url.absoluteString)")
        if let body = body, method != "GET" {
            print("   Body: \(body)")
        }
        #endif

        let (data, response) = try await session.data(for: req)

        guard let http = response as? HTTPURLResponse else {
            throw ApiError.decode("Invalid response")
        }

        #if DEBUG
        print("   ← \(http.statusCode) (\(data.count) bytes)")
        if let responseStr = String(data: data, encoding: .utf8) {
            print("   Response: \(responseStr)")
        }
        #endif

        // Handle 401 with token refresh
        if http.statusCode == 401 && auth && !path.contains("/auth/refresh") {
            if let _ = TokenManager.shared.refreshToken {
                do {
                    let tokens = try await refreshTokens()
                    TokenManager.shared.saveAuth(accessToken: tokens.accessToken, refreshToken: tokens.refreshToken)
                    return try await request(method: method, path: path, body: body, auth: auth)
                } catch {
                    TokenManager.shared.clear()
                    throw ApiError.http(401, "Session expired")
                }
            }
        }

        if http.statusCode == 204 { return [:] }

        guard (200...299).contains(http.statusCode) else {
            let msg = (try? JSONSerialization.jsonObject(with: data) as? [String: Any])?["error"] as? String
                ?? String(data: data, encoding: .utf8) ?? "HTTP \(http.statusCode)"
            throw ApiError.http(http.statusCode, msg)
        }

        if data.isEmpty { return [:] }

        let json = try JSONSerialization.jsonObject(with: data)
        if let dict = json as? [String: Any] { return dict }
        if let arr = json as? [[String: Any]] { return ["data": arr] }
        return [:]
    }
}
