import SwiftUI

struct CreatePostFab: View {
    var action: () -> Void

    @Environment(\.appColors) private var colors

    var body: some View {
        VStack {
            Spacer()
            HStack {
                Spacer()
                Button(action: action) {
                    Image(systemName: "plus")
                        .font(.system(size: 22, weight: .medium))
                        .foregroundColor(colors.onAccent)
                        .frame(width: 56, height: 56)
                        .background(colors.accent)
                        .clipShape(Circle())
                        .shadow(color: .black.opacity(0.15), radius: 8, x: 0, y: 4)
                }
                .buttonStyle(.plain)
                .padding(.trailing, 20)
            }
        }
    }
}

#Preview {
    CreatePostFab(action: {})
}
