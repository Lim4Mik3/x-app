import SwiftUI

struct ProfileSection<Content: View>: View {
    let title: String
    @ViewBuilder let content: () -> Content

    @Environment(\.appColors) private var colors

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.system(size: 13, weight: .semibold))
                .foregroundColor(colors.textSecondary)
                .padding(.leading, 4)

            VStack(spacing: 0) {
                content()
            }
            .background(colors.surface)
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }
}

struct ProfileDivider: View {
    @Environment(\.appColors) private var colors

    var body: some View {
        Rectangle()
            .fill(colors.divider)
            .frame(height: 0.5)
            .padding(.horizontal, 16)
    }
}
