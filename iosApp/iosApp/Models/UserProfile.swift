import Foundation

struct UserProfile {
    let name: String
    let email: String
    let phone: String
    let address: String
    let location: String
    let initials: String
    let preferences: [UserPreference]
}

struct UserPreference: Identifiable {
    let id: String
    let key: String
    let label: String
    var enabled: Bool
}
