import SwiftUI
import GoogleSignIn

@main
struct iOSApp: App {
    @StateObject private var languageManager = LanguageManager.shared

    var body: some Scene {
        WindowGroup {
            ContentView()
                .withAppTheme()
                .environmentObject(languageManager)
                .onOpenURL { url in
                    GIDSignIn.sharedInstance.handle(url)
                }
                .task {
                    await SignalKeysCache.shared.refresh()
                }
        }
    }
}

