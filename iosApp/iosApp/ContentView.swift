import SwiftUI

struct ContentView: View {
    @State private var isLoggedIn = TokenManager.shared.isLoggedIn

    var body: some View {
        if isLoggedIn {
            MainScreen(onLogout: { isLoggedIn = false })
        } else {
            LoginScreen(onLoginSuccess: { isLoggedIn = true })
        }
    }
}
