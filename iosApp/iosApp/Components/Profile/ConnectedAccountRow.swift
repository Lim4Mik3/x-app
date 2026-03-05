import SwiftUI

struct ConnectedAccountRow: View {
    let icon: String
    let name: String
    let connected: Bool
    let statusLabel: String
    let connectLabel: String
    var onToggle: () -> Void = {}

    @Environment(\.appColors) private var colors

    var body: some View {
        Button(action: onToggle) {
            HStack(spacing: 14) {
                Image(systemName: icon)
                    .font(.system(size: 16))
                    .foregroundColor(colors.textSecondary)
                    .frame(width: 20, height: 20)

                Text(name)
                    .font(.system(size: 15))
                    .foregroundColor(colors.textPrimary)

                Spacer()

                Text(connected ? statusLabel : connectLabel)
                    .font(.system(size: 13, weight: connected ? .medium : .regular))
                    .foregroundColor(connected ? colors.badgeText : colors.accent)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
        }
        .buttonStyle(.plain)
    }
}
