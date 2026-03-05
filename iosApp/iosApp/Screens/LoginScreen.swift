import SwiftUI
import AuthenticationServices

struct LoginScreen: View {
    var onLoginSuccess: () -> Void

    @Environment(\.appColors) private var colors
    @State private var isLoading = false
    @State private var errorMessage: String?

    var body: some View {
        ZStack {
            colors.background.ignoresSafeArea()

            VStack(spacing: 0) {
                Spacer()

                // Branding
                Text("X")
                    .font(.system(size: 56, weight: .black))
                    .tracking(-2)
                    .foregroundColor(colors.textPrimary)

                Text("Sua comunidade, em tempo real")
                    .font(.system(size: 16))
                    .foregroundColor(colors.textSecondary)
                    .padding(.top, 8)

                Spacer().frame(height: 48)

                // Sign in with Apple
                SignInWithAppleButton(.signIn) { request in
                    request.requestedScopes = [.fullName, .email]
                } onCompletion: { _ in
                    // TODO: restore real auth when tokens are integrated
                    onLoginSuccess()
                }
                .signInWithAppleButtonStyle(
                    UITraitCollection.current.userInterfaceStyle == .dark ? .white : .black
                )
                .frame(height: 52)
                .cornerRadius(12)
                .padding(.horizontal, 32)
                .disabled(isLoading)

                // Error
                if let error = errorMessage {
                    Text(error)
                        .font(.system(size: 13))
                        .foregroundColor(colors.destructive)
                        .multilineTextAlignment(.center)
                        .padding(.top, 16)
                        .padding(.horizontal, 32)
                }

                Spacer()
            }

            if isLoading {
                ProgressView()
                    .tint(colors.accent)
            }
        }
    }

    private func handleAppleSignIn(_ result: Result<ASAuthorization, Error>) {
        switch result {
        case .success(let auth):
            guard let credential = auth.credential as? ASAuthorizationAppleIDCredential,
                  let tokenData = credential.identityToken,
                  let idToken = String(data: tokenData, encoding: .utf8) else {
                errorMessage = "Token não recebido da Apple"
                return
            }

            isLoading = true
            errorMessage = nil

            Task {
                do {
                    let response = try await ApiClient.shared.socialLogin(provider: "apple", token: idToken)
                    TokenManager.shared.saveAuth(
                        accessToken: response.accessToken,
                        refreshToken: response.refreshToken
                    )
                    TokenManager.shared.userId = response.user.id
                    TokenManager.shared.displayName = response.user.displayName ?? response.user.email

                    await MainActor.run {
                        isLoading = false
                        onLoginSuccess()
                    }
                } catch {
                    await MainActor.run {
                        isLoading = false
                        errorMessage = error.localizedDescription
                    }
                }
            }

        case .failure(let error):
            if (error as NSError).code != ASAuthorizationError.canceled.rawValue {
                errorMessage = "Erro na autenticação"
            }
        }
    }
}
