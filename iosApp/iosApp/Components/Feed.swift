import SwiftUI

// MARK: - Feed

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
    var onStoryClick: (StoryItem) -> Void = { _ in }
    var onLoadMore: () -> Void = {}
    var onRefresh: (() async -> Void)? = nil

    @Environment(\.appColors) private var colors

    var body: some View {
        ScrollView {
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
                    // Stories section
                    VStack(spacing: 0) {
                        StoriesRow(stories: mockStories, onStoryClick: onStoryClick)
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

                    // Load more trigger
                    Color.clear.frame(height: 1)
                        .onAppear { onLoadMore() }

                    if isLoading {
                        ProgressView()
                            .tint(colors.accent)
                            .padding(16)
                    }
                }
            }
        }
        .padding(.top, topInset)
        .padding(.bottom, bottomInset)
        .background(
            ScrollViewObserver { offset in
                onScrollOffsetChanged?(offset)
            }
        )
        .refreshable {
            await onRefresh?()
        }
    }

}

// MARK: - Feed Post Item

struct FeedPostItem: View {
    let post: ApiFeedPost
    var onSignalClick: () -> Void = {}
    var onCommentClick: () -> Void = {}
    var onShareClick: () -> Void = {}
    var onReportClick: () -> Void = {}

    @Environment(\.appColors) private var colors
    @State private var showMenu = false

    var body: some View {
        VStack(spacing: 0) {
            VStack(alignment: .leading, spacing: 0) {
                // Header: Type · Distance · Time  ···
                HStack(spacing: 6) {
                    let tColor = parseHexColor(post.typeColor)
                    Text(post.type)
                        .font(.system(size: 11, weight: .semibold))
                        .foregroundColor(tColor)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(tColor.opacity(0.12))
                        .clipShape(Capsule())

                    if let dist = post.distance {
                        Text("·")
                            .font(.system(size: 12))
                            .foregroundColor(colors.textSecondary)
                        Text(dist)
                            .font(.system(size: 12))
                            .foregroundColor(colors.textSecondary)
                            .lineLimit(1)
                    }

                    Spacer()

                    Text(post.timeAgo)
                        .font(.system(size: 11))
                        .foregroundColor(colors.textSecondary)

                    Menu {
                        Button(role: .destructive) {
                            onReportClick()
                        } label: {
                            Label("Denunciar", systemImage: "flag")
                        }
                    } label: {
                        Image(systemName: "ellipsis")
                            .font(.system(size: 14))
                            .foregroundColor(colors.textSecondary)
                            .frame(width: 28, height: 28)
                    }
                }

                // Content
                Text(post.content)
                    .font(.system(size: 15))
                    .foregroundColor(colors.textPrimary)
                    .lineSpacing(3)
                    .fixedSize(horizontal: false, vertical: true)
                    .padding(.top, 10)

                // Categories as hashtags
                if !post.categories.isEmpty {
                    Text(post.categories.map { toHashtag($0) }.joined(separator: "  "))
                        .font(.system(size: 11))
                        .foregroundColor(colors.textSecondary)
                        .lineLimit(1)
                        .padding(.top, 16)
                }

                // Divider
                Rectangle()
                    .fill(colors.divider)
                    .frame(height: 0.5)
                    .padding(.top, 10)

                // Interaction buttons (equal width, full tap area)
                HStack(spacing: 0) {
                    Button(action: onSignalClick) {
                        InteractionLabel(icon: "antenna.radiowaves.left.and.right", label: post.signalsCount > 0 ? "\(post.signalsCount)" : "Sinal")
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 8)
                            .contentShape(Rectangle())
                    }
                    .buttonStyle(.plain)
                    Rectangle()
                        .fill(colors.divider)
                        .frame(width: 0.5, height: 14)
                    Button(action: onCommentClick) {
                        InteractionLabel(icon: "bubble.left", label: post.commentsCount > 0 ? "\(post.commentsCount)" : "Comentar")
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 8)
                            .contentShape(Rectangle())
                    }
                    .buttonStyle(.plain)
                    Rectangle()
                        .fill(colors.divider)
                        .frame(width: 0.5, height: 14)
                    Button(action: onShareClick) {
                        InteractionLabel(icon: "square.and.arrow.up", label: "Compartilhar")
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 8)
                            .contentShape(Rectangle())
                    }
                    .buttonStyle(.plain)
                }
                .padding(.top, 10)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)

            // Divider
            Rectangle()
                .fill(colors.divider.opacity(0.4))
                .frame(height: 6)
        }
        .background(colors.surface)
    }
}

// MARK: - Sub-components

