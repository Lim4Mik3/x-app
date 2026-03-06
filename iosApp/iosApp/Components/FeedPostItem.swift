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
                        .padding(.bottom, 8)
                }

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
                .padding(.top, 2)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)

            Spacer().frame(height: 2)

            Rectangle()
                .fill(colors.divider)
                .frame(height: 0.5)

            Spacer().frame(height: 6)
        }
        .background(colors.background)
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

// MARK: - Preview

#Preview("Feed Post") {
    let mockPosts = [
        ApiFeedPost(
            id: "1",
            content: "Acabou a luz aqui no bairro, alguem mais ta sem energia? Ja faz mais de 2 horas e nada da companhia resolver.",
            type: "Alerta",
            typeColor: "#FF6B35",
            categories: ["Energia", "Infraestrutura"],
            postedAt: "2026-03-06T14:30:00",
            signalsCount: 12,
            commentsCount: 5,
            distance: "350m"
        ),
        ApiFeedPost(
            id: "2",
            content: "Feira livre amanha na praca central, quem quiser frutas frescas aparece cedo que vale a pena!",
            type: "Evento",
            typeColor: "#4CAF50",
            categories: ["Comunidade"],
            postedAt: "2026-03-06T10:00:00",
            signalsCount: 3,
            commentsCount: 0,
            distance: "1.2km"
        ),
        ApiFeedPost(
            id: "3",
            content: "Cuidado com o buraco na rua das flores esquina com a av. brasil, quase perdi o pneu do carro ali.",
            type: "Perigo",
            typeColor: "#F44336",
            categories: ["Transito", "Infraestrutura"],
            postedAt: "2026-03-05T22:15:00",
            signalsCount: 28,
            commentsCount: 11,
            distance: "800m"
        ),
    ]

    ScrollView {
        LazyVStack(spacing: 0) {
            ForEach(mockPosts) { post in
                FeedPostItem(post: post)
            }
        }
    }
    .background(Color.black)
    .withAppTheme()
    .preferredColorScheme(.dark)
}
