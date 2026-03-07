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
    let name: String?
    let email: String?
    let phone: String?
    let avatarUrl: String?
    let locale: String?
    let addresses: [ApiAddress]
    let accounts: [ApiAccount]

    var displayName: String? { name }

    var primaryAddress: ApiAddress? { addresses.first { $0.isPrimary } ?? addresses.first }

    var initials: String {
        let parts = (name ?? "").split(separator: " ")
        if parts.count >= 2 {
            return "\(parts[0].prefix(1))\(parts[1].prefix(1))".uppercased()
        }
        return String((name ?? "?").prefix(2)).uppercased()
    }

    static func from(_ dict: [String: Any]) -> ApiUser {
        let addrs = (dict["addresses"] as? [[String: Any]] ?? []).compactMap { ApiAddress.from($0) }
        let accs = (dict["accounts"] as? [[String: Any]] ?? []).compactMap { ApiAccount.from($0) }
        return ApiUser(
            id: dict["id"] as? String ?? "",
            name: dict["name"] as? String ?? dict["display_name"] as? String,
            email: dict["email"] as? String,
            phone: dict["phone"] as? String,
            avatarUrl: dict["avatar_url"] as? String ?? dict["avatar"] as? String,
            locale: dict["locale"] as? String,
            addresses: addrs,
            accounts: accs
        )
    }
}

struct ApiAddress {
    let label: String
    let street: String
    let number: String
    let complement: String?
    let neighborhood: String
    let city: String
    let state: String
    let country: String
    let zipCode: String
    let isPrimary: Bool

    var formatted: String { "\(street), \(number)" }
    var location: String { "\(city), \(state)" }

    static func from(_ dict: [String: Any]) -> ApiAddress? {
        ApiAddress(
            label: dict["label"] as? String ?? "",
            street: dict["street"] as? String ?? "",
            number: dict["number"] as? String ?? "",
            complement: dict["complement"] as? String,
            neighborhood: dict["neighborhood"] as? String ?? "",
            city: dict["city"] as? String ?? "",
            state: dict["state"] as? String ?? "",
            country: dict["country"] as? String ?? "",
            zipCode: dict["zip_code"] as? String ?? "",
            isPrimary: dict["is_primary"] as? Bool ?? false
        )
    }
}

struct ApiAccount {
    let provider: String
    let connectedAt: String

    static func from(_ dict: [String: Any]) -> ApiAccount? {
        guard let provider = dict["provider"] as? String else { return nil }
        return ApiAccount(
            provider: provider,
            connectedAt: dict["connected_at"] as? String ?? ""
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
    let content: String
    let type: String
    let typeKey: String
    let typeColor: String?
    let categories: [String]
    let postedAt: String
    var signalsCount: Int
    let commentsCount: Int
    let distance: String?

    var timeAgo: String { formatTimeAgo(postedAt) }

    static func from(_ dict: [String: Any]) -> ApiFeedPost? {
        guard let id = dict["id"] as? String else { return nil }
        let cats = dict["categories"] as? [String] ?? []
        return ApiFeedPost(
            id: id,
            content: dict["content"] as? String ?? "",
            type: dict["type"] as? String ?? "",
            typeKey: dict["type_key"] as? String ?? "",
            typeColor: dict["type_color"] as? String,
            categories: cats,
            postedAt: dict["posted_at"] as? String ?? "",
            signalsCount: dict["signals_count"] as? Int ?? 0,
            commentsCount: dict["comments_count"] as? Int ?? 0,
            distance: dict["distance"] as? String
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
    let opposite: String?
    var id: String { key }

    static func from(_ dict: [String: Any]) -> SignalKey? {
        guard let key = dict["key"] as? String else { return nil }
        return SignalKey(
            key: key,
            label: dict["label"] as? String ?? key.replacingOccurrences(of: "_", with: " ").capitalized,
            category: dict["category"] as? String ?? "",
            opposite: dict["opposite"] as? String
        )
    }
}

struct SignalPair: Identifiable {
    let left: SignalKey
    let right: SignalKey
    var id: String { "\(left.key)-\(right.key)" }
}

struct SignalGroup: Identifiable {
    let category: String
    let label: String
    let pairs: [SignalPair]
    var id: String { category }

    static func groupFromKeys(_ keys: [SignalKey]) -> [SignalGroup] {
        let byCategory = Dictionary(grouping: keys, by: { $0.category })
        let categoryOrder = ["verification", "reaction"]
        let categoryLabels = ["verification": "Verificação", "reaction": "Reação"]

        return categoryOrder.compactMap { cat in
            guard let signals = byCategory[cat], !signals.isEmpty else { return nil }
            var pairs: [SignalPair] = []
            var used = Set<String>()
            for signal in signals {
                guard !used.contains(signal.key) else { continue }
                if let oppKey = signal.opposite,
                   let opp = signals.first(where: { $0.key == oppKey }) {
                    pairs.append(SignalPair(left: signal, right: opp))
                    used.insert(signal.key)
                    used.insert(opp.key)
                } else {
                    pairs.append(SignalPair(left: signal, right: signal))
                    used.insert(signal.key)
                }
            }
            return SignalGroup(category: cat, label: categoryLabels[cat] ?? cat.capitalized, pairs: pairs)
        }
    }
}

struct PostSignals {
    let signals: [String: Int]
    let mySignals: [String]

    static func from(_ dict: [String: Any]) -> PostSignals {
        let signalsDict = dict["signals"] as? [String: Int] ?? [:]
        let userArr = dict["my_signals"] as? [String] ?? dict["user_signals"] as? [String] ?? []
        return PostSignals(signals: signalsDict, mySignals: userArr)
    }
}

struct SyncSignalsResponse {
    let added: [String]
    let removed: [String]
    let signals: [String: Int]
    let mySignals: [String]

    static func from(_ dict: [String: Any]) -> SyncSignalsResponse {
        return SyncSignalsResponse(
            added: dict["added"] as? [String] ?? [],
            removed: dict["removed"] as? [String] ?? [],
            signals: dict["signals"] as? [String: Int] ?? [:],
            mySignals: dict["my_signals"] as? [String] ?? []
        )
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

func formatCompactNumber(_ value: Int) -> String {
    switch value {
    case ..<1_000:
        return "\(value)"
    case ..<10_000:
        let k = Double(value) / 1_000
        return k.truncatingRemainder(dividingBy: 1) == 0
            ? "\(Int(k))k"
            : String(format: "%.1fk", k)
    case ..<1_000_000:
        return "\(value / 1_000)k"
    case ..<10_000_000:
        let m = Double(value) / 1_000_000
        return m.truncatingRemainder(dividingBy: 1) == 0
            ? "\(Int(m))M"
            : String(format: "%.1fM", m)
    default:
        return "\(value / 1_000_000)M"
    }
}

func toHashtag(_ text: String) -> String {
    let camel = text.split(separator: " ").map { word in
        word.prefix(1).uppercased() + word.dropFirst()
    }.joined()
    return "#\(camel)"
}

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
