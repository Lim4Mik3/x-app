import SwiftUI

// Toggle this to bypass login during development
let DEV_SKIP_LOGIN = true

struct ContentView: View {
    @State private var isLoggedIn = DEV_SKIP_LOGIN || TokenManager.shared.isLoggedIn

    var body: some View {
        MainScreen(
            isLoggedIn: isLoggedIn,
            onLoginSuccess: { isLoggedIn = true },
            onLogout: { isLoggedIn = false }
        )
    }
}

