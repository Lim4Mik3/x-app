import SwiftUI

struct ReviewPostScreen: View {
    var postText: String
    var location: String
    var mediaUrl: URL? = nil
    var onDismiss: () -> Void
    var onConfirm: () -> Void

    @Environment(\.appColors) private var colors
    @EnvironmentObject var lang: LanguageManager

    var body: some View {
        VStack(spacing: 0) {
            // Header
            VStack(alignment: .leading, spacing: 2) {
                HStack(alignment: .top) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(lang.s("review_title"))
                            .font(.system(size: 20, weight: .bold))
                            .foregroundColor(colors.textPrimary)
                        Text(lang.s("review_subtitle"))
                            .font(.system(size: 13))
                            .foregroundColor(colors.textSecondary)
                    }
                    Spacer()
                    Button(action: onDismiss) {
                        Image(systemName: "xmark")
                            .font(.system(size: 15, weight: .medium))
                            .foregroundColor(colors.textSecondary)
                            .frame(width: 32, height: 32)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 20)
            .padding(.top, 8)
            .padding(.bottom, 16)

            Rectangle()
                .fill(colors.divider)
                .frame(height: 0.5)

            // Scrollable content
            ScrollView {
                VStack(spacing: 0) {
                    // Content section
                    ReviewSection(label: lang.s("review_text_label")) {
                        Text(postText.isEmpty ? "-" : postText)
                            .font(.system(size: 15))
                            .foregroundColor(colors.textPrimary)
                            .lineSpacing(4)
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }

                    // Location section
                    ReviewSection(label: lang.s("review_location_label")) {
                        HStack(spacing: 8) {
                            Image(systemName: "mappin.circle.fill")
                                .font(.system(size: 16))
                                .foregroundColor(colors.accent)
                            Text(location)
                                .font(.system(size: 15))
                                .foregroundColor(colors.textPrimary)
                                .lineLimit(1)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                    }

                    // Media section
                    ReviewSection(label: lang.s("review_media_label")) {
                        if let mediaUrl = mediaUrl,
                           let uiImage = UIImage(contentsOfFile: mediaUrl.path) {
                            Image(uiImage: uiImage)
                                .resizable()
                                .aspectRatio(16/9, contentMode: .fill)
                                .clipShape(RoundedRectangle(cornerRadius: 8))
                        } else {
                            HStack(spacing: 8) {
                                Image(systemName: "photo")
                                    .font(.system(size: 16))
                                    .foregroundColor(colors.textSecondary)
                                Text(lang.s("no_media_attached"))
                                    .font(.system(size: 14))
                                    .foregroundColor(colors.textSecondary)
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                        }
                    }

                    Spacer().frame(height: 16)
                }
            }

            // Divider above button
            Rectangle()
                .fill(colors.divider)
                .frame(height: 0.5)

            // Publish button
            Button(action: onConfirm) {
                Text(lang.s("confirm_publish"))
                    .font(.system(size: 15, weight: .semibold))
                    .frame(maxWidth: .infinity)
                    .frame(height: 48)
                    .background(colors.accent)
                    .foregroundColor(colors.onAccent)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            .buttonStyle(.plain)
            .padding(.horizontal, 20)
            .padding(.vertical, 16)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(colors.background)
    }
}

private struct ReviewSection<Content: View>: View {
    var label: String
    @ViewBuilder var content: () -> Content

    @Environment(\.appColors) private var colors

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(label.uppercased())
                .font(.system(size: 11, weight: .semibold))
                .foregroundColor(colors.textSecondary)
                .tracking(0.8)

            VStack {
                content()
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .background(colors.surface)
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
        .padding(.horizontal, 20)
        .padding(.top, 16)
    }
}

#Preview {
    ReviewPostScreen(
        postText: "Hello world",
        location: "Osasco",
        onDismiss: {},
        onConfirm: {}
    )
}
