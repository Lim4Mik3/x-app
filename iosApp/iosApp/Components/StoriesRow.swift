import SwiftUI

struct StoryItem: Identifiable, Hashable {
    let id: String
    let label: String
    let color: Color
    var hasUnread: Bool = true

    static func == (lhs: StoryItem, rhs: StoryItem) -> Bool { lhs.id == rhs.id }
    func hash(into hasher: inout Hasher) { hasher.combine(id) }
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

struct StoriesRow: View {
    let stories: [StoryItem]
    var locationName: String = ""
    var onStoryClick: (StoryItem) -> Void = { _ in }

    @Environment(\.appColors) private var colors

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            if !locationName.isEmpty {
                Text(String(format: NSLocalizedString("stories_in_city", comment: ""), locationName))
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(colors.textSecondary)
                    .padding(.horizontal, 16)
            }

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
                                            .frame(width: 60, height: 60)
                                    } else {
                                        Circle()
                                            .stroke(colors.divider, lineWidth: 1)
                                            .frame(width: 60, height: 60)
                                    }
                                    Circle()
                                        .fill(story.color.opacity(0.25))
                                        .frame(width: 52, height: 52)
                                }
                                Text(story.label)
                                    .font(.system(size: 11, weight: .medium))
                                    .foregroundColor(colors.textPrimary)
                                    .lineLimit(1)
                                    .minimumScaleFactor(0.8)
                            }
                            .frame(width: 68)
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 2)
            }
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