private func parseHexColor(_ hex: String?) -> Color {
    guard var h = hex?.trimmingCharacters(in: .whitespaces), !h.isEmpty else { return .gray }
    if h.hasPrefix("#") { h = String(h.dropFirst()) }
    guard h.count == 6, let rgb = UInt64(h, radix: 16) else { return .gray }
    return Color(
        red: Double((rgb >> 16) & 0xFF) / 255,
        green: Double((rgb >> 8) & 0xFF) / 255,
        blue: Double(rgb & 0xFF) / 255
    )
}

// MARK: - Stories

struct StoryItem: Identifiable, Hashable {
    let id: String
    let label: String
    let color: Color
    var hasUnread: Bool = true

    static func == (lhs: StoryItem, rhs: StoryItem) -> Bool { lhs.id == rhs.id }
    func hash(into hasher: inout Hasher) { hasher.combine(id) }
}

struct StoriesRow: View {
    let stories: [StoryItem]
    var onStoryClick: (StoryItem) -> Void = { _ in }

    @Environment(\.appColors) private var colors

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(stories) { story in
                    Button {
                        onStoryClick(story)
                    } label: {
                        VStack(spacing: 5) {
                            ZStack {
                                if story.hasUnread {
                                    SpinningBorder(color: colors.accent)
                                        .frame(width: 55, height: 55)
                                } else {
                                    Circle()
                                        .stroke(colors.divider, lineWidth: 1)
                                        .frame(width: 55, height: 55)
                                }
                                Circle()
                                    .fill(story.color.opacity(0.25))
                                    .frame(width: 47, height: 47)
                            }
                            Text(story.label)
                                .font(.system(size: 12, weight: .medium))
                                .foregroundColor(colors.textPrimary)
                                .lineLimit(1)
                        }
                        .frame(width: 62)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 2)
        }
    }
}

private struct SpinningBorder: View {
    let color: Color
    @State private var rotation: Double = 0

    var body: some View {
        Circle()
            .stroke(
                AngularGradient(
                    gradient: Gradient(colors: [
                        color,
                        color.opacity(0.7),
                        color.opacity(0.7),
                        color
                    ]),
                    center: .center
                ),
                lineWidth: 3
            )
            .rotationEffect(.degrees(rotation))
            .onAppear {
                withAnimation(.linear(duration: 2).repeatForever(autoreverses: false)) {
                    rotation = 360
                }
            }
    }
}

// MARK: - Story Viewer

enum StoryMediaType {
    case photo
    case video
}

struct StoryContent: Identifiable {
    let id: String
    let text: String
    let timeAgo: String
    var mediaType: StoryMediaType = .photo
    var durationMs: Double? = nil

    var displayDurationMs: Double {
        switch mediaType {
        case .photo: return 8000
        case .video: return durationMs ?? 8000
        }
    }
}

struct StoryViewer: View {
    let stories: [StoryItem]
    let initialStoryIndex: Int
    let contentsForStory: (StoryItem) -> [StoryContent]
    let onDismiss: () -> Void

    @State private var currentStoryIndex: Int
    @State private var currentContentIndex = 0
    @State private var progress: CGFloat = 0
    @State private var timer: Timer?
    @State private var dragOffset: CGFloat = 0
    @State private var verticalDragOffset: CGFloat = 0
    @State private var dragDirection: Int = 0 // 0=undecided, 1=horizontal, 2=vertical
    @State private var screenWidth: CGFloat = 390

    private let tickInterval: Double = 0.05
    private let swipeThreshold: CGFloat = 0.3

    init(stories: [StoryItem], initialStoryIndex: Int, contentsForStory: @escaping (StoryItem) -> [StoryContent], onDismiss: @escaping () -> Void) {
        self.stories = stories
        self.initialStoryIndex = initialStoryIndex
        self.contentsForStory = contentsForStory
        self.onDismiss = onDismiss
        _currentStoryIndex = State(initialValue: initialStoryIndex)
    }

    private var currentStory: StoryItem { stories[currentStoryIndex] }
    private var currentContents: [StoryContent] { contentsForStory(currentStory) }
    private var canGoNextStory: Bool { currentStoryIndex < stories.count - 1 }
    private var canGoPrevStory: Bool { currentStoryIndex > 0 }
    private var swipeProgress: CGFloat { screenWidth > 0 ? dragOffset / screenWidth : 0 }

