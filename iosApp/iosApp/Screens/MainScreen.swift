import SwiftUI
import CoreLocation

struct MainScreen: View {
    var isLoggedIn: Bool = false
    var onLoginSuccess: () -> Void = {}
    var onLogout: () -> Void = {}
    @State private var selectedTab: AppTab = .home
    @State private var showLoginOverlay = false
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
    @State private var cityName = ""

    // Interaction sheets
    @State private var showCommentForPost: ApiFeedPost?
    @State private var showSignalForPost: ApiFeedPost?
    @State private var showReportForPost: ApiFeedPost?
    @State private var showStoryViewer: StoryItem?
    @State private var storyTransition: StoryTapInfo?
    @State private var storyTransitionExpanded = false

    private func requireAuth(_ action: @escaping () -> Void) {
        if isLoggedIn { action() } else { withAnimation(.easeOut(duration: 0.25)) { showLoginOverlay = true } }
    }

    var body: some View {
        GeometryReader { geometry in
            ZStack(alignment: .top) {
                // Content
                Group {
                    switch selectedTab {
                    case .home:
                        Feed(
                            posts: feedPosts,
                            isLoading: isFeedLoading,
                            onScrollOffsetChanged: { scrollState.onScrollOffsetChanged($0) },
                            topInset: scrollState.headerHeight,
                            bottomInset: isLoggedIn ? scrollState.bottomBarHeight : 0,
                            onSignalClick: { post in requireAuth { showSignalForPost = post } },
                            onCommentClick: { post in requireAuth { showCommentForPost = post } },
                            onShareClick: { _ in },
                            onReportClick: { post in requireAuth { showReportForPost = post } },
                            onStoryTap: { tapInfo in
                                storyTransition = tapInfo
                                storyTransitionExpanded = false

                                withAnimation(.timingCurve(0.4, 0, 0.2, 1, duration: 0.15)) {
                                    storyTransitionExpanded = true
                                }

                                DispatchQueue.main.asyncAfter(deadline: .now() + 0.13) {
                                    showStoryViewer = tapInfo.story
                                }

                                DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
                                    storyTransition = nil
                                    storyTransitionExpanded = false
                                }
                            },
                            locationName: cityName,
                            onLoadMore: {
                                if hasMore && !isFeedLoading && feedCursor != nil {
                                    loadFeed()
                                }
                            },
                            onRefresh: {
                                await refreshFeed()
                            }
                        )
                    case .myPosts:
                        if isLoggedIn {
                            MyPostsScreen()
                                .padding(.bottom, scrollState.bottomBarHeight)
                        } else {
                            LoginScreen(
                                onLoginSuccess: onLoginSuccess,
                                onDismiss: { selectedTab = .home }
                            )
                        }
                    case .profile:
                        if isLoggedIn {
                            ProfileScreen(onLogout: {
                                Task {
                                    try? await ApiClient.shared.logout()
                                    TokenManager.shared.clear()
                                    onLogout()
                                }
                            })
                                .padding(.bottom, scrollState.bottomBarHeight)
                        } else {
                            LoginScreen(
                                onLoginSuccess: onLoginSuccess,
                                onDismiss: { selectedTab = .home }
                            )
                        }
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

                // Top gradient (visible when header is hidden)
                if selectedTab == .home && scrollState.topBarOffset > 0 {
                    VStack {
                        LinearGradient(
                            colors: [colors.background, colors.background.opacity(0)],
                            startPoint: .top,
                            endPoint: .bottom
                        )
                        .frame(height: geometry.safeAreaInsets.top * 1.5)
                        .opacity(Double(min(scrollState.topBarOffset / scrollState.headerHeight, 1)))
                        .ignoresSafeArea(edges: .top)
                        Spacer()
                    }
                    .allowsHitTesting(false)
                }

                // Bottom gradient (visible when bottom bar is hidden or user is not logged in)
                if selectedTab == .home && (!isLoggedIn || scrollState.bottomBarOffset > 0) {
                    VStack(spacing: 0) {
                        Spacer()
                        LinearGradient(
                            colors: [colors.background.opacity(0), colors.background],
                            startPoint: .top,
                            endPoint: .bottom
                        )
                        .frame(height: geometry.safeAreaInsets.bottom * 1.5)
                        .opacity(!isLoggedIn ? 1 : Double(min(scrollState.bottomBarOffset / scrollState.bottomBarHeight, 1)))
                    }
                    .ignoresSafeArea(edges: .bottom)
                    .allowsHitTesting(false)
                }

                // Bottom bar
                if isLoggedIn {
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
                }

                // FAB
                if selectedTab == .home {
                    CreatePostFab {
                        requireAuth {
                            withAnimation(.easeInOut(duration: 0.25)) {
                                navigator.navigate(to: .createPost)
                            }
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

                // Login overlay (triggered by interactions when not logged in)
                if showLoginOverlay {
                    LoginScreen(
                        onLoginSuccess: {
                            onLoginSuccess()
                            withAnimation(.easeIn(duration: 0.2)) { showLoginOverlay = false }
                        },
                        onDismiss: { withAnimation(.easeIn(duration: 0.2)) { showLoginOverlay = false } }
                    )
                    .zIndex(20)
                    .transition(.opacity)
                }

                // Story expand transition
                if let tap = storyTransition {
                    let screen = UIScreen.main.bounds
                    let maxDx = max(tap.center.x, screen.width - tap.center.x)
                    let maxDy = max(tap.center.y, screen.height - tap.center.y)
                    let expandedSize = sqrt(maxDx * maxDx + maxDy * maxDy) * 2
                    let currentSize = storyTransitionExpanded ? expandedSize : tap.size

                    Circle()
                        .fill(Color(red: 0.1, green: 0.1, blue: 0.18))
                        .frame(width: currentSize, height: currentSize)
                        .position(tap.center)
                        .ignoresSafeArea()
                        .zIndex(14)
                        .allowsHitTesting(false)
                }

                // Story viewer
                if let story = showStoryViewer {
                    let initialIndex = mockStories.firstIndex(where: { $0.id == story.id }) ?? 0
                    StoryViewer(
                        stories: mockStories,
                        initialStoryIndex: initialIndex,
                        contentsForStory: { _ in mockStoryContents() },
                        onDismiss: { showStoryViewer = nil }
                    )
                    .zIndex(15)
                    .transition(.identity)
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

    private func refreshFeed() async {
        guard let loc = locationService.location else { return }
        do {
            let response = try await ApiClient.shared.getFeed(
                lat: loc.coordinate.latitude,
                lng: loc.coordinate.longitude,
                limit: 20,
                cursor: nil
            )
            await MainActor.run {
                feedPosts = response.posts
                feedCursor = response.nextCursor
                hasMore = response.hasMore
            }
        } catch {}
    }

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
                cityName = p.locality ?? p.subAdministrativeArea ?? ""
            }
        }
    }
}

private func mockStoryContents() -> [StoryContent] {
    [
        StoryContent(id: "1", text: "Buraco grande na rua principal, cuidado ao passar!", timeAgo: "2h"),
        StoryContent(id: "2", text: "Semáforo quebrado no cruzamento da Av. Brasil", timeAgo: "4h"),
        StoryContent(id: "3", text: "Festa de rua acontecendo neste fim de semana", timeAgo: "6h"),
    ]
}

// Make ApiFeedPost work with .sheet(item:)
extension ApiFeedPost: Hashable {
    static func == (lhs: ApiFeedPost, rhs: ApiFeedPost) -> Bool { lhs.id == rhs.id }
    func hash(into hasher: inout Hasher) { hasher.combine(id) }
}
