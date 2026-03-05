import SwiftUI
import CoreLocation

// MARK: - Feed

struct Feed: View {
    let posts: [ApiFeedPost]
    var userLocation: CLLocation?
    var isLoading: Bool = false
    var onScrollOffsetChanged: ((CGFloat) -> Void)? = nil
    var topInset: CGFloat = 0
    var bottomInset: CGFloat = 0
    var onSignalClick: (ApiFeedPost) -> Void = { _ in }
    var onCommentClick: (ApiFeedPost) -> Void = { _ in }
    var onShareClick: (ApiFeedPost) -> Void = { _ in }
    var onReportClick: (ApiFeedPost) -> Void = { _ in }
    var onLoadMore: () -> Void = {}

    @Environment(\.appColors) private var colors

    var body: some View {
        if isLoading && posts.isEmpty {
            VStack {
                Spacer()
                ProgressView().tint(colors.accent)
                Spacer()
            }
        } else if !isLoading && posts.isEmpty {
            VStack {
                Spacer()
                Text("Nenhuma publicação por perto")
                    .font(.system(size: 14))
                    .foregroundColor(colors.textSecondary)
                Spacer()
            }
        } else {
            ScrollView {
                LazyVStack(spacing: 0) {
                    ForEach(posts) { post in
                        FeedPostItem(
                            post: post,
                            distanceText: distanceText(for: post),
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
                .padding(.top, topInset)
                .padding(.bottom, bottomInset)
                .background(
                    ScrollViewObserver { offset in
                        onScrollOffsetChanged?(offset)
                    }
                )
            }
        }
    }

    private func distanceText(for post: ApiFeedPost) -> String? {
        guard let loc = userLocation, let lat = post.latitude, let lng = post.longitude else { return nil }
        let dist = LocationService.distanceBetween(
            from: loc.coordinate,
            to: CLLocationCoordinate2D(latitude: lat, longitude: lng)
        )
        if dist < 100 { return "A menos de 100m" }
        if dist < 1000 { return "A \(Int(dist))m" }
        return String(format: "A %.1fkm", dist / 1000)
    }
}

// MARK: - Feed Post Item

struct FeedPostItem: View {
    let post: ApiFeedPost
    var distanceText: String?
    var onSignalClick: () -> Void = {}
    var onCommentClick: () -> Void = {}
    var onShareClick: () -> Void = {}
    var onReportClick: () -> Void = {}

    @Environment(\.appColors) private var colors
    @State private var showMenu = false

    var body: some View {
        VStack(spacing: 0) {
            VStack(alignment: .leading, spacing: 0) {
                // Header: type badge + category + time + 3-dot menu
                HStack(spacing: 6) {
                    TypeBadge(type: post.typeLabel)

                    if let cat = post.categoryLabel {
                        Text("·")
                            .font(.system(size: 12))
                            .foregroundColor(colors.textSecondary)
                        Text(cat)
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

                // Distance
                if let dist = distanceText {
                    Text(dist)
                        .font(.system(size: 11))
                        .foregroundColor(colors.textSecondary.opacity(0.7))
                        .padding(.top, 2)
                }

                // Content
                Text(post.originalText)
                    .font(.system(size: 15))
                    .foregroundColor(colors.textPrimary)
                    .lineSpacing(3)
                    .fixedSize(horizontal: false, vertical: true)
                    .padding(.top, 10)

                // Interaction buttons
                HStack(spacing: 20) {
                    InteractionBtn(icon: "antenna.radiowaves.left.and.right", label: "Sinal", action: onSignalClick)
                    InteractionBtn(icon: "bubble.left", label: "Comentar", action: onCommentClick)
                    InteractionBtn(icon: "square.and.arrow.up", label: "Compartilhar", action: onShareClick)
                }
                .padding(.top, 12)
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

private struct TypeBadge: View {
    let type: String
    @Environment(\.appColors) private var colors

    private var badgeColor: Color {
        switch type.lowercased() {
        case "alerta": return Color.orange.opacity(0.12)
        case "relato": return Color.blue.opacity(0.12)
        case "denúncia", "denuncia": return Color.red.opacity(0.12)
        case "informação", "informacao": return Color.blue.opacity(0.12)
        case "comércio", "comercio": return Color.purple.opacity(0.12)
        case "pedido": return Color.green.opacity(0.12)
        case "reclamação", "reclamacao": return Color.orange.opacity(0.12)
        default: return Color.gray.opacity(0.12)
        }
    }

    private var textColor: Color {
        switch type.lowercased() {
        case "alerta": return .orange
        case "relato": return .blue
        case "denúncia", "denuncia": return .red
        case "informação", "informacao": return .blue
        case "comércio", "comercio": return .purple
        case "pedido": return .green
        case "reclamação", "reclamacao": return .orange
        default: return .gray
        }
    }

    var body: some View {
        Text(type)
            .font(.system(size: 11, weight: .semibold))
            .foregroundColor(textColor)
            .padding(.horizontal, 6)
            .padding(.vertical, 2)
            .background(badgeColor)
            .clipShape(RoundedRectangle(cornerRadius: 4))
    }
}

private struct InteractionBtn: View {
    let icon: String
    let label: String
    let action: () -> Void

    @Environment(\.appColors) private var colors

    var body: some View {
        Button(action: action) {
            HStack(spacing: 4) {
                Image(systemName: icon)
                    .font(.system(size: 14))
                Text(label)
                    .font(.system(size: 12, weight: .medium))
            }
            .foregroundColor(colors.textSecondary)
        }
        .buttonStyle(.plain)
    }
}
