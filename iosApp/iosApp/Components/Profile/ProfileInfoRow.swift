import SwiftUI

struct ProfileInfoRow: View {
    let icon: String
    let label: String
    let value: String

    @Environment(\.appColors) private var colors

    var body: some View {
        HStack(spacing: 14) {
            Image(systemName: icon)
                .font(.system(size: 16))
                .foregroundColor(colors.textSecondary)
                .frame(width: 20, height: 20)

            VStack(alignment: .leading, spacing: 1) {
                Text(label)
                    .font(.system(size: 12))
                    .foregroundColor(colors.textSecondary)

                Text(value)
                    .font(.system(size: 15))
                    .foregroundColor(colors.textPrimary)
            }

            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
    }
}
