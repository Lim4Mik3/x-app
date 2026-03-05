import Foundation

final class TokenManager {
    static let shared = TokenManager()

    private let defaults = UserDefaults.standard
    private let accessKey = "x_access_token"
    private let refreshKey = "x_refresh_token"
    private let displayNameKey = "x_display_name"
    private let userIdKey = "x_user_id"

    private init() {}

    var accessToken: String? {
        get { defaults.string(forKey: accessKey) }
        set { defaults.set(newValue, forKey: accessKey) }
    }

    var refreshToken: String? {
        get { defaults.string(forKey: refreshKey) }
        set { defaults.set(newValue, forKey: refreshKey) }
    }

    var displayName: String? {
        get { defaults.string(forKey: displayNameKey) }
        set { defaults.set(newValue, forKey: displayNameKey) }
    }

    var userId: String? {
        get { defaults.string(forKey: userIdKey) }
        set { defaults.set(newValue, forKey: userIdKey) }
    }

    var isLoggedIn: Bool { accessToken != nil }

    func saveAuth(accessToken: String, refreshToken: String) {
        self.accessToken = accessToken
        self.refreshToken = refreshToken
    }

    func clear() {
        defaults.removeObject(forKey: accessKey)
        defaults.removeObject(forKey: refreshKey)
        defaults.removeObject(forKey: displayNameKey)
        defaults.removeObject(forKey: userIdKey)
    }
}