    var body: some View {
        GeometryReader { geo in
            ZStack {
                Color.black.ignoresSafeArea()

                // Previous story (behind, when swiping right)
                if canGoPrevStory && dragOffset > 0 {
                    let bp = abs(swipeProgress)
                    storyPage(storyIndex: currentStoryIndex - 1, isActive: false)
                        .opacity(0.4 + Double(bp) * 0.6)
                }

                // Next story (behind, when swiping left)
                if canGoNextStory && dragOffset < 0 {
                    let bp = abs(swipeProgress)
                    storyPage(storyIndex: currentStoryIndex + 1, isActive: false)
                        .opacity(0.4 + Double(bp) * 0.6)
                }

                // Current story (on top, slides + tilts away)
                storyPage(storyIndex: currentStoryIndex, isActive: true)
                    .offset(x: dragOffset)
                    .rotationEffect(.degrees(dragOffset != 0 ? Double(swipeProgress * -3) : 0))
            }
            .offset(y: verticalDragOffset)
            .opacity(verticalDragOffset > 0 ? Double(1 - min(verticalDragOffset / geo.size.height, 1) * 0.5) : 1)
            .onAppear {
                screenWidth = geo.size.width
                startProgress()
            }
            .onDisappear {
                timer?.invalidate()
            }
            .gesture(
                DragGesture()
                    .onChanged { value in
                        let tx = value.translation.width
                        let ty = value.translation.height

                        // Decide direction once per gesture
                        if dragDirection == 0 {
                            if abs(tx) > abs(ty) && abs(tx) > 10 {
                                dragDirection = 1 // horizontal
                            } else if abs(ty) > abs(tx) && abs(ty) > 10 {
                                dragDirection = 2 // vertical
                            } else {
                                return
                            }
                        }

                        if dragDirection == 1 {
                            if tx > 0 && !canGoPrevStory { return }
                            if tx < 0 && !canGoNextStory { return }
                            dragOffset = tx
                            timer?.invalidate()
                        } else if dragDirection == 2 && ty > 0 {
                            // Resistance: logarithmic curve
                            let screenH = geo.size.height
                            let ratio = min(ty / screenH, 2)
                            verticalDragOffset = screenH * (1 - (1 / (ratio * 0.85 + 1))) * 1.3
                            timer?.invalidate()
                        }
                    }
                    .onEnded { value in
                        let dir = dragDirection
                        dragDirection = 0
                        let screenH = geo.size.height

                        let swipeOutCurve = Animation.timingCurve(0.32, 0.72, 0, 1, duration: 0.24)
                        let snapBackCurve = Animation.interpolatingSpring(stiffness: 400, damping: 28)

                        if dir == 1 {
                            let normalized = dragOffset / screenWidth

                            if normalized < -swipeThreshold && canGoNextStory {
                                withAnimation(swipeOutCurve) {
                                    dragOffset = -screenWidth
                                }
                                DispatchQueue.main.asyncAfter(deadline: .now() + 0.25) {
                                    var t = Transaction()
                                    t.disablesAnimations = true
                                    withTransaction(t) {
                                        dragOffset = 0
                                        progress = 0
                                        currentStoryIndex += 1
                                        currentContentIndex = 0
                                    }
                                    startProgress()
                                }
                            } else if normalized > swipeThreshold && canGoPrevStory {
                                withAnimation(swipeOutCurve) {
                                    dragOffset = screenWidth
                                }
                                DispatchQueue.main.asyncAfter(deadline: .now() + 0.25) {
                                    var t = Transaction()
                                    t.disablesAnimations = true
                                    withTransaction(t) {
                                        dragOffset = 0
                                        progress = 0
                                        currentStoryIndex -= 1
                                        currentContentIndex = 0
                                    }
                                    startProgress()
                                }
                            } else {
                                withAnimation(snapBackCurve) {
                                    dragOffset = 0
                                }
                                startProgress()
                            }
                        } else if dir == 2 {
                            let normalizedY = verticalDragOffset / screenH

                            if normalizedY > 0.35 {
                                withAnimation(.timingCurve(0.32, 0.72, 0, 1, duration: 0.28)) {
                                    verticalDragOffset = screenH
                                }
                                DispatchQueue.main.asyncAfter(deadline: .now() + 0.28) {
                                    onDismiss()
                                }
                            } else {
                                withAnimation(snapBackCurve) {
                                    verticalDragOffset = 0
                                }
                                startProgress()
                            }
                        }
                    }
            )
        }
    }

