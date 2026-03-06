import SwiftUI

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
    @State private var infoPanelOffset: CGFloat = 0
    @State private var isInfoPanelOpen = false
    @State private var dragDirection: Int = 0
    @State private var screenWidth: CGFloat = 390
    @State private var safeAreaTop: CGFloat = 0
    @State private var safeAreaBottom: CGFloat = 0
    @State private var panelDragStart: CGFloat = 0

    @Environment(\.appColors) private var colors

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

    private func openProgress(_ geo: GeometryProxy) -> CGFloat {
        let panelH = geo.size.height * 0.9
        return panelH > 0 ? min(max(infoPanelOffset / panelH, 0), 1) : 0
    }

    var body: some View {
        GeometryReader { geo in
            let op = openProgress(geo)

            ZStack(alignment: .bottom) {
                // Background panel — fades on dismiss drag
                colors.background.ignoresSafeArea()
                    .opacity(verticalDragOffset > 0 ? Double(max(1 - (verticalDragOffset / geo.size.height) * 2.5, 0)) : 1)

                // Stories container (only this gets scaled)
                ZStack {
                    if canGoPrevStory {
                        let bp = abs(swipeProgress)
                        storyPage(storyIndex: currentStoryIndex - 1, isActive: false)
                            .offset(x: -screenWidth + dragOffset)
                            .opacity(dragOffset > 0 ? 0.4 + Double(bp) * 0.6 : 0)
                    }

                    if canGoNextStory {
                        let bp = abs(swipeProgress)
                        storyPage(storyIndex: currentStoryIndex + 1, isActive: false)
                            .offset(x: screenWidth + dragOffset)
                            .opacity(dragOffset < 0 ? 0.4 + Double(bp) * 0.6 : 0)
                    }

                    storyPage(storyIndex: currentStoryIndex, isActive: true)
                        .offset(x: dragOffset)
                        .rotationEffect(.degrees(dragOffset != 0 ? Double(swipeProgress * -3) : 0))
                }
                .offset(y: verticalDragOffset - infoPanelOffset + op * 10)
                .scaleEffect(1 - op * 0.02)
                .opacity(verticalDragOffset > 0
                    ? Double(max(1 - verticalDragOffset / geo.size.height, 0))
                    : 1)

                // Info panel
                if infoPanelOffset > 0 {
                    let panelH = geo.size.height * 0.9
                    let story = stories[currentStoryIndex]
                    let content = contentsForStory(story)[safe: currentContentIndex]
                    StoryInfoPanel(
                        storyLabel: story.label,
                        content: content,
                        onCommentTap: {}
                    )
                    .frame(height: panelH + geo.safeAreaInsets.bottom)
                    .frame(maxWidth: .infinity)
                    .ignoresSafeArea(.all, edges: .bottom)
                    .offset(y: panelH - infoPanelOffset)
                }
            }
            .onAppear {
                screenWidth = geo.size.width
                safeAreaTop = geo.safeAreaInsets.top
                safeAreaBottom = geo.safeAreaInsets.bottom
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

                        if dragDirection == 0 {
                            if abs(tx) > abs(ty) && abs(tx) > 10 {
                                dragDirection = 1
                            } else if abs(ty) > abs(tx) && abs(ty) > 10 {
                                dragDirection = 2
                                if isInfoPanelOpen {
                                    panelDragStart = infoPanelOffset
                                }
                            } else {
                                return
                            }
                        }

                        if dragDirection == 1 {
                            if isInfoPanelOpen { return }
                            if tx > 0 && !canGoPrevStory { return }
                            if tx < 0 && !canGoNextStory { return }
                            dragOffset = tx
                            timer?.invalidate()
                        } else if dragDirection == 2 {
                            let screenH = geo.size.height
                            let panelH = screenH * 0.9

                            if ty < 0 && panelDragStart == 0 && !isInfoPanelOpen && verticalDragOffset == 0 {
                                panelDragStart = infoPanelOffset
                                isInfoPanelOpen = true
                                timer?.invalidate()
                            }

                            if isInfoPanelOpen && panelDragStart >= 0 && verticalDragOffset == 0 {
                                let newOffset = panelDragStart + (-ty)
                                infoPanelOffset = min(max(newOffset, 0), panelH)
                            } else if ty > 0 && !isInfoPanelOpen {
                                verticalDragOffset = ty * 0.9
                                timer?.invalidate()
                            }
                        }
                    }
                    .onEnded { value in
                        let dir = dragDirection
                        let savedPanelStart = panelDragStart
                        dragDirection = 0
                        panelDragStart = 0
                        let screenH = geo.size.height

                        let swipeOutCurve = Animation.timingCurve(0.32, 0.72, 0, 1, duration: 0.24)
                        let snapBackCurve = Animation.interpolatingSpring(stiffness: 400, damping: 28)
                        let panelSpring = Animation.easeOut(duration: 0.25)

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
                            let panelH = screenH * 0.9

                            if isInfoPanelOpen && infoPanelOffset > 0 {
                                let draggedAmount = abs(infoPanelOffset - savedPanelStart)
                                let shouldClose = infoPanelOffset < savedPanelStart
                                if shouldClose && draggedAmount > panelH * 0.10 {
                                    withAnimation(panelSpring) {
                                        infoPanelOffset = 0
                                    }
                                    isInfoPanelOpen = false
                                    startProgress()
                                } else {
                                    withAnimation(panelSpring) {
                                        infoPanelOffset = panelH
                                    }
                                }
                            } else if verticalDragOffset > 0 {
                                let normalizedY = verticalDragOffset / screenH
                                if normalizedY > 0.20 {
                                    withAnimation(.timingCurve(0.32, 0.72, 0, 1, duration: 0.28)) {
                                        verticalDragOffset = screenH + safeAreaTop + safeAreaBottom
                                    }
                                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.28) {
                                        onDismiss()
                                    }
                                } else {
                                    withAnimation(snapBackCurve) {
                                        verticalDragOffset = 0
                                    }
                                    isInfoPanelOpen = false
                                    startProgress()
                                }
                            }
                        }
                    }
            )
        }
    }

    // MARK: - Story Page

    @ViewBuilder
    private func storyPage(storyIndex: Int, isActive: Bool) -> some View {
        let story = stories[storyIndex]
        let contents = contentsForStory(story)
        let contentIdx = isActive ? currentContentIndex : 0
        let current = contents.indices.contains(contentIdx) ? contents[contentIdx] : nil

        ZStack {
            Color(red: 0.1, green: 0.1, blue: 0.18)
                .padding(.top, -safeAreaTop)
                .padding(.bottom, -safeAreaBottom)
 
            if isActive {
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

    // MARK: - Helpers

    private func barWidth(for index: Int, totalWidth: CGFloat) -> CGFloat {
        if index < currentContentIndex {
            return totalWidth
        } else if index == currentContentIndex {
            return totalWidth * progress
        } else {
            return 0
        }
    }

    private func animateStorySwitch(direction: Int) {
        timer?.invalidate()
        let target = CGFloat(direction) * screenWidth
        withAnimation(.timingCurve(0.32, 0.72, 0, 1, duration: 0.24)) {
            dragOffset = target
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.25) {
            var t = Transaction()
            t.disablesAnimations = true
            withTransaction(t) {
                dragOffset = 0
                progress = 0
                currentStoryIndex -= direction
                currentContentIndex = 0
            }
            startProgress()
        }
    }

    private func tapNext() {
        let contents = currentContents
        if currentContentIndex < contents.count - 1 {
            currentContentIndex += 1
            startProgress()
        } else if canGoNextStory {
            animateStorySwitch(direction: -1)
        } else {
            onDismiss()
        }
    }

    private func tapPrev() {
        if currentContentIndex > 0 {
            currentContentIndex -= 1
            startProgress()
        } else if canGoPrevStory {
            animateStorySwitch(direction: 1)
        }
    }

    private func startProgress() {
        timer?.invalidate()
        progress = 0

        guard !isInfoPanelOpen else { return }
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
                    animateStorySwitch(direction: -1)
                } else {
                    onDismiss()
                }
            }
        }
    }
}

