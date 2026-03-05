import SwiftUI

struct ContentView: View {
    @State private var isLoggedIn = TokenManager.shared.isLoggedIn

    var body: some View {
        MainScreen(
            isLoggedIn: isLoggedIn,
            onLoginSuccess: { isLoggedIn = true },
            onLogout: { isLoggedIn = false }
        )
    }
}
