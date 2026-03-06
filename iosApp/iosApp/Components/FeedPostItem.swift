import SwiftUI

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

                // Interaction buttons
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

            Rectangle()
                .fill(colors.divider.opacity(0.4))
                .frame(height: 6)
        }
        .background(colors.surface)
    }
}

// MARK: - Helpers

func parseHexColor(_ hex: String?) -> Color {
    guard var h = hex?.trimmingCharacters(in: .whitespaces), !h.isEmpty else { return .gray }
    if h.hasPrefix("#") { h = String(h.dropFirst()) }
    guard h.count == 6, let rgb = UInt64(h, radix: 16) else { return .gray }
    return Color(
        red: Double((rgb >> 16) & 0xFF) / 255,
        green: Double((rgb >> 8) & 0xFF) / 255,
        blue: Double(rgb & 0xFF) / 255
    )
}

struct InteractionLabel: View {
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
