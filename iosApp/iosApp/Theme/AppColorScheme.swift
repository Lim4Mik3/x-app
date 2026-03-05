import SwiftUI

struct AppColorScheme {
    let textPrimary: Color
    let textSecondary: Color
    let background: Color
    let surface: Color
    let divider: Color
    let avatarBackground: Color
    let badgeBackground: Color
    let badgeText: Color
    let accent: Color
    let destructive: Color
    let onAccent: Color
    let onDestructive: Color
    let tabActive: Color
    let tabInactive: Color
}

extension AppColorScheme {
    static let light = AppColorScheme(
        textPrimary: Color(red: 0.10, green: 0.10, blue: 0.10),
        textSecondary: Color(red: 0.56, green: 0.56, blue: 0.58),
        background: .white,
        surface: .white,
        divider: Color(red: 0.90, green: 0.90, blue: 0.92),
        avatarBackground: Color(red: 0.91, green: 0.91, blue: 0.93),
        badgeBackground: Color(red: 0.91, green: 0.96, blue: 0.91),
        badgeText: Color(red: 0.18, green: 0.49, blue: 0.20),
        accent: Color(red: 0.145, green: 0.388, blue: 0.922),
        destructive: .red,
        onAccent: .white,
        onDestructive: .white,
        tabActive: Color(red: 0.10, green: 0.10, blue: 0.10),
        tabInactive: Color(red: 0.56, green: 0.56, blue: 0.58)
    )

    static let dark = AppColorScheme(
        textPrimary: Color(red: 0.96, green: 0.96, blue: 0.96),
        textSecondary: Color(red: 0.56, green: 0.56, blue: 0.58),
        background: .black,
        surface: Color(red: 0.11, green: 0.11, blue: 0.12),
        divider: Color(red: 0.22, green: 0.22, blue: 0.23),
        avatarBackground: Color(red: 0.17, green: 0.17, blue: 0.18),
        badgeBackground: Color(red: 0.11, green: 0.23, blue: 0.11),
        badgeText: Color(red: 0.51, green: 0.78, blue: 0.52),
        accent: Color(red: 0.36, green: 0.61, blue: 0.97),
        destructive: Color(red: 1.0, green: 0.41, blue: 0.38),
        onAccent: .white,
        onDestructive: .white,
        tabActive: Color(red: 0.96, green: 0.96, blue: 0.96),
        tabInactive: Color(red: 0.56, green: 0.56, blue: 0.58)
    )
}
