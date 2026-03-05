import SwiftUI

class LanguageManager: ObservableObject {
    static let shared = LanguageManager()

    @Published private(set) var bundle: Bundle
    @Published private(set) var currentLanguage: String

    private init() {
        let saved = UserDefaults.standard.string(forKey: "app_language") ?? "pt-BR"
        currentLanguage = saved
        bundle = LanguageManager.loadBundle(for: saved)
    }

    func setLanguage(_ code: String) {
        currentLanguage = code
        UserDefaults.standard.set(code, forKey: "app_language")
        bundle = LanguageManager.loadBundle(for: code)
    }

    func s(_ key: String) -> String {
        bundle.localizedString(forKey: key, value: nil, table: nil)
    }

    private static func loadBundle(for code: String) -> Bundle {
        if let path = Bundle.main.path(forResource: code, ofType: "lproj"),
           let bundle = Bundle(path: path) {
            return bundle
        }
        return .main
    }
}
