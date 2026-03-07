import SwiftUI

struct SignalPopup: View {
    let postId: String
    let typeKey: String
    var onSignalsUpdated: (Int) -> Void = { _ in }
    var onDismiss: () -> Void

    @Environment(\.appColors) private var colors
    @EnvironmentObject var lang: LanguageManager
    @State private var signalKeys: [SignalKey] = []
    @State private var signalGroups: [SignalGroup] = []
    @State private var postSignals: PostSignals?
    @State private var selectedKeys: Set<String> = []
    @State private var isLoading = true
    @State private var unsupportedType = false
    @State private var sheetY: CGFloat = UIScreen.main.bounds.height
    @State private var backdropOpacity: Double = 0
    @State private var dragOffset: CGFloat = 0

    private var hasChanges: Bool {
        selectedKeys != Set(postSignals?.mySignals ?? [])
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            Color.black.opacity(backdropOpacity)
                .ignoresSafeArea()
                .onTapGesture { dismiss() }

            sheetContent
                .offset(y: sheetY + max(dragOffset, 0))
                .gesture(
                    DragGesture()
                        .onChanged { value in
                            dragOffset = value.translation.height
                        }
                        .onEnded { value in
                            if value.translation.height > 120 || value.velocity.height > 500 {
                                dismiss()
                            } else {
                                withAnimation(.interpolatingSpring(stiffness: 300, damping: 30)) {
                                    dragOffset = 0
                                }
                            }
                        }
                )
        }
        .ignoresSafeArea(edges: .bottom)
        .task {
            await loadData()
            // Data loaded — now animate in
            withAnimation(.interpolatingSpring(stiffness: 350, damping: 28)) {
                sheetY = 0
                backdropOpacity = 0.5
            }
        }
    }

    // Sheet is ALWAYS built — never conditionally removed/added
    private var sheetContent: some View {
        VStack(spacing: 0) {
            // Drag handle
            Capsule()
                .fill(colors.divider)
                .frame(width: 36, height: 5)
                .padding(.top, 10)
                .padding(.bottom, 16)

            // Header
            VStack(alignment: .leading, spacing: 8) {
                Text(lang.s("signal_title"))
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(colors.textPrimary)
                Text(lang.s("signal_subtitle"))
                    .font(.system(size: 13))
                    .foregroundColor(colors.textSecondary)
                    .lineSpacing(2)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 20)
            .padding(.bottom, 20)

            if isLoading {
                ProgressView().tint(colors.accent)
                    .frame(height: 200)
                    .frame(maxWidth: .infinity)
            } else if unsupportedType {
                VStack(spacing: 12) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.system(size: 32))
                        .foregroundColor(colors.textSecondary.opacity(0.5))
                    Text(lang.s("signal_unsupported"))
                        .font(.system(size: 14))
                        .foregroundColor(colors.textSecondary)
                        .multilineTextAlignment(.center)
                }
                .frame(height: 160)
                .frame(maxWidth: .infinity)
                .padding(.horizontal, 20)
            } else {
                ScrollView(showsIndicators: false) {
                    VStack(spacing: 24) {
                        ForEach(signalGroups) { group in
                            SignalGroupView(
                                group: group,
                                selectedKeys: $selectedKeys,
                                signalCounts: postSignals?.signals ?? [:]
                            )
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.bottom, 8)
                }
                .frame(maxHeight: UIScreen.main.bounds.height * 0.75)
                .fixedSize(horizontal: false, vertical: true)
            }

            Spacer().frame(height: 34)
        }
        .background(
            UnevenRoundedRectangle(topLeadingRadius: 24, topTrailingRadius: 24)
                .fill(colors.surface)
                .shadow(color: .black.opacity(0.3), radius: 20, y: -5)
        )
    }

    private func dismiss() {
        // Animate out
        withAnimation(.interpolatingSpring(stiffness: 400, damping: 30)) {
            sheetY = UIScreen.main.bounds.height
            backdropOpacity = 0
            dragOffset = 0
        }

        // Sync with API if there are changes
        if hasChanges {
            // Optimistic count: apply delta to current total
            let oldKeys = Set(postSignals?.mySignals ?? [])
            let added = selectedKeys.subtracting(oldKeys).count
            let removed = oldKeys.subtracting(selectedKeys).count
            let currentTotal = postSignals?.signals.values.reduce(0, +) ?? 0
            let previousTotal = currentTotal
            let optimisticTotal = max(currentTotal + added - removed, 0)
            onSignalsUpdated(optimisticTotal)

            let keys = Array(selectedKeys)
            let pid = postId
            let callback = onSignalsUpdated
            Task {
                do {
                    let response = try await ApiClient.shared.syncSignals(postId: pid, signalKeys: keys)
                    let serverTotal = response.signals.values.reduce(0, +)
                    await MainActor.run { callback(serverTotal) }
                } catch {
                    // Revert optimistic update and show error
                    await MainActor.run {
                        callback(previousTotal)
                        ToastManager.shared.show(LanguageManager.shared.s("toast_signal_error"))
                    }
                }
            }
        }

        DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
            onDismiss()
        }
    }

    private func loadData() async {
        isLoading = true
        if let keys = await SignalKeysCache.shared.getKeys(for: typeKey) {
            signalKeys = keys
            signalGroups = SignalGroup.groupFromKeys(keys)
            postSignals = try? await ApiClient.shared.getPostSignals(postId: postId)
            if let existing = postSignals?.mySignals {
                selectedKeys = Set(existing)
            }
        } else {
            unsupportedType = true
        }
        isLoading = false
    }
}

