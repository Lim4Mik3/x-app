import SwiftUI

@main
struct iOSApp: App {
    @StateObject private var languageManager = LanguageManager.shared

    var body: some Scene {
        WindowGroup {
            ContentView()
                .withAppTheme()
                .environmentObject(languageManager)
        }
    }
}
