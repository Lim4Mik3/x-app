import SwiftUI

struct LocationPickerScreen: View {
    var onDismiss: () -> Void
    var onLocationSelected: (String) -> Void

    @Environment(\.appColors) private var colors
    @EnvironmentObject var lang: LanguageManager

    private let locations = ["Osasco", "São Paulo", "Barueri", "Carapicuíba"]

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Text(lang.s("select_location"))
                    .font(.system(size: 17, weight: .semibold))
                    .tracking(-0.2)
                    .foregroundColor(colors.onDestructive)
                Spacer()
                Button(action: onDismiss) {
                    Image(systemName: "xmark")
                        .font(.system(size: 15, weight: .medium))
                        .foregroundColor(colors.onDestructive.opacity(0.7))
                        .frame(width: 32, height: 32)
                }
                .buttonStyle(.plain)
            }
            .padding(.horizontal, 20)
            .padding(.top, 8)
            .padding(.bottom, 16)

            Rectangle()
                .fill(colors.onDestructive.opacity(0.2))
                .frame(height: 0.5)

            VStack(spacing: 0) {
                ForEach(locations, id: \.self) { location in
                    Button(action: { onLocationSelected(location) }) {
                        HStack {
                            Text(location)
                                .font(.system(size: 16))
                                .foregroundColor(colors.onDestructive)
                            Spacer()
                        }
                        .padding(.horizontal, 20)
                        .padding(.vertical, 14)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.top, 8)

            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(colors.destructive)
    }
}

// MARK: - Preview

#Preview {
    LocationPickerScreen(
        onDismiss: {},
        onLocationSelected: { _ in }
    )
}
