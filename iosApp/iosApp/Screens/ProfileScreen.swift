import SwiftUI
import PhotosUI

struct ProfileScreen: View {
    @Environment(\.appColors) private var colors
    @EnvironmentObject var lang: LanguageManager

    var onLogout: () -> Void = {}

    @State private var notificationsEnabled = true
    @State private var googleConnected = true
    @State private var appleConnected = false
    @State private var facebookConnected = false
    @State private var photoData: Data?
    @State private var showPhotoPicker = false
    @State private var selectedPhotoItem: PhotosPickerItem?

    private let languages: [(tag: String, name: String)] = [
        ("pt-BR", "Português"),
        ("en", "English"),
        ("es", "Español")
    ]

    private let user = UserProfile(
        name: "Leonardo Oliveira",
        email: "leo.oliveira@email.com",
        phone: "+55 11 99876-5432",
        address: "Rua das Flores, 123",
        location: "Osasco, SP",
        initials: "LO",
        preferences: []
    )

    private var selectedLanguageName: String {
        languages.first { $0.tag == lang.currentLanguage }?.name ?? languages[0].name
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                Spacer().frame(height: 60)

                // --- Profile Header ---
                ProfileHeader(
                    name: user.name,
                    email: user.email,
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
                        value: user.email
                    )
                    ProfileDivider()
                    ProfileInfoRow(
                        icon: "phone",
                        label: lang.s("profile_phone"),
                        value: user.phone
                    )
                }

                Spacer().frame(height: 24)

                // --- Location Section ---
                ProfileSection(title: lang.s("profile_section_location")) {
                    ProfileInfoRow(
                        icon: "house",
                        label: lang.s("profile_address"),
                        value: user.address
                    )
                    ProfileDivider()
                    ProfileInfoRow(
                        icon: "location",
                        label: lang.s("profile_location"),
                        value: user.location
                    )
                }

                Spacer().frame(height: 24)

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
                        connected: googleConnected,
                        statusLabel: lang.s("connected_status"),
                        connectLabel: lang.s("connected_connect"),
                        onToggle: { googleConnected.toggle() }
                    )
                    ProfileDivider()
                    ConnectedAccountRow(
                        icon: "apple.logo",
                        name: lang.s("connected_apple"),
                        connected: appleConnected,
                        statusLabel: lang.s("connected_status"),
                        connectLabel: lang.s("connected_connect"),
                        onToggle: { appleConnected.toggle() }
                    )
                    ProfileDivider()
                    ConnectedAccountRow(
                        icon: "person.2.fill",
                        name: lang.s("connected_facebook"),
                        connected: facebookConnected,
                        statusLabel: lang.s("connected_status"),
                        connectLabel: lang.s("connected_connect"),
                        onToggle: { facebookConnected.toggle() }
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
    }
}

#Preview {
    ProfileScreen()
        .environmentObject(LanguageManager.shared)
        .withAppTheme()
}
