import SwiftUI

private struct AppColorsKey: EnvironmentKey {
    static let defaultValue = AppColorScheme.light
}

private struct AppSpacingKey: EnvironmentKey {
    static let defaultValue = AppSpacing()
}

extension EnvironmentValues {
    var appColors: AppColorScheme {
        get { self[AppColorsKey.self] }
        set { self[AppColorsKey.self] = newValue }
    }

    var appSpacing: AppSpacing {
        get { self[AppSpacingKey.self] }
        set { self[AppSpacingKey.self] = newValue }
    }
}

struct AppThemeModifier: ViewModifier {
    @Environment(\.colorScheme) private var colorScheme

    func body(content: Content) -> some View {
        content
            .environment(\.appColors, colorScheme == .dark ? .dark : .light)
            .environment(\.appSpacing, AppSpacing())
    }
}

extension View {
    func withAppTheme() -> some View {
        modifier(AppThemeModifier())
    }
}
