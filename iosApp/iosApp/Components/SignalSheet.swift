import SwiftUI

struct SignalSheet: View {
    let postId: String
    let postType: String
    var onDismiss: () -> Void

    @Environment(\.appColors) private var colors
    @State private var signalKeys: [SignalKey] = []
    @State private var postSignals: PostSignals?
    @State private var selectedKey: String?
    @State private var isLoading = true
    @State private var isSending = false

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Text("Confirmar sinal")
                    .font(.system(size: 17, weight: .semibold))
                    .foregroundColor(colors.textPrimary)
                Spacer()
                Button(action: onDismiss) {
                    Image(systemName: "xmark")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(colors.textSecondary)
                        .frame(width: 28, height: 28)
                }
                .buttonStyle(.plain)
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 16)

            Rectangle().fill(colors.divider).frame(height: 0.5)

            if isLoading {
                Spacer().frame(height: 40)
                ProgressView().tint(colors.accent)
                Spacer().frame(height: 40)
            } else {
                VStack(spacing: 0) {
                    ForEach(signalKeys) { signal in
                        let isSelected = selectedKey == signal.key
                        let count = postSignals?.signals[signal.key] ?? 0

                        Button {
                            selectedKey = isSelected ? nil : signal.key
                        } label: {
                            HStack(spacing: 14) {
                                ZStack {
                                    Circle()
                                        .fill(isSelected ? colors.accent : colors.divider)
                                        .frame(width: 22, height: 22)
                                    if isSelected {
                                        Image(systemName: "checkmark")
                                            .font(.system(size: 11, weight: .bold))
                                            .foregroundColor(colors.onAccent)
                                    }
                                }

                                Text(signal.label)
                                    .font(.system(size: 15))
                                    .foregroundColor(colors.textPrimary)

                                Spacer()

                                if count > 0 {
                                    Text("\(count)")
                                        .font(.system(size: 13))
                                        .foregroundColor(colors.textSecondary)
                                }
                            }
                            .padding(.horizontal, 20)
                            .padding(.vertical, 14)
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.top, 8)

                Spacer().frame(height: 16)

                Button(action: confirmSignal) {
                    if isSending {
                        ProgressView().tint(colors.onAccent)
                    } else {
                        Text("Confirmar")
                            .font(.system(size: 15, weight: .semibold))
                    }
                }
                .frame(maxWidth: .infinity)
                .frame(height: 48)
                .background(selectedKey != nil && !isSending ? colors.accent : colors.accent.opacity(0.3))
                .foregroundColor(colors.onAccent)
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .padding(.horizontal, 20)
                .disabled(selectedKey == nil || isSending)
                .buttonStyle(.plain)
            }

            Spacer().frame(height: 24)
        }
        .background(colors.background)
        .task { await loadData() }
    }

    private func loadData() async {
        isLoading = true
        async let keysTask = ApiClient.shared.getSignalKeys(type: postType.isEmpty ? nil : postType)
        async let signalsTask = ApiClient.shared.getPostSignals(postId: postId)
        signalKeys = (try? await keysTask) ?? []
        postSignals = try? await signalsTask
        selectedKey = postSignals?.userSignals.first
        isLoading = false
    }

    private func confirmSignal() {
        guard let key = selectedKey else { return }
        isSending = true
        Task {
            let alreadySignaled = postSignals?.userSignals.contains(key) == true
            if alreadySignaled {
                try? await ApiClient.shared.removeSignal(postId: postId, signalKey: key)
            } else {
                if let existing = postSignals?.userSignals.first {
                    try? await ApiClient.shared.removeSignal(postId: postId, signalKey: existing)
                }
                try? await ApiClient.shared.addSignal(postId: postId, signalKey: key)
            }
            isSending = false
            onDismiss()
        }
    }
}
