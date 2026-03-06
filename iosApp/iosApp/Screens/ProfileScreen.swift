import SwiftUI
import PhotosUI

struct ProfileScreen: View {
    @Environment(\.appColors) private var colors
    @EnvironmentObject var lang: LanguageManager

    var onLogout: () -> Void = {}

    @State private var user: ApiUser?
    @State private var isLoading = true
    @State private var notificationsEnabled = true
    @State private var photoData: Data?
    @State private var showPhotoPicker = false
    @State private var selectedPhotoItem: PhotosPickerItem?

    private let languages: [(tag: String, name: String)] = [
        ("pt-BR", "Português"),
        ("en", "English"),
        ("es", "Español")
    ]

    private var selectedLanguageName: String {
        languages.first { $0.tag == lang.currentLanguage }?.name ?? languages[0].name
    }

    private func isProviderConnected(_ provider: String) -> Bool {
        user?.accounts.contains { $0.provider == provider } ?? false
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                Spacer().frame(height: 60)

                if isLoading {
                    Spacer().frame(height: 100)
                    ProgressView()
                        .tint(colors.accent)
                } else if let user = user {
                    // --- Profile Header ---
                    ProfileHeader(
                        name: user.name ?? "",
                        email: user.email ?? "",
                        initials: user.initials,
                        photoData: photoData,
                        onAvatarTap: { showPhotoPicker = true }
                    )

                    Spacer().frame(height: 32)

                    // --- Personal Info Section ---
                    ProfileSection(title: lang.s("profile_section_personal")) {
                        ProfileInfoRow(
                            icon: "envelope",
                            label: lang.s("profile_email"),
                            value: user.email ?? "-"
                        )
                        ProfileDivider()
                        ProfileInfoRow(
                            icon: "phone",
                            label: lang.s("profile_phone"),
                            value: user.phone ?? "-"
                        )
                    }

                    Spacer().frame(height: 24)

                    // --- Location Section ---
                    if !user.addresses.isEmpty {
                        ProfileSection(title: lang.s("profile_section_location")) {
                            ForEach(Array(user.addresses.enumerated()), id: \.offset) { index, addr in
                                if index > 0 {
                                    ProfileDivider()
                                }
                                ProfileInfoRow(
                                    icon: addr.isPrimary ? "house.fill" : "briefcase.fill",
                                    label: addr.label,
                                    value: "\(addr.formatted) · \(addr.location)"
                                )
                            }
                        }

                        Spacer().frame(height: 24)
                    }

                    // --- Preferences Section ---
                    ProfileSection(title: lang.s("profile_section_preferences")) {
                        PreferenceToggleRow(
                            label: lang.s("pref_notifications"),
                            isOn: $notificationsEnabled
                        )
                    }

                    Spacer().frame(height: 24)

                    // --- Language Section (dropdown) ---
                    ProfileSection(title: lang.s("language_label")) {
                        Menu {
                            ForEach(languages, id: \.tag) { language in
                                Button {
                                    lang.setLanguage(language.tag)
                                } label: {
                                    HStack {
                                        Text(language.name)
                                        if lang.currentLanguage == language.tag {
                                            Image(systemName: "checkmark")
                                        }
                                    }
                                }
                            }
                        } label: {
                            HStack {
                                Text(selectedLanguageName)
                                    .font(.system(size: 15))
                                    .foregroundColor(colors.textPrimary)
                                Spacer()
                                Image(systemName: "chevron.down")
                                    .font(.system(size: 13))
                                    .foregroundColor(colors.textSecondary)
                            }
                            .padding(.horizontal, 16)
                            .padding(.vertical, 14)
                        }
                    }

                    Spacer().frame(height: 24)

                    // --- Connected Accounts Section ---
                    ProfileSection(title: lang.s("profile_section_connected")) {
                        ConnectedAccountRow(
                            icon: "envelope.fill",
                            name: lang.s("connected_google"),
                            connected: isProviderConnected("google"),
                            statusLabel: lang.s("connected_status"),
                            connectLabel: lang.s("connected_connect"),
                            onToggle: {}
                        )
                        ProfileDivider()
                        ConnectedAccountRow(
                            icon: "apple.logo",
                            name: lang.s("connected_apple"),
                            connected: isProviderConnected("apple"),
                            statusLabel: lang.s("connected_status"),
                            connectLabel: lang.s("connected_connect"),
                            onToggle: {}
                        )
                    }

                    Spacer().frame(height: 32)

                    // --- Logout Button ---
                    Button(action: onLogout) {
                        Text(lang.s("profile_logout"))
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundColor(colors.onDestructive)
                            .frame(maxWidth: .infinity)
                            .frame(height: 48)
                            .background(colors.destructive)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                    }

                    Spacer().frame(height: 40)
                }
            }
            .padding(.horizontal, 20)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .photosPicker(
            isPresented: $showPhotoPicker,
            selection: $selectedPhotoItem,
            matching: .images
        )
        .onChange(of: selectedPhotoItem) { newItem in
            guard let item = newItem else { return }
            Task {
                if let data = try? await item.loadTransferable(type: Data.self) {
                    photoData = data
                }
            }
        }
        .task {
            await loadProfile()
        }
    }

    private func loadProfile() async {
        do {
            let data = try await ApiClient.shared.getMe()
            user = data
        } catch {
            #if DEBUG
            print("Failed to load profile: \(error)")
            #endif
        }
        isLoading = false
    }
}

#Preview {
    ProfileScreen()
        .environmentObject(LanguageManager.shared)
        .withAppTheme()
}