// MARK: - Story Info Panel

struct StoryInfoPanel: View {
    let storyLabel: String
    let content: StoryContent?
    var onCommentTap: () -> Void = {}

    @Environment(\.appColors) private var colors

    var body: some View {
        let shape = UnevenRoundedRectangle(topLeadingRadius: 20, bottomLeadingRadius: 0, bottomTrailingRadius: 0, topTrailingRadius: 20)
        VStack(spacing: 0) {
            // Drag handle
            RoundedRectangle(cornerRadius: 3)
                .fill(colors.textSecondary.opacity(0.3))
                .frame(width: 40, height: 5)
                .padding(.top, 10)

            // Title & subtitle
            VStack(alignment: .leading, spacing: 4) {
                Text("Detalhes do acontecimento")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(colors.textPrimary)
                Text(storyLabel)
                    .font(.system(size: 14))
                    .foregroundColor(colors.textSecondary)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 20)
            .padding(.top, 16)

            // Divider
            Rectangle()
                .fill(colors.divider)
                .frame(height: 0.5)
                .padding(.horizontal, 20)
                .padding(.top, 16)

            // Post content
            if let content = content {
                VStack(alignment: .leading, spacing: 12) {
                    Text(content.text)
                        .font(.system(size: 16))
                        .foregroundColor(colors.textPrimary)
                        .lineSpacing(4)

                    HStack(spacing: 16) {
                        Label(content.timeAgo, systemImage: "clock")
                        Label(content.mediaType == .photo ? "Foto" : "Vídeo", systemImage: content.mediaType == .photo ? "camera" : "video")
                    }
                    .font(.system(size: 13))
                    .foregroundColor(colors.textSecondary)

                    // Signals
                    HStack(spacing: 8) {
                        ForEach(mockSignals, id: \.0) { emoji, count in
                            HStack(spacing: 4) {
                                Text(emoji)
                                    .font(.system(size: 14))
                                Text("\(count)")
                                    .font(.system(size: 13, weight: .medium))
                                    .foregroundColor(colors.textSecondary)
                            }
                            .padding(.horizontal, 10)
                            .padding(.vertical, 6)
                            .background(colors.avatarBackground)
                            .clipShape(Capsule())
                        }
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, 20)
                .padding(.top, 16)
            }

            // Divider
            Rectangle()
                .fill(colors.divider)
                .frame(height: 0.5)
                .padding(.horizontal, 20)
                .padding(.top, 16)

            // Comments header
            HStack {
                Text("Comentários")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(colors.textPrimary)
                Spacer()
                Text("\(mockPanelComments.count)")
                    .font(.system(size: 14))
                    .foregroundColor(colors.textSecondary)
            }
            .padding(.horizontal, 20)
            .padding(.top, 16)

            // Comments list
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    ForEach(mockPanelComments, id: \.name) { comment in
                        HStack(alignment: .top, spacing: 12) {
                            Circle()
                                .fill(colors.avatarBackground)
                                .frame(width: 34, height: 34)
                                .overlay(
                                    Text(String(comment.name.prefix(1)))
                                        .font(.system(size: 14, weight: .semibold))
                                        .foregroundColor(colors.textSecondary)
                                )
                            VStack(alignment: .leading, spacing: 4) {
                                HStack(spacing: 6) {
                                    Text(comment.name)
                                        .font(.system(size: 14, weight: .semibold))
                                        .foregroundColor(colors.textPrimary)
                                    Text(comment.timeAgo)
                                        .font(.system(size: 12))
                                        .foregroundColor(colors.textSecondary)
                                }
                                Text(comment.text)
                                    .font(.system(size: 14))
                                    .foregroundColor(colors.textPrimary)
                                    .lineSpacing(3)
                            }
                        }
                    }
                }
                .padding(.horizontal, 20)
                .padding(.top, 12)
            }

            // Comment button
            Button(action: onCommentTap) {
                HStack(spacing: 8) {
                    Image(systemName: "bubble.left")
                        .font(.system(size: 15))
                    Text("Comentar")
                        .font(.system(size: 15, weight: .semibold))
                }
                .foregroundColor(colors.onAccent)
                .frame(maxWidth: .infinity)
                .frame(height: 46)
                .background(colors.accent)
                .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            .padding(.horizontal, 20)
            .padding(.top, 12)
            .padding(.bottom, 16)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(shape.fill(colors.surface))
        .overlay(shape.stroke(colors.divider, lineWidth: 1))
        .clipShape(shape)
    }
}

private struct PanelComment {
    let name: String
    let text: String
    let timeAgo: String
}

private let mockPanelComments: [PanelComment] = [
    PanelComment(name: "Maria S.", text: "Passei por lá agora e realmente está complicado! Tem que ter muito cuidado.", timeAgo: "1h"),
    PanelComment(name: "João P.", text: "Obrigado pelo aviso, vou desviar por outra rua.", timeAgo: "2h"),
    PanelComment(name: "Ana L.", text: "Já tem uns 3 dias assim, ninguém faz nada pra resolver esse problema.", timeAgo: "3h"),
    PanelComment(name: "Carlos R.", text: "Liguei pra prefeitura e disseram que vão enviar uma equipe.", timeAgo: "5h"),
]

private let mockSignals: [(String, Int)] = [
    ("⚠️", 12),
    ("👍", 8),
    ("📍", 5),
]

extension Array {
    subscript(safe index: Int) -> Element? {
        indices.contains(index) ? self[index] : nil
    }
}
