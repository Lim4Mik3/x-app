import SwiftUI

struct Header: View {
    let locationName: String

    @Environment(\.appColors) private var colors

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 6) {
                Image(systemName: "location.fill")
                    .font(.system(size: 14))
                    .foregroundColor(colors.accent)
                Text(locationName)
                    .font(.system(size: 17, weight: .semibold))
                    .tracking(-0.2)
                    .foregroundColor(colors.textPrimary)
                Spacer()
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
