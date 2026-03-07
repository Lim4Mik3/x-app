import SwiftUI

struct ToastMessage: Identifiable, Equatable {
    let id = UUID()
    let text: String
    let type: ToastType

    enum ToastType {
        case error, success, info

        var icon: String {
            switch self {
            case .error: return "xmark.circle.fill"
            case .success: return "checkmark.circle.fill"
            case .info: return "info.circle.fill"
            }
        }

        var color: Color {
            switch self {
            case .error: return Color(red: 0.85, green: 0.25, blue: 0.25)
            case .success: return Color(red: 0.2, green: 0.7, blue: 0.45)
            case .info: return Color(red: 0.3, green: 0.5, blue: 0.9)
            }
        }
    }
}

@MainActor
final class ToastManager: ObservableObject {
    static let shared = ToastManager()
    @Published var current: ToastMessage?
    private var dismissTask: Task<Void, Never>?

    private init() {}

    func show(_ text: String, type: ToastMessage.ToastType = .error) {
        dismissTask?.cancel()
        withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
            current = ToastMessage(text: text, type: type)
        }
        dismissTask = Task {
            try? await Task.sleep(nanoseconds: 3_500_000_000)
            guard !Task.isCancelled else { return }
            withAnimation(.easeOut(duration: 0.25)) {
                current = nil
            }
        }
    }
}

// MARK: - Toast View

struct ToastOverlay: View {
    @ObservedObject private var manager = ToastManager.shared

    var body: some View {
        VStack {
            if let toast = manager.current {
                HStack(spacing: 10) {
                    Image(systemName: toast.type.icon)
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(.white)

                    Text(toast.text)
                        .font(.system(size: 13, weight: .medium))
                        .foregroundColor(.white)
                        .lineLimit(2)

                    Spacer()
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
                .background(
                    RoundedRectangle(cornerRadius: 12)
                        .fill(toast.type.color)
                        .shadow(color: .black.opacity(0.2), radius: 8, y: 4)
                )
                .padding(.horizontal, 16)
                .transition(.move(edge: .top).combined(with: .opacity))
                .onTapGesture {
                    withAnimation(.easeOut(duration: 0.2)) {
                        manager.current = nil
                    }
                }
            }

            Spacer()
        }
        .padding(.top, 8)
        .allowsHitTesting(manager.current != nil)
        .animation(.spring(response: 0.35, dampingFraction: 0.8), value: manager.current)
    }
}
