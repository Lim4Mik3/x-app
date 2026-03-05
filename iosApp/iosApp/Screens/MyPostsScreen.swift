import SwiftUI

struct MyPostsScreen: View {
    @Environment(\.appColors) private var colors
    @EnvironmentObject var lang: LanguageManager

    var body: some View {
        VStack(spacing: 4) {
            Text(lang.s("my_posts_title"))
                .font(.system(size: 20, weight: .semibold))
                .foregroundColor(colors.textPrimary)
            Text(lang.s("my_posts_empty"))
                .font(.system(size: 14))
                .foregroundColor(colors.textSecondary)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

#Preview {
    MyPostsScreen()
}
