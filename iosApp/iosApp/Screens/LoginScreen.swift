import SwiftUI
import AuthenticationServices

struct LoginScreen: View {
    var onLoginSuccess: () -> Void
    var onDismiss: (() -> Void)? = nil

    @Environment(\.appColors) private var colors
    @Environment(\.colorScheme) private var colorScheme
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var animating = false

    var body: some View {
        ZStack {
            // Animated gradient background
            ZStack {
                Color.black.opacity(0.1)

                Ellipse()
                    .fill(colors.accent.opacity(0.35))
                    .frame(width: 340, height: 220)
                    .blur(radius: 80)
                    .rotationEffect(.degrees(animating ? 25 : -15))
                    .offset(
                        x: animating ? 140 : -120,
                        y: animating ? -280 : 200
                    )

                RoundedRectangle(cornerRadius: 60)
                    .fill(Color.purple.opacity(0.3))
                    .frame(width: 200, height: 300)
                    .blur(radius: 70)
                    .rotationEffect(.degrees(animating ? -20 : 30))
                    .offset(
                        x: animating ? -150 : 130,
                        y: animating ? 250 : -220
                    )

                Capsule()
                    .fill(Color.pink.opacity(0.25))
                    .frame(width: 180, height: 280)
                    .blur(radius: 60)
                    .rotationEffect(.degrees(animating ? 40 : -25))
                    .offset(
                        x: animating ? 120 : -100,
                        y: animating ? 200 : -260
                    )
            }
            .ignoresSafeArea()
            .background(.ultraThinMaterial)
            .ignoresSafeArea()
            .onTapGesture { onDismiss?() }
            .onAppear {
                withAnimation(.easeInOut(duration: 4).repeatForever(autoreverses: true)) {
                    animating = true
                }
            }

            // Card
            VStack(alignment: .leading, spacing: 0) {
                // Header
                Text(NSLocalizedString("login_title", comment: ""))
                    .font(.system(size: 26, weight: .bold))
                    .foregroundColor(colors.textPrimary)

                Text(NSLocalizedString("login_subtitle", comment: ""))
                    .font(.system(size: 16))
                    .foregroundColor(colors.textSecondary)
                    .padding(.top, 8)

                // Divider
                Rectangle()
                    .fill(colors.divider)
                    .frame(height: 0.5)
                    .padding(.top, 24)

                Text(NSLocalizedString("login_header", comment: ""))
                    .font(.system(size: 14))
                    .foregroundColor(colors.textSecondary)
                    .padding(.top, 16)

                // Buttons
                VStack(spacing: 10) {
                    SignInWithAppleButton(.continue) { request in
                        request.requestedScopes = [.fullName, .email]
                    } onCompletion: { result in
                        handleAppleSignIn(result)
                    }
                    .signInWithAppleButtonStyle(colorScheme == .dark ? .white : .black)
                    .frame(height: 48)
                    .cornerRadius(12)

                    Button {
                        // TODO: Google sign in
                    } label: {
                        HStack(spacing: 10) {
                            Image(systemName: "g.circle.fill")
                                .font(.system(size: 18))
                            Text(NSLocalizedString("login_google", comment: ""))
                                .font(.system(size: 15, weight: .medium))
                        }
                        .foregroundColor(colors.textPrimary)
                        .frame(maxWidth: .infinity)
                        .frame(height: 48)
                        .background(colorScheme == .dark ? Color.white.opacity(0.08) : Color.black.opacity(0.04))
                        .cornerRadius(12)
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(colors.divider, lineWidth: 0.5)
                        )
                    }
                    .disabled(isLoading)
                }
                .padding(.top, 20)

                // Error
                if let error = errorMessage {
                    Text(error)
                        .font(.system(size: 13))
                        .foregroundColor(colors.destructive)
                        .frame(maxWidth: .infinity)
                        .multilineTextAlignment(.center)
                        .padding(.top, 12)
                }

                // Divider
                Rectangle()
                    .fill(colors.divider)
                    .frame(height: 0.5)
                    .padding(.top, 20)

                // Community note
                Text(NSLocalizedString("login_community", comment: ""))
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(colors.textSecondary.opacity(colorScheme == .dark ? 0.6 : 0.85))
                    .frame(maxWidth: .infinity)
                    .multilineTextAlignment(.center)
                    .padding(.top, 14)

                // Terms
                Text(NSLocalizedString("login_terms", comment: ""))
                    .font(.system(size: 11))
                    .foregroundColor(colors.textSecondary.opacity(colorScheme == .dark ? 0.5 : 0.75))
                    .frame(maxWidth: .infinity)
                    .multilineTextAlignment(.center)
                    .padding(.top, 20)
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 28)
            .background(
                RoundedRectangle(cornerRadius: 20)
                    .fill(colors.surface)
                    .shadow(color: .black.opacity(0.15), radius: 20, y: 8)
            )
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(colors.divider, lineWidth: 0.5)
            )
            .padding(.horizontal, 24)

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
