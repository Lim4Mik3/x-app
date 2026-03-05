import SwiftUI

struct ReportSheet: View {
    let postId: String
    var onDismiss: () -> Void

    @Environment(\.appColors) private var colors
    @State private var reasons: [ReportReason] = []
    @State private var selectedReason: String?
    @State private var detail = ""
    @State private var isLoading = true
    @State private var isSending = false
    @State private var errorMsg: String?

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Text("Denunciar publicação")
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
                    ForEach(reasons) { reason in
                        let isSelected = selectedReason == reason.key

                        Button { selectedReason = reason.key } label: {
                            HStack(spacing: 14) {
                                ZStack {
                                    Circle()
                                        .fill(isSelected ? colors.destructive : colors.divider)
                                        .frame(width: 22, height: 22)
                                    if isSelected {
                                        Image(systemName: "checkmark")
                                            .font(.system(size: 11, weight: .bold))
                                            .foregroundColor(colors.onDestructive)
                                    }
                                }
                                Text(reason.label)
                                    .font(.system(size: 15))
                                    .foregroundColor(colors.textPrimary)
                                Spacer()
                            }
                            .padding(.horizontal, 20)
                            .padding(.vertical, 14)
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.top, 8)

                // Detail
                TextField("Detalhes (opcional)", text: $detail, axis: .vertical)
                    .font(.system(size: 14))
                    .foregroundColor(colors.textPrimary)
                    .padding(16)
                    .background(colors.surface)
                    .clipShape(RoundedRectangle(cornerRadius: 10))
                    .padding(.horizontal, 20)
                    .padding(.top, 12)
                    .lineLimit(1...4)

                if let err = errorMsg {
                    Text(err)
                        .font(.system(size: 12))
                        .foregroundColor(colors.destructive)
                        .padding(.horizontal, 20)
                        .padding(.top, 8)
                }

                Spacer().frame(height: 16)

                Button(action: submitReport) {
                    if isSending {
                        ProgressView().tint(colors.onDestructive)
                    } else {
                        Text("Denunciar")
                            .font(.system(size: 15, weight: .semibold))
                    }
                }
                .frame(maxWidth: .infinity)
                .frame(height: 48)
                .background(selectedReason != nil && !isSending ? colors.destructive : colors.destructive.opacity(0.3))
                .foregroundColor(colors.onDestructive)
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .padding(.horizontal, 20)
                .disabled(selectedReason == nil || isSending)
                .buttonStyle(.plain)
            }

            Spacer().frame(height: 24)
        }
        .background(colors.background)
        .task { await loadReasons() }
    }

    private func loadReasons() async {
        isLoading = true
        reasons = (try? await ApiClient.shared.getReportReasons()) ?? []
        isLoading = false
    }

    private func submitReport() {
        guard let reason = selectedReason else { return }
        isSending = true
        errorMsg = nil
        Task {
            do {
                _ = try await ApiClient.shared.reportPost(
                    postId: postId,
                    reason: reason,
                    detail: detail.isEmpty ? nil : detail
                )
                isSending = false
                onDismiss()
            } catch let error as ApiError {
                isSending = false
                if case .http(409, _) = error {
                    errorMsg = "Você já denunciou esta publicação"
                } else {
                    errorMsg = error.localizedDescription
                }
            } catch {
                isSending = false
                errorMsg = error.localizedDescription
            }
        }
    }
}
