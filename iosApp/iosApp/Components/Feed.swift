import SwiftUI

struct Feed: View {
    let posts: [ApiFeedPost]
    var isLoading: Bool = false
    var onScrollOffsetChanged: ((CGFloat) -> Void)? = nil
    var topInset: CGFloat = 0
    var bottomInset: CGFloat = 0
    var onSignalClick: (ApiFeedPost) -> Void = { _ in }
    var onCommentClick: (ApiFeedPost) -> Void = { _ in }
    var onShareClick: (ApiFeedPost) -> Void = { _ in }
    var onReportClick: (ApiFeedPost) -> Void = { _ in }
    var onStoryTap: (StoryTapInfo) -> Void = { _ in }
    var locationName: String = ""
    var onLoadMore: () -> Void = {}
    var onRefresh: (() async -> Void)? = nil

    @Environment(\.appColors) private var colors

    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 0) {
                Color.clear.frame(height: topInset)

                if isLoading && posts.isEmpty {
                    VStack {
                        Spacer().frame(height: 200)
                        ProgressView().tint(colors.accent)
                    }
                    .frame(maxWidth: .infinity)
                } else if !isLoading && posts.isEmpty {
                    VStack {
                        Spacer().frame(height: 200)
                        Text("Nenhuma publicação por perto")
                            .font(.system(size: 14))
                            .foregroundColor(colors.textSecondary)
                    }
                    .frame(maxWidth: .infinity)
                } else {
                    LazyVStack(spacing: 0) {
                        VStack(spacing: 0) {
                            StoriesRow(stories: mockStories, locationName: locationName, onStoryTap: onStoryTap)
                                .padding(.vertical, 12)
                        }
                        .background(
                            LinearGradient(
                                colors: [colors.accent.opacity(0.06), colors.surface],
                                startPoint: .top,
                                endPoint: .bottom
                            )
                        )
                        Rectangle()
                            .fill(colors.divider.opacity(0.4))
                            .frame(height: 6)

                        ForEach(posts) { post in
                            FeedPostItem(
                                post: post,
                                onSignalClick: { onSignalClick(post) },
                                onCommentClick: { onCommentClick(post) },
                                onShareClick: { onShareClick(post) },
                                onReportClick: { onReportClick(post) }
                            )
                        }

                        Color.clear.frame(height: 1)
                            .onAppear { onLoadMore() }

                        if isLoading {
                            ProgressView()
                                .tint(colors.accent)
                                .padding(16)
                        }
                    }
                }

                Color.clear.frame(height: bottomInset)
            }
            .background(
                ScrollViewObserver { offset in
                    onScrollOffsetChanged?(offset)
                }
            )
        }
        .refreshable {
            await onRefresh?()
        }
    }
}
