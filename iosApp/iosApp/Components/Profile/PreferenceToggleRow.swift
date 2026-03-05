import SwiftUI

struct PreferenceToggleRow: View {
    let label: String
    @Binding var isOn: Bool

    @Environment(\.appColors) private var colors

    var body: some View {
        HStack {
            Text(label)
                .font(.system(size: 15))
                .foregroundColor(colors.textPrimary)

            Spacer()

            Toggle("", isOn: $isOn)
                .labelsHidden()
                .tint(colors.accent)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
    }
}
