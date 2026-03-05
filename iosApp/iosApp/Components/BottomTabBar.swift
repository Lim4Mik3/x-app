import SwiftUI

enum AppTab: CaseIterable {
    case home, myPosts, profile

    var labelKey: String {
        switch self {
        case .home: return "tab_home"
        case .myPosts: return "tab_my_posts"
        case .profile: return "tab_profile"
        }
    }

    var icon: String {
        switch self {
        case .home: return "house.fill"
        case .myPosts: return "square.and.pencil"
        case .profile: return "person.fill"
        }
    }
}

struct BottomTabBar: View {
    @Binding var selectedTab: AppTab

    @Environment(\.appColors) private var colors
    @EnvironmentObject var lang: LanguageManager

    var body: some View {
        VStack(spacing: 0) {
            Rectangle()
                .fill(colors.divider)
                .frame(height: 0.5)

            HStack {
                ForEach(AppTab.allCases, id: \.self) { tab in
                    Button {
                        selectedTab = tab
                    } label: {
                        VStack(spacing: 4) {
                            Image(systemName: tab.icon)
                                .font(.system(size: 20))
                            Text(lang.s(tab.labelKey))
                                .font(.system(size: 11))
                        }
                        .frame(maxWidth: .infinity)
                        .foregroundColor(
                            selectedTab == tab
                                ? colors.tabActive
                                : colors.tabInactive
                        )
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.top, 8)
            .padding(.bottom, 4)
        }
        .background(colors.surface)
    }
}

#Preview {
    BottomTabBar(selectedTab: .constant(.home))
        .environmentObject(LanguageManager.shared)
}
