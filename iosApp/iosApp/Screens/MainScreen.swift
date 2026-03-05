import SwiftUI

struct MainScreen: View {
    @State private var selectedLocation = "Osasco"
    @State private var selectedTab: AppTab = .home
    @State private var draftPostText = ""
    @State private var draftLocation = "Osasco"
    @State private var draftMediaUrl: URL? = nil
    @StateObject private var scrollState = ScrollAwareState()
    @StateObject private var navigator = OverlayNavigator()

    @Environment(\.appColors) private var colors
    @EnvironmentObject var lang: LanguageManager

    var body: some View {
        GeometryReader { geometry in
        ZStack(alignment: .top) {
            // Content layer — feed scrolls behind the bars
            Group {
                switch selectedTab {
                case .home:
                    Feed(
                        posts: mockPosts,
                        onScrollOffsetChanged: { offset in
                            scrollState.onScrollOffsetChanged(offset)
                        },
                        topInset: scrollState.headerHeight,
                        bottomInset: scrollState.bottomBarHeight
                    )
                case .myPosts:
                    MyPostsScreen()
                        .padding(.bottom, scrollState.bottomBarHeight)
                case .profile:
                    ProfileScreen()
                        .padding(.bottom, scrollState.bottomBarHeight)
                }
            }

            // Header overlay — slides up
            if selectedTab == .home {
                VStack(spacing: 0) {
                    Header(
                        locationName: selectedLocation,
                        timeAgo: lang.s("time_ago"),
                        statusLabel: lang.s("mood_calm"),
                        onLocationTap: {
                            withAnimation(.easeOut(duration: 0.15)) {
                                navigator.navigate(to: .locationPicker)
                            }
                        },
                        onMoodTap: {
                            withAnimation(.easeOut(duration: 0.15)) {
                                navigator.navigate(to: .moodDetail)
                            }
                        }
                    )
                    .background(colors.surface)
                    .background(
                        GeometryReader { proxy in
                            Color.clear.onAppear {
                                scrollState.headerHeight = proxy.size.height
                            }
                        }
                    )

                    // Persistent divider always visible
                    Rectangle()
                        .fill(colors.divider)
                        .frame(height: 0.5)

                    Spacer()
                }
                .offset(y: -scrollState.topBarOffset)
            }

            // Bottom bar overlay — slides down
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

            // FAB — create new post
            if selectedTab == .home {
                CreatePostFab {
                    withAnimation(.easeInOut(duration: 0.25)) {
                        navigator.navigate(to: .createPost)
                    }
                }
                .padding(.bottom, scrollState.bottomBarHeight + 16)
                .offset(y: scrollState.bottomBarOffset)
            }

            // Overlays — stack-based navigation
            // CreatePost stays rendered underneath when Map is on top
            if navigator.contains(.createPost) {
                CreatePostScreen(
                    text: draftPostText,
                    onTextChanged: { draftPostText = $0 },
                    onDismiss: {
                        draftPostText = ""
                        withAnimation(.easeInOut(duration: 0.25)) {
                            navigator.popToRoot()
                        }
                    },
                    onPublish: {
                        withAnimation(.easeInOut(duration: 0.25)) {
                            navigator.navigate(to: .map)
                        }
                    }
                )
                .transition(.asymmetric(
                    insertion: .move(edge: .trailing),
                    removal: .move(edge: .trailing)
                ))
                .zIndex(10)
            }

            if navigator.contains(.map) && !navigator.contains(.media) {
                MapScreen(
                    onDismiss: {
                        withAnimation(.easeInOut(duration: 0.25)) {
                            navigator.pop()
                        }
                    },
                    onLocationConfirmed: {
                        draftLocation = "Osasco"
                        withAnimation(.easeInOut(duration: 0.25)) {
                            navigator.navigate(to: .media)
                        }
                    }
                )
                .transition(.move(edge: .trailing))
                .zIndex(11)
            }

            if navigator.contains(.media) && !navigator.contains(.reviewPost) {
                CameraScreen(
                    onDismiss: {
                        withAnimation(.easeInOut(duration: 0.25)) {
                            navigator.pop()
                        }
                    },
                    onMediaCaptured: { url in
                        draftMediaUrl = url
                        withAnimation(.easeInOut(duration: 0.25)) {
                            navigator.navigate(to: .reviewPost)
                        }
                    },
                    onSkip: {
                        draftMediaUrl = nil
                        withAnimation(.easeInOut(duration: 0.25)) {
                            navigator.navigate(to: .reviewPost)
                        }
                    }
                )
                .transition(.move(edge: .trailing))
                .zIndex(12)
            }

            if navigator.contains(.reviewPost) {
                ReviewPostScreen(
                    postText: draftPostText,
                    location: draftLocation,
                    mediaUrl: draftMediaUrl,
                    onDismiss: {
                        draftPostText = ""
                        draftMediaUrl = nil
                        withAnimation(.easeInOut(duration: 0.25)) {
                            navigator.popToRoot()
                        }
                    },
                    onConfirm: {
                        draftPostText = ""
                        draftMediaUrl = nil
                        withAnimation(.easeInOut(duration: 0.25)) {
                            navigator.popToRoot()
                        }
                    }
                )
                .transition(.move(edge: .trailing))
                .zIndex(13)
            }

            if navigator.contains(.locationPicker) {
                LocationPickerScreen(
                    onDismiss: {
                        withAnimation(.linear(duration: 0.05)) {
                            navigator.pop()
                        }
                    },
                    onLocationSelected: { location in
                        selectedLocation = location
                        withAnimation(.linear(duration: 0.05)) {
                            navigator.pop()
                        }
                    }
                )
                .transition(.asymmetric(
                    insertion: .move(edge: .bottom),
                    removal: .opacity
                ))
                .zIndex(10)
            }

            if navigator.contains(.moodDetail) {
                MoodDetailScreen(
                    onDismiss: {
                        withAnimation(.linear(duration: 0.05)) {
                            navigator.pop()
                        }
                    }
                )
                .transition(.asymmetric(
                    insertion: .move(edge: .bottom),
                    removal: .opacity
                ))
                .zIndex(10)
            }
        }
        .onAppear {
            scrollState.safeAreaTop = geometry.safeAreaInsets.top
            scrollState.safeAreaBottom = geometry.safeAreaInsets.bottom
        }
        .background(colors.background)
        .onChange(of: selectedTab) { _ in
            scrollState.reset()
        }
        } // GeometryReader
    }
}

#Preview {
    MainScreen()
}