// MARK: - Signal Group View

struct SignalGroupView: View {
    let group: SignalGroup
    @Binding var selectedKeys: Set<String>
    let signalCounts: [String: Int]

    @Environment(\.appColors) private var colors
    @EnvironmentObject var lang: LanguageManager

    private var localizedLabel: String {
        switch group.category {
        case "verification": return lang.s("signal_section_verification")
        case "reaction": return lang.s("signal_section_reaction")
        default: return group.label
        }
    }

    var body: some View {
        VStack(spacing: 14) {
            HStack(spacing: 8) {
                Rectangle()
                    .fill(colors.divider)
                    .frame(height: 0.5)
                Text(localizedLabel.uppercased())
                    .font(.system(size: 10, weight: .semibold))
                    .foregroundColor(colors.textSecondary)
                    .tracking(1)
                    .fixedSize()
                Rectangle()
                    .fill(colors.divider)
                    .frame(height: 0.5)
            }

            ForEach(group.pairs) { pair in
                HStack(spacing: 16) {
                    SignalItemView(
                        signal: pair.left,
                        isPositive: true,
                        isSelected: selectedKeys.contains(pair.left.key),
                        count: signalCounts[pair.left.key] ?? 0,
                        onTap: { toggleSignal(pair.left, opposite: pair.right) }
                    )

                    if pair.left.key != pair.right.key {
                        SignalItemView(
                            signal: pair.right,
                            isPositive: false,
                            isSelected: selectedKeys.contains(pair.right.key),
                            count: signalCounts[pair.right.key] ?? 0,
                            onTap: { toggleSignal(pair.right, opposite: pair.left) }
                        )
                    }
                }
            }
        }
    }

    private func toggleSignal(_ signal: SignalKey, opposite: SignalKey) {
        withAnimation(.easeInOut(duration: 0.2)) {
            if selectedKeys.contains(signal.key) {
                selectedKeys.remove(signal.key)
            } else {
                selectedKeys.insert(signal.key)
                if signal.key != opposite.key {
                    selectedKeys.remove(opposite.key)
                }
            }
        }
    }
}

// MARK: - Signal Item (circle style)

private let positiveColor = Color(red: 0.2, green: 0.7, blue: 0.5)
private let negativeColor = Color(red: 0.85, green: 0.35, blue: 0.35)

struct SignalItemView: View {
    let signal: SignalKey
    let isPositive: Bool
    let isSelected: Bool
    let count: Int
    let onTap: () -> Void

    @Environment(\.appColors) private var colors

    private var tintColor: Color {
        isPositive ? positiveColor : negativeColor
    }

    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 6) {
                ZStack {
                    Circle()
                        .fill(isSelected ? tintColor.opacity(0.2) : tintColor.opacity(0.06))
                        .frame(width: 52, height: 52)

                    Circle()
                        .stroke(isSelected ? tintColor.opacity(0.5) : tintColor.opacity(0.12), lineWidth: 1.5)
                        .frame(width: 52, height: 52)

                    Text(emojiForSignal(signal.key))
                        .font(.system(size: 24))
                        .saturation(isSelected ? 1 : 0)
                        .scaleEffect(isSelected ? 1.1 : 1.0)

                    if count > 0 {
                        Text(formatCompactNumber(count))
                            .font(.system(size: 11, weight: .medium))
                            .tracking(0.3)
                            .foregroundColor(.white)
                            .padding(.horizontal, 7)
                            .padding(.vertical, 3)
                            .background(Capsule().fill(isSelected ? tintColor : colors.divider))
                            .offset(x: 22, y: -22)
                    }
                }

                Text(signal.label)
                    .font(.system(size: 10, weight: isSelected ? .semibold : .regular))
                    .foregroundColor(isSelected ? tintColor : colors.textSecondary)
                    .multilineTextAlignment(.center)
                    .lineLimit(2)
                    .fixedSize(horizontal: false, vertical: true)
            }
            .frame(maxWidth: .infinity)
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Emoji mapping

private func emojiForSignal(_ key: String) -> String {
    let map: [String: String] = [
        "saw_it": "👀",
        "i_confirm": "✅",
        "is_recurring": "🔁",
        "didnt_see": "🤷",
        "not_true": "🚫",
        "exaggerated": "😤",
        "solidarity": "🤝",
        "outrage": "😡",
        "fear": "😨",
        "relief": "😮‍💨",
        "gratitude": "🙏",
        "still_happening": "⏳",
        "resolved": "🔧",
        "me_too": "🙋",
        "urgent": "🔥",
        "helpful": "💡",
        "funny": "😂",
        "sad": "😢",
        "love": "❤️",
        "danger": "⚠️",
        "safe": "🛡️",
    ]
    return map[key] ?? "📌"
}
