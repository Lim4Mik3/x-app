import SwiftUI
import UIKit

struct ProfileHeader: View {
    let name: String
    let email: String
    let initials: String
    var photoData: Data?
    var onAvatarTap: () -> Void = {}

    @Environment(\.appColors) private var colors

    var body: some View {
        VStack(spacing: 0) {
            // Avatar with camera badge
            Button(action: onAvatarTap) {
                ZStack(alignment: .bottomTrailing) {
                    // Avatar circle
                    ZStack {
                        Circle()
                            .fill(colors.accent)
                            .frame(width: 80, height: 80)

                        if let data = photoData, let uiImage = UIImage(data: data) {
                            Image(uiImage: uiImage)
                                .resizable()
                                .scaledToFill()
                                .frame(width: 80, height: 80)
                                .clipShape(Circle())
                        } else {
                            Text(initials)
                                .font(.system(size: 28, weight: .semibold))
                                .foregroundColor(colors.onAccent)
                        }
                    }

                    // Camera badge
                    ZStack {
                        Circle()
                            .fill(colors.surface)
                            .frame(width: 28, height: 28)

                        Circle()
                            .fill(colors.accent)
                            .frame(width: 24, height: 24)

                        Image(systemName: "camera.fill")
                            .font(.system(size: 11))
                            .foregroundColor(colors.onAccent)
                    }
                    .offset(x: 2, y: 2)
                }
            }
            .buttonStyle(.plain)

            Spacer().frame(height: 12)

            Text(name)
                .font(.system(size: 20, weight: .semibold))
                .foregroundColor(colors.textPrimary)

            Spacer().frame(height: 2)

            Text(email)
                .font(.system(size: 14))
                .foregroundColor(colors.textSecondary)
        }
    }
}
