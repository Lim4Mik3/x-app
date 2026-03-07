import Foundation

final class SignalKeysCache {
    static let shared = SignalKeysCache()
    private init() {}

    private let defaults = UserDefaults.standard
    private let cacheKey = "signal_keys_cache"
    private let ttl: TimeInterval = 24 * 60 * 60 // 24 hours

    private var memoryCache: [String: [SignalKey]]?

    /// Returns signal keys for a given type key (e.g. "INFORMATION", "ALERT").
    /// Uses memory cache > disk cache > API fetch.
    /// Returns nil if the type key is not recognized.
    func getKeys(for typeKey: String) async -> [SignalKey]? {
        // Memory cache
        if let cached = memoryCache {
            return cached[typeKey]
        }

        // Disk cache
        let currentLocale = LanguageManager.shared.currentLanguage
        if let disk = loadFromDisk(), !disk.isExpired, disk.locale == currentLocale {
            memoryCache = disk.signals
            return disk.signals[typeKey]
        }

        // Fetch from API
        await refresh()
        let result = memoryCache ?? loadFromDisk()?.signals ?? [:]
        return result[typeKey]
    }

    /// Force refresh from API. Called on app launch or language change.
    func refresh() async {
        do {
            let allSignals = try await ApiClient.shared.getAllSignalKeys()
            #if DEBUG
            print("📦 SignalKeysCache fetched \(allSignals.count) types: \(allSignals.keys.sorted())")
            for (type, keys) in allSignals {
                print("   \(type): \(keys.map { $0.key })")
            }
            #endif
            memoryCache = allSignals
            saveToDisk(signals: allSignals, locale: LanguageManager.shared.currentLanguage)
        } catch {
            // Fallback: load stale cache if available
            if let disk = loadFromDisk() {
                memoryCache = disk.signals
            }
            #if DEBUG
            print("📦 SignalKeysCache refresh failed: \(error)")
            #endif
        }
    }

    /// Invalidate cache (e.g. on language change).
    func invalidate() {
        memoryCache = nil
    }

    // MARK: - Disk persistence

    private struct DiskCache: Codable {
        let fetchedAt: Date
        let locale: String
        let signals: [String: [CodableSignalKey]]

        var isExpired: Bool {
            Date().timeIntervalSince(fetchedAt) > 24 * 60 * 60
        }
    }

    private struct CodableSignalKey: Codable {
        let key: String
        let label: String
        let category: String
        let opposite: String?
    }

    private func saveToDisk(signals: [String: [SignalKey]], locale: String) {
        let codable = DiskCache(
            fetchedAt: Date(),
            locale: locale,
            signals: signals.mapValues { keys in
                keys.map { CodableSignalKey(key: $0.key, label: $0.label, category: $0.category, opposite: $0.opposite) }
            }
        )
        if let data = try? JSONEncoder().encode(codable) {
            defaults.set(data, forKey: cacheKey)
        }
    }

    private func loadFromDisk() -> (signals: [String: [SignalKey]], locale: String, isExpired: Bool)? {
        guard let data = defaults.data(forKey: cacheKey),
              let cache = try? JSONDecoder().decode(DiskCache.self, from: data) else { return nil }
        let signals = cache.signals.mapValues { keys in
            keys.map { SignalKey(key: $0.key, label: $0.label, category: $0.category, opposite: $0.opposite) }
        }
        return (signals: signals, locale: cache.locale, isExpired: cache.isExpired)
    }
}
