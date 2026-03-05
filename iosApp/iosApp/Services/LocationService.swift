import Foundation
import CoreLocation

final class LocationService: NSObject, ObservableObject, CLLocationManagerDelegate {

    static let shared = LocationService()

    private let manager = CLLocationManager()
    private var lastFetchTime: Date?
    private var ttl: TimeInterval = 30 // 30 seconds default

    @Published var location: CLLocation?
    @Published var authorized = false

    var cachedLocation: CLLocation? { location }

    var isCacheValid: Bool {
        guard let location = location, let lastFetch = lastFetchTime else { return false }
        return Date().timeIntervalSince(lastFetch) < ttl
    }

    private override init() {
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyBest
    }

    // MARK: - Public API

    func requestPermission() {
        manager.requestWhenInUseAuthorization()
    }

    /// Returns cached location if valid, otherwise fetches fresh.
    func fetch(forceRefresh: Bool = false) {
        guard authorized else { return }
        if !forceRefresh && isCacheValid { return }
        manager.requestLocation()
    }

    func setTtl(_ seconds: TimeInterval) {
        ttl = seconds
    }

    // MARK: - Distance helpers

    static func distanceBetween(from: CLLocationCoordinate2D, to: CLLocationCoordinate2D) -> Double {
        let a = CLLocation(latitude: from.latitude, longitude: from.longitude)
        let b = CLLocation(latitude: to.latitude, longitude: to.longitude)
        return a.distance(from: b)
    }

    static func formatDistance(_ meters: Double) -> String {
        if meters < 10 {
            return "Menos de 10 metros do ocorrido"
        } else if meters < 1000 {
            return "A \(Int(meters.rounded())) metros do ocorrido"
        } else {
            return String(format: "A %.1f km do ocorrido", meters / 1000)
        }
    }

    // MARK: - CLLocationManagerDelegate

    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        let status = manager.authorizationStatus
        authorized = status == .authorizedWhenInUse || status == .authorizedAlways
        if authorized && location == nil {
            fetch()
        }
    }

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let latest = locations.last else { return }
        location = latest
        lastFetchTime = Date()
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        // Silent fail — location is optional
    }
}
