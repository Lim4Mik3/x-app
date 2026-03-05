import SwiftUI

struct Header: View {
    let locationName: String
    let timeAgo: String
    let statusLabel: String
    var onLocationTap: () -> Void = {}
    var onMoodTap: () -> Void = {}

    @Environment(\.appColors) private var colors

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                LocationSelector(
                    locationName: locationName,
                    onTap: onLocationTap
                )
                Spacer()
                StatusInfo(
                    timeAgo: timeAgo,
                    statusLabel: statusLabel,
                    onTap: onMoodTap
                )
            }
            .padding(.horizontal, 20)
            .padding(.top, 4)
            .padding(.bottom, 12)

            Rectangle()
                .fill(colors.divider)
                .frame(height: 0.5)
        }
    }
}

// MARK: - Location Selector

private struct LocationSelector: View {
    let locationName: String
    var onTap: () -> Void = {}

    @Environment(\.appColors) private var colors

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 3) {
                Text(locationName)
                    .font(.system(size: 17, weight: .semibold))
                    .tracking(-0.2)
                    .foregroundColor(colors.textPrimary)
                Image(systemName: "chevron.down")
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(colors.textSecondary)
            }
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Status Info

private struct StatusInfo: View {
    let timeAgo: String
    let statusLabel: String
    var onTap: () -> Void = {}

    @Environment(\.appColors) private var colors

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 6) {
                Text(timeAgo)
                    .font(.system(size: 12))
                    .tracking(-0.1)
                    .foregroundColor(colors.textSecondary)
                Text("\u{00B7}")
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(colors.textSecondary)
                StatusBadge(label: statusLabel)
            }
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Status Badge

private struct StatusBadge: View {
    let label: String

    @Environment(\.appColors) private var colors

    var body: some View {
        Text(label)
            .font(.system(size: 11, weight: .medium))
            .tracking(-0.1)
            .foregroundColor(colors.badgeText)
            .padding(.horizontal, 10)
            .padding(.vertical, 3)
            .background(colors.badgeBackground)
            .clipShape(RoundedRectangle(cornerRadius: 10))
    }
}

// MARK: - Preview

#Preview {
    Header(
        locationName: "Osasco",
        timeAgo: "2min ago",
        statusLabel: "Tranquilo"
    )
}
