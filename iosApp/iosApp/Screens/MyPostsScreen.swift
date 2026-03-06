import SwiftUI

struct MyPostsScreen: View {
    @Environment(\.appColors) private var colors
    @EnvironmentObject var lang: LanguageManager

    @State private var posts: [ApiFeedPost] = []
    @State private var isLoading = true

    var body: some View {
        VStack(spacing: 0) {
            // Header
            Text(lang.s("my_posts_title"))
                .font(.system(size: 20, weight: .semibold))
                .foregroundColor(colors.textPrimary)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, 20)
                .padding(.top, 60)
                .padding(.bottom, 16)

            if isLoading {
                Spacer()
                ProgressView()
                    .tint(colors.accent)
                Spacer()
            } else if posts.isEmpty {
                Spacer()
                VStack(spacing: 4) {
                    Image(systemName: "doc.text")
                        .font(.system(size: 32))
                        .foregroundColor(colors.textSecondary.opacity(0.5))
                    Text(lang.s("my_posts_empty"))
                        .font(.system(size: 14))
                        .foregroundColor(colors.textSecondary)
                }
                Spacer()
            } else {
                ScrollView {
                    LazyVStack(spacing: 0) {
                        ForEach(posts) { post in
                            FeedPostItem(post: post)
                        }
                    }
                }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .task {
            await loadPosts()
        }
    }

    private func loadPosts() async {
        do {
            posts = try await ApiClient.shared.getMyPosts()
        } catch {
            #if DEBUG
            print("Failed to load my posts: \(error)")
            #endif
        }
        isLoading = false
    }
}
