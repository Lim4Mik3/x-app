import SwiftUI
import CoreLocation

struct MainScreen: View {
    var onLogout: () -> Void = {}
    @State private var selectedTab: AppTab = .home
    @StateObject private var scrollState = ScrollAwareState()
    @StateObject private var navigator = OverlayNavigator()
    @StateObject private var locationService = LocationService.shared

    @Environment(\.appColors) private var colors

    // Feed state
    @State private var feedPosts: [ApiFeedPost] = []
    @State private var isFeedLoading = false
    @State private var feedCursor: String?
    @State private var hasMore = true
    @State private var locationName = "Carregando..."

    // Interaction sheets
    @State private var showCommentForPost: ApiFeedPost?
    @State private var showSignalForPost: ApiFeedPost?
    @State private var showReportForPost: ApiFeedPost?

    var body: some View {
        GeometryReader { geometry in
            ZStack(alignment: .top) {
                // Content
                Group {
                    switch selectedTab {
                    case .home:
                        Feed(
                            posts: feedPosts,
                            userLocation: locationService.location,
                            isLoading: isFeedLoading,
                            onScrollOffsetChanged: { scrollState.onScrollOffsetChanged($0) },
                            topInset: scrollState.headerHeight,
                            bottomInset: scrollState.bottomBarHeight,
                            onSignalClick: { showSignalForPost = $0 },
                            onCommentClick: { showCommentForPost = $0 },
                            onShareClick: { _ in },
                            onReportClick: { showReportForPost = $0 },
                            onLoadMore: {
                                if hasMore && !isFeedLoading && feedCursor != nil {
                                    loadFeed()
                                }
                            }
                        )
                    case .myPosts:
                        MyPostsScreen()
                            .padding(.bottom, scrollState.bottomBarHeight)
                    case .profile:
                        ProfileScreen(onLogout: {
                            Task {
                                try? await ApiClient.shared.logout()
                                TokenManager.shared.clear()
                                onLogout()
                            }
                        })
                            .padding(.bottom, scrollState.bottomBarHeight)
                    }
                }

                // Header
                if selectedTab == .home {
                    VStack(spacing: 0) {
                        Header(locationName: locationName)
                            .background(colors.surface)
                            .background(
                                GeometryReader { proxy in
                                    Color.clear.onAppear {
                                        scrollState.headerHeight = proxy.size.height
                                    }
                                }
                            )
                        Rectangle().fill(colors.divider).frame(height: 0.5)
                        Spacer()
                    }
                    .offset(y: -scrollState.topBarOffset)
                }

                // Bottom bar
                VStack {
                    Spacer()
                    BottomTabBar(selectedTab: $selectedTab)
                        .background(colors.surface)
                        .background(
                            GeometryReader { proxy in
                                Color.clear.onAppear {
                                    scrollState.bottomBarHeight = proxy.size.height
                                }
                            }
                        )
                }
                .offset(y: scrollState.bottomBarOffset)

                // FAB
                if selectedTab == .home {
                    CreatePostFab {
                        withAnimation(.easeInOut(duration: 0.25)) {
                            navigator.navigate(to: .createPost)
                        }
                    }
                    .padding(.bottom, scrollState.bottomBarHeight + 16)
                    .offset(y: scrollState.bottomBarOffset)
                }

                // CreatePost overlay
                if navigator.contains(.createPost) {
                    CreatePostScreen(
                        onDismiss: {
                            withAnimation(.easeInOut(duration: 0.25)) {
                                navigator.popToRoot()
                            }
                        },
                        onPostCreated: {
                            loadFeed(refresh: true)
                        }
                    )
                    .transition(.asymmetric(
                        insertion: .move(edge: .trailing),
                        removal: .move(edge: .trailing)
                    ))
                    .zIndex(10)
                }

                // Sheets
            }
            .onAppear {
                scrollState.safeAreaTop = geometry.safeAreaInsets.top
                scrollState.safeAreaBottom = geometry.safeAreaInsets.bottom
                locationService.setTtl(60) // 1 minute cache
                locationService.requestPermission()
            }
            .background(colors.background)
            .onChange(of: selectedTab) { _ in scrollState.reset() }
            .onChange(of: locationService.location) { loc in
                if loc != nil && feedPosts.isEmpty {
                    loadFeed(refresh: true)
                }
                reverseGeocode(loc)
            }
            .sheet(item: $showCommentForPost) { post in
                CommentSheet(postId: post.id, onDismiss: { showCommentForPost = nil })
                    .presentationDetents([.large])
            }
            .sheet(item: $showSignalForPost) { post in
                SignalSheet(postId: post.id, postType: post.type, onDismiss: { showSignalForPost = nil })
                    .presentationDetents([.medium])
            }
            .sheet(item: $showReportForPost) { post in
                ReportSheet(postId: post.id, onDismiss: { showReportForPost = nil })
                    .presentationDetents([.medium, .large])
            }
        }
    }

    // MARK: - Feed Loading

    private func loadFeed(refresh: Bool = false) {
        guard let loc = locationService.location else { return }
        guard !isFeedLoading else { return }

        isFeedLoading = true
        if refresh {
            feedCursor = nil
            hasMore = true
        }

        let cursor = refresh ? nil : feedCursor

        Task {
            do {
                let response = try await ApiClient.shared.getFeed(
                    lat: loc.coordinate.latitude,
                    lng: loc.coordinate.longitude,
                    limit: 20,
                    cursor: cursor
                )
                await MainActor.run {
                    if refresh {
                        feedPosts = response.posts
                    } else {
                        feedPosts += response.posts
                    }
                    feedCursor = response.nextCursor
                    hasMore = response.hasMore
                    isFeedLoading = false
                }
            } catch {
                await MainActor.run { isFeedLoading = false }
            }
        }
    }

    // MARK: - Reverse Geocode

    private func reverseGeocode(_ location: CLLocation?) {
        guard let loc = location else { return }
        CLGeocoder().reverseGeocodeLocation(loc) { placemarks, _ in
            if let p = placemarks?.first {
                locationName = p.subLocality ?? p.locality ?? p.subAdministrativeArea ?? "Ao seu redor"
            }
        }
    }
}

// Make ApiFeedPost work with .sheet(item:)
extension ApiFeedPost: Hashable {
    static func == (lhs: ApiFeedPost, rhs: ApiFeedPost) -> Bool { lhs.id == rhs.id }
    func hash(into hasher: inout Hasher) { hasher.combine(id) }
}
