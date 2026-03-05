import SwiftUI

struct MoodDetailScreen: View {
    var onDismiss: () -> Void

    @Environment(\.appColors) private var colors
    @EnvironmentObject var lang: LanguageManager

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Text(lang.s("mood_title"))
                    .font(.system(size: 17, weight: .semibold))
                    .tracking(-0.2)
                    .foregroundColor(colors.onAccent)
                Spacer()
                Button(action: onDismiss) {
                    Image(systemName: "xmark")
                        .font(.system(size: 15, weight: .medium))
                        .foregroundColor(colors.onAccent.opacity(0.7))
                        .frame(width: 32, height: 32)
                }
                .buttonStyle(.plain)
            }
            .padding(.horizontal, 20)
            .padding(.top, 8)
            .padding(.bottom, 16)

            Rectangle()
                .fill(colors.onAccent.opacity(0.2))
                .frame(height: 0.5)

            VStack(alignment: .leading, spacing: 8) {
                Text(lang.s("mood_calm"))
                    .font(.system(size: 28, weight: .bold))
                    .tracking(-0.3)
                    .foregroundColor(colors.onAccent)

                Text(lang.s("mood_updated"))
                    .font(.system(size: 14))
                    .foregroundColor(colors.onAccent.opacity(0.7))
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 20)
            .padding(.top, 24)

            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(colors.accent)
    }
}

// MARK: - Preview

#Preview {
    MoodDetailScreen(onDismiss: {})
}
