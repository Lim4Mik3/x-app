import SwiftUI

struct CreatePostScreen: View {
    var onDismiss: () -> Void
    var onPostCreated: () -> Void = {}

    @State private var text = ""
    @State private var isPublishing = false
    @State private var errorMessage: String?
    @State private var isRecording = false
    @State private var recordingSeconds = 0
    @State private var wavePhases: [Bool] = Array(repeating: false, count: 20)

    @Environment(\.appColors) private var colors

    let recordingTimer = Timer.publish(every: 1, on: .main, in: .common).autoconnect()

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Text("Novo post")
                    .font(.system(size: 17, weight: .semibold))
                    .tracking(-0.2)
                    .foregroundColor(colors.textPrimary)
                Spacer()
                Button(action: onDismiss) {
                    Image(systemName: "xmark")
                        .font(.system(size: 15, weight: .medium))
                        .foregroundColor(colors.textSecondary)
                        .frame(width: 32, height: 32)
                }
                .buttonStyle(.plain)
            }
            .padding(.horizontal, 20)
            .padding(.top, 8)
            .padding(.bottom, 16)

            Rectangle().fill(colors.divider).frame(height: 0.5)

            // Text editor
            TextEditor(text: $text)
                .font(.system(size: 16))
                .foregroundColor(colors.textPrimary)
                .scrollContentBackground(.hidden)
                .background(Color.clear)
                .overlay(alignment: .topLeading) {
                    if text.isEmpty {
                        Text("No que você está pensando?")
                            .font(.system(size: 16))
                            .foregroundColor(colors.textSecondary)
                            .padding(.horizontal, 5)
                            .padding(.vertical, 8)
                            .allowsHitTesting(false)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 8)
                .frame(maxHeight: .infinity)

            // Error
            if let error = errorMessage {
                Text(error)
                    .font(.system(size: 12))
                    .foregroundColor(colors.destructive)
                    .padding(.horizontal, 20)
                    .padding(.bottom, 4)
            }

            // Bottom bar
            Rectangle().fill(colors.divider).frame(height: 0.5)

            HStack(spacing: 10) {
                // Main button: Publicar or Recording
                ZStack {
                    // Publish mode
                    Button(action: publishPost) {
                        HStack(spacing: 8) {
                            if isPublishing {
                                ProgressView()
                                    .tint(colors.onAccent)
                                Text("Publicando...")
                                    .font(.system(size: 15, weight: .semibold))
                            } else {
                                Image(systemName: "paperplane.fill")
                                    .font(.system(size: 15, weight: .medium))
                                Text("Publicar")
                                    .font(.system(size: 15, weight: .semibold))
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 48)
                        .background(text.isEmpty || isPublishing ? colors.accent.opacity(0.3) : colors.accent)
                        .foregroundColor(colors.onAccent)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                    .buttonStyle(.plain)
                    .disabled(text.isEmpty || isPublishing)
                    .opacity(isRecording ? 0 : 1)

                    // Recording mode
                    HStack(spacing: 0) {
                        Button {
                            withAnimation(.easeInOut(duration: 0.2)) {
                                isRecording = false
                                stopWaveAnimation()
                            }
                        } label: {
                            Image(systemName: "xmark")
                                .font(.system(size: 15, weight: .medium))
                                .foregroundColor(colors.textSecondary)
                                .frame(width: 40, height: 48)
                        }
                        .buttonStyle(.plain)

                        AudioWaveBars(barColor: colors.accent, phases: wavePhases)
                            .frame(maxWidth: .infinity)

                        Text(formatTime(recordingSeconds))
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(colors.textSecondary)
                            .monospacedDigit()
                            .padding(.horizontal, 12)
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 48)
                    .background(colors.surface)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .opacity(isRecording ? 1 : 0)
                }
                .animation(.easeInOut(duration: 0.3), value: isRecording)

                // Mic / Send audio button
                Button {
                    withAnimation(.easeInOut(duration: 0.2)) {
                        if isRecording {
                            isRecording = false
                            stopWaveAnimation()
                        } else {
                            isRecording = true
                            recordingSeconds = 0
                            startWaveAnimation()
                        }
                    }
                } label: {
                    ZStack {
                        Image(systemName: "mic")
                            .font(.system(size: 18, weight: .medium))
                            .opacity(isRecording ? 0 : 1)
                        Image(systemName: "paperplane.fill")
                            .font(.system(size: 16, weight: .medium))
                            .opacity(isRecording ? 1 : 0)
                    }
                    .foregroundColor(isRecording ? colors.onAccent : colors.textSecondary)
                    .frame(width: 48, height: 48)
                    .background(isRecording ? colors.accent : colors.surface)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
                .buttonStyle(.plain)
                .animation(.easeInOut(duration: 0.2), value: isRecording)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(colors.background)
        .onReceive(recordingTimer) { _ in
            if isRecording { recordingSeconds += 1 }
        }
    }

    private func publishPost() {
        guard !text.isEmpty, !isPublishing else { return }
        isPublishing = true
        errorMessage = nil

        Task {
            let loc = LocationService.shared.cachedLocation
            guard let loc = loc else {
                LocationService.shared.fetch(forceRefresh: true)
                try? await Task.sleep(nanoseconds: 500_000_000)
                guard let loc2 = LocationService.shared.cachedLocation else {
                    await MainActor.run {
                        errorMessage = "Localização não disponível"
                        isPublishing = false
                    }
                    return
                }
                do {
                    _ = try await ApiClient.shared.createPost(
                        text: text, lat: loc2.coordinate.latitude, lng: loc2.coordinate.longitude
                    )
                    await MainActor.run {
                        isPublishing = false
                        onPostCreated()
                        onDismiss()
                    }
                } catch {
                    await MainActor.run {
                        isPublishing = false
                        errorMessage = error.localizedDescription
                    }
                }
                return
            }

            do {
                _ = try await ApiClient.shared.createPost(
                    text: text, lat: loc.coordinate.latitude, lng: loc.coordinate.longitude
                )
                await MainActor.run {
                    isPublishing = false
                    onPostCreated()
                    onDismiss()
                }
            } catch {
                await MainActor.run {
                    isPublishing = false
                    errorMessage = error.localizedDescription
                }
            }
        }
    }

    private func startWaveAnimation() {
        for i in 0..<wavePhases.count {
            let mod = i % 7
            let delay = Double(mod) * 0.12
            DispatchQueue.main.asyncAfter(deadline: .now() + delay) {
                withAnimation(
                    .easeInOut(duration: 0.4 + Double(mod) * 0.08)
                    .repeatForever(autoreverses: true)
                ) {
                    wavePhases[i] = true
                }
            }
        }
    }

    private func stopWaveAnimation() {
        withAnimation(.easeInOut(duration: 0.2)) {
            wavePhases = Array(repeating: false, count: 20)
        }
    }

    private func formatTime(_ seconds: Int) -> String {
        let mins = seconds / 60
        let secs = seconds % 60
        return "\(mins):\(String(format: "%02d", secs))"
    }
}

struct AudioWaveBars: View {
    var barColor: Color
    var phases: [Bool]

    var body: some View {
        HStack(spacing: 0) {
            ForEach(0..<phases.count, id: \.self) { index in
                RoundedRectangle(cornerRadius: 2)
                    .fill(barColor)
                    .frame(width: 4, height: phases[index] ? 22 : 6)
                    .frame(maxWidth: .infinity)
            }
        }
        .frame(height: 22)
    }
}
