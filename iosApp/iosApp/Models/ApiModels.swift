import Foundation

// MARK: - Auth

struct AuthResponse {
    let user: ApiUser
    let accessToken: String
    let refreshToken: String
    let isNewUser: Bool

    static func from(_ dict: [String: Any]) throws -> AuthResponse {
        guard let userDict = dict["user"] as? [String: Any],
              let at = dict["access_token"] as? String,
              let rt = dict["refresh_token"] as? String else {
            throw ApiError.decode("Invalid auth response")
        }
        return AuthResponse(
            user: ApiUser.from(userDict),
            accessToken: at,
            refreshToken: rt,
            isNewUser: dict["is_new_user"] as? Bool ?? false
        )
    }
}

struct TokenPair {
    let accessToken: String
    let refreshToken: String

    static func from(_ dict: [String: Any]) throws -> TokenPair {
        guard let at = dict["access_token"] as? String,
              let rt = dict["refresh_token"] as? String else {
            throw ApiError.decode("Invalid token pair")
        }
        return TokenPair(accessToken: at, refreshToken: rt)
    }
}

struct ApiUser {
    let id: String
    let displayName: String?
    let email: String?
    let phone: String?
    let avatarUrl: String?
    let addressCity: String?
    let addressState: String?

    static func from(_ dict: [String: Any]) -> ApiUser {
        ApiUser(
            id: dict["id"] as? String ?? "",
            displayName: dict["display_name"] as? String,
            email: dict["email"] as? String,
            phone: dict["phone"] as? String,
            avatarUrl: dict["avatar_url"] as? String,
            addressCity: dict["address_city"] as? String,
            addressState: dict["address_state"] as? String
        )
    }
}

// MARK: - Feed

struct FeedResponse {
    let posts: [ApiFeedPost]
    let total: Int
    let nextCursor: String?
    let hasMore: Bool

    static func from(_ dict: [String: Any]) throws -> FeedResponse {
        guard let postsArr = dict["posts"] as? [[String: Any]] else {
            throw ApiError.decode("Invalid feed response")
        }
        return FeedResponse(
            posts: postsArr.compactMap { ApiFeedPost.from($0) },
            total: dict["total"] as? Int ?? 0,
            nextCursor: dict["next_cursor"] as? String,
            hasMore: dict["has_more"] as? Bool ?? false
        )
    }
}

struct ApiFeedPost: Identifiable {
    let id: String
    let originalText: String
    let normalizedText: String
    let type: String
    let typeConfidence: Double
    let categories: [String]
    let createdAt: String
    let latitude: Double?
    let longitude: Double?

    var timeAgo: String { formatTimeAgo(createdAt) }

    var typeLabel: String {
        switch type.uppercased() {
        case "NARRAR": return "Relato"
        case "ALERTAR": return "Alerta"
        case "VENDER": return "Comércio"
        case "INFORMAR": return "Informação"
        case "PEDIR": return "Pedido"
        case "RECLAMAR": return "Reclamação"
        case "DENUNCIAR": return "Denúncia"
        default: return type
        }
    }

    var categoryLabel: String? {
        guard let cat = categories.first else { return nil }
        switch cat.uppercased() {
        case "SEGURANCA_CRIME": return "Crime"
        case "INFRAESTRUTURA": return "Infraestrutura"
        case "TRANSITO": return "Trânsito"
        case "CLIMA": return "Clima"
        case "SAUDE": return "Saúde"
        case "COMUNIDADE": return "Comunidade"
        case "COMERCIO": return "Comércio"
        case "EVENTO": return "Evento"
        case "ANIMAL": return "Animais"
        case "ESPORTE": return "Esporte"
        case "TRANSPORTE": return "Transporte"
        default: return cat.replacingOccurrences(of: "_", with: " ").capitalized
        }
    }

    static func from(_ dict: [String: Any]) -> ApiFeedPost? {
        guard let id = dict["id"] as? String else { return nil }
        let cats = dict["categories"] as? [String] ?? []
        return ApiFeedPost(
            id: id,
            originalText: dict["original_text"] as? String ?? "",
            normalizedText: dict["normalized_text"] as? String ?? "",
            type: dict["type"] as? String ?? "",
            typeConfidence: dict["type_confidence"] as? Double ?? 0,
            categories: cats,
            createdAt: dict["created_at"] as? String ?? "",
            latitude: dict["latitude"] as? Double,
            longitude: dict["longitude"] as? Double
        )
    }
}

// MARK: - Comments

struct ApiComment: Identifiable {
    let id: String
    let postId: String
    let parentId: String?
    let authorName: String
    let text: String
    let upvotes: Int
    let downvotes: Int
    let createdAt: String
    let replies: [ApiComment]

    var timeAgo: String { formatTimeAgo(createdAt) }

    static func from(_ dict: [String: Any]) -> ApiComment? {
        guard let id = dict["id"] as? String else { return nil }
        let repliesArr = dict["replies"] as? [[String: Any]] ?? []
        return ApiComment(
            id: id,
            postId: dict["post_id"] as? String ?? "",
            parentId: dict["parent_id"] as? String,
            authorName: dict["author_name"] as? String ?? "",
            text: dict["text"] as? String ?? "",
            upvotes: dict["upvotes"] as? Int ?? 0,
            downvotes: dict["downvotes"] as? Int ?? 0,
            createdAt: dict["created_at"] as? String ?? "",
            replies: repliesArr.compactMap { from($0) }
        )
    }
}

// MARK: - Signals

struct SignalKey: Identifiable {
    let key: String
    let label: String
    let category: String
    let polarity: String
    var id: String { key }

    static func from(_ dict: [String: Any]) -> SignalKey? {
        guard let key = dict["key"] as? String else { return nil }
        return SignalKey(
            key: key,
            label: dict["label"] as? String ?? key.replacingOccurrences(of: "_", with: " ").capitalized,
            category: dict["category"] as? String ?? "",
            polarity: dict["polarity"] as? String ?? ""
        )
    }
}

struct PostSignals {
    let signals: [String: Int]
    let userSignals: [String]

    static func from(_ dict: [String: Any]) -> PostSignals {
        let signalsDict = dict["signals"] as? [String: Int] ?? [:]
        let userArr = dict["user_signals"] as? [String] ?? []
        return PostSignals(signals: signalsDict, userSignals: userArr)
    }
}

// MARK: - Reports

struct ReportReason: Identifiable {
    let key: String
    let label: String
    var id: String { key }

    static func from(_ dict: [String: Any]) -> ReportReason? {
        guard let key = dict["key"] as? String else { return nil }
        return ReportReason(
            key: key,
            label: dict["label"] as? String ?? key.replacingOccurrences(of: "_", with: " ").capitalized
        )
    }
}

// MARK: - Utils

private let isoFormatter: DateFormatter = {
    let f = DateFormatter()
    f.dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
    f.timeZone = TimeZone(identifier: "UTC")
    f.locale = Locale(identifier: "en_US_POSIX")
    return f
}()

func formatTimeAgo(_ isoDate: String) -> String {
    guard !isoDate.isEmpty else { return "" }
    let clean = isoDate
        .components(separatedBy: ".").first?
        .components(separatedBy: "Z").first ?? isoDate
    guard let date = isoFormatter.date(from: clean) else { return "" }
    let diff = Int(Date().timeIntervalSince(date)) / 60
    switch diff {
    case ..<1: return "agora"
    case ..<60: return "\(diff)min"
    case ..<1440: return "\(diff / 60)h"
    case ..<43200: return "\(diff / 1440)d"
    default: return "\(diff / 43200)m"
    }
}