    @ViewBuilder
    private func storyPage(storyIndex: Int, isActive: Bool) -> some View {
        let story = stories[storyIndex]
        let contents = contentsForStory(story)
        let contentIdx = isActive ? currentContentIndex : 0
        let current = contents.indices.contains(contentIdx) ? contents[contentIdx] : nil

        ZStack {
            Color(red: 0.1, green: 0.1, blue: 0.18)
                .ignoresSafeArea()

            if isActive {
                // Tap zones
                HStack(spacing: 0) {
                    Color.clear
                        .contentShape(Rectangle())
                        .onTapGesture { tapPrev() }
                    Color.clear
                        .contentShape(Rectangle())
                        .onTapGesture { tapNext() }
                }
            }

            VStack(spacing: 0) {
                // Progress bars
                HStack(spacing: 4) {
                    ForEach(Array(contents.enumerated()), id: \.offset) { index, _ in
                        GeometryReader { geo in
                            ZStack(alignment: .leading) {
                                RoundedRectangle(cornerRadius: 2)
                                    .fill(Color.white.opacity(0.3))
                                RoundedRectangle(cornerRadius: 2)
                                    .fill(Color.white)
                                    .frame(width: isActive ? barWidth(for: index, totalWidth: geo.size.width) : 0)
                            }
                        }
                        .frame(height: 3)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 12)

                // Header
                HStack(spacing: 10) {
                    Circle()
                        .fill(story.color.opacity(0.25))
                        .frame(width: 36, height: 36)

                    VStack(alignment: .leading, spacing: 1) {
                        Text("Acontecimentos de")
                            .font(.system(size: 12))
                            .foregroundColor(.white.opacity(0.7))
                        Text(story.label)
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.white)
                    }

                    Spacer()

                    Button(action: onDismiss) {
                        Image(systemName: "xmark")
                            .font(.system(size: 18, weight: .medium))
                            .foregroundColor(.white)
                            .frame(width: 32, height: 32)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 16)

                Spacer()

                // Content
                if let current = current {
                    VStack(alignment: .leading, spacing: 12) {
                        Text(current.text)
                            .font(.system(size: 18))
                            .foregroundColor(.white)
                            .lineSpacing(4)
                        Text(current.timeAgo)
                            .font(.system(size: 13))
                            .foregroundColor(.white.opacity(0.5))
                    }
                    .padding(.horizontal, 32)
                }

                Spacer()
            }
        }
    }

    private func barWidth(for index: Int, totalWidth: CGFloat) -> CGFloat {
        if index < currentContentIndex {
            return totalWidth
        } else if index == currentContentIndex {
            return totalWidth * progress
        } else {
            return 0
        }
    }

    private func tapNext() {
        let contents = currentContents
        if currentContentIndex < contents.count - 1 {
            currentContentIndex += 1
            startProgress()
        } else if canGoNextStory {
            currentStoryIndex += 1
            currentContentIndex = 0
            startProgress()
        } else {
            onDismiss()
        }
    }

    private func tapPrev() {
        if currentContentIndex > 0 {
            currentContentIndex -= 1
            startProgress()
        } else if canGoPrevStory {
            currentStoryIndex -= 1
            currentContentIndex = 0
            startProgress()
        }
    }

    private func startProgress() {
        timer?.invalidate()
        progress = 0

        let contents = currentContents
        guard contents.indices.contains(currentContentIndex) else { return }
        let duration = contents[currentContentIndex].displayDurationMs / 1000.0
        let increment = tickInterval / duration

        timer = Timer.scheduledTimer(withTimeInterval: tickInterval, repeats: true) { t in
            progress += CGFloat(increment)
            if progress >= 1.0 {
                t.invalidate()
                if currentContentIndex < contents.count - 1 {
                    currentContentIndex += 1
                    startProgress()
                } else if canGoNextStory {
                    currentStoryIndex += 1
                    currentContentIndex = 0
                    startProgress()
                } else {
                    onDismiss()
                }
            }
        }
    }
}

let mockStories: [StoryItem] = [
    StoryItem(id: "1", label: "Centro", color: .orange),
    StoryItem(id: "2", label: "Pinheiros", color: .blue),
    StoryItem(id: "3", label: "Vila Madalena", color: .red),
    StoryItem(id: "4", label: "Moema", color: .green),
    StoryItem(id: "5", label: "Itaim Bibi", color: .purple),
    StoryItem(id: "6", label: "Consolação", color: .pink),
    StoryItem(id: "7", label: "Liberdade", color: .cyan),
    StoryItem(id: "8", label: "Bela Vista", color: .mint),
]

private struct InteractionLabel: View {
    let icon: String
    let label: String

    @Environment(\.appColors) private var colors

    var body: some View {
        HStack(spacing: 4) {
            Image(systemName: icon)
                .font(.system(size: 14))
            Text(label)
                .font(.system(size: 12, weight: .medium))
        }
        .foregroundColor(colors.textSecondary)
    }
}
