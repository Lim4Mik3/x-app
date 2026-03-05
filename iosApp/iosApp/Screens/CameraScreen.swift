import SwiftUI
import AVFoundation

// MARK: - CameraScreen

struct CameraScreen: View {
    var onDismiss: () -> Void
    var onMediaCaptured: (URL) -> Void
    var onSkip: (() -> Void)? = nil

    @StateObject private var camera = CameraManager()
    @State private var cameraPermission: AVAuthorizationStatus = AVCaptureDevice.authorizationStatus(for: .video)
    @State private var micPermission: AVAuthorizationStatus = AVCaptureDevice.authorizationStatus(for: .audio)
    @EnvironmentObject var lang: LanguageManager

    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()

            if cameraPermission == .authorized {
                CameraContentView(
                    camera: camera,
                    onDismiss: onDismiss,
                    onMediaCaptured: onMediaCaptured,
                    onSkip: onSkip
                )
            } else if cameraPermission == .denied || cameraPermission == .restricted {
                permissionDeniedView(permanent: true)
            } else {
                permissionDeniedView(permanent: false)
            }
        }
        .onAppear {
            requestPermissions()
        }
        .onDisappear {
            camera.stopSession()
        }
    }

    private func requestPermissions() {
        AVCaptureDevice.requestAccess(for: .video) { granted in
            DispatchQueue.main.async {
                cameraPermission = AVCaptureDevice.authorizationStatus(for: .video)
                if granted {
                    AVCaptureDevice.requestAccess(for: .audio) { _ in
                        DispatchQueue.main.async {
                            micPermission = AVCaptureDevice.authorizationStatus(for: .audio)
                            camera.startSession()
                        }
                    }
                }
            }
        }
    }

    private func permissionDeniedView(permanent: Bool) -> some View {
        ZStack {
            // Close button
            VStack {
                HStack {
                    Button(action: onDismiss) {
                        Image(systemName: "xmark")
                            .font(.system(size: 20, weight: .medium))
                            .foregroundColor(.white)
                            .frame(width: 44, height: 44)
                    }
                    .padding(.leading, 12)
                    .padding(.top, 4)
                    Spacer()
                }
                Spacer()
            }

            VStack(spacing: 16) {
                Text("📷")
                    .font(.system(size: 48))

                Text(permanent
                     ? lang.s("camera_permission_denied")
                     : lang.s("camera_permission_needed"))
                    .foregroundColor(.white)
                    .font(.system(size: 15))
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)

                Button(action: {
                    if permanent {
                        if let url = URL(string: UIApplication.openSettingsURLString) {
                            UIApplication.shared.open(url)
                        }
                    } else {
                        requestPermissions()
                    }
                }) {
                    Text(permanent ? lang.s("open_settings") : lang.s("allow_access"))
                        .font(.system(size: 15, weight: .medium))
                        .foregroundColor(.black)
                        .padding(.horizontal, 24)
                        .padding(.vertical, 10)
                        .background(Color.white)
                        .cornerRadius(8)
                }
            }
        }
    }
}

// MARK: - CameraContentView

private struct CameraContentView: View {
    @ObservedObject var camera: CameraManager
    var onDismiss: () -> Void
    var onMediaCaptured: (URL) -> Void
    var onSkip: (() -> Void)? = nil
    @EnvironmentObject var lang: LanguageManager

    var body: some View {
        ZStack {
            // Camera preview — edge-to-edge
            CameraPreviewView(session: camera.session)
                .ignoresSafeArea()

            // Top controls
            VStack {
                HStack {
                    Button(action: {
                        camera.stopRecording()
                        onDismiss()
                    }) {
                        Image(systemName: "xmark")
                            .font(.system(size: 20, weight: .medium))
                            .foregroundColor(.white)
                            .frame(width: 44, height: 44)
                    }

                    Spacer()

                    // Recording indicator
                    if camera.isRecording {
                        HStack(spacing: 6) {
                            Circle()
                                .fill(Color.red)
                                .frame(width: 10, height: 10)
                            Text(camera.formattedTime)
                                .foregroundColor(.white)
                                .font(.system(size: 14, weight: .medium))
                        }
                    }

                    Spacer()

                    // Flash toggle — only for back camera
                    if camera.currentPosition == .back {
                        Button(action: { camera.toggleFlash() }) {
                            Text(camera.flashIcon)
                                .font(.system(size: 18))
                                .frame(width: 44, height: 44)
                        }
                    } else {
                        Spacer().frame(width: 44)
                    }
                }
                .padding(.horizontal, 12)
                .padding(.top, 4)

                Spacer()

                // Hint text
                if !camera.isRecording {
                    Text(lang.s("camera_hint"))
                        .foregroundColor(.white.opacity(0.7))
                        .font(.system(size: 12))
                        .padding(.bottom, 4)
                }

                // Bottom controls
                HStack {
                    // Skip button
                    if let onSkip = onSkip {
                        Button(action: onSkip) {
                            Text(lang.s("skip_media"))
                                .font(.system(size: 14, weight: .medium))
                                .foregroundColor(.white.opacity(0.6))
                                .frame(width: 48, height: 48)
                        }
                    } else {
                        Spacer().frame(width: 48)
                    }

                    Spacer()

                    // Capture button
                    CaptureButton(
                        isRecording: camera.isRecording,
                        onTap: {
                            if camera.isRecording {
                                camera.stopRecording()
                            } else {
                                camera.takePhoto { url in
                                    onMediaCaptured(url)
                                }
                            }
                        },
                        onLongPress: {
                            camera.startRecording { url in
                                onMediaCaptured(url)
                            }
                        }
                    )

                    Spacer()

                    // Switch camera
                    Button(action: { camera.switchCamera() }) {
                        Text("🔄")
                            .font(.system(size: 24))
                            .frame(width: 48, height: 48)
                    }
                    .disabled(camera.isRecording)
                    .opacity(camera.isRecording ? 0.4 : 1.0)
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 24)
            }
        }
    }
}

// MARK: - CaptureButton

private struct CaptureButton: View {
    let isRecording: Bool
    let onTap: () -> Void
    let onLongPress: () -> Void

    @State private var isPressed = false

    var body: some View {
        ZStack {
            Circle()
                .stroke(isRecording ? Color.red : Color.white, lineWidth: 4)
                .frame(width: 72, height: 72)

            Circle()
                .fill(isRecording ? Color.red : Color.white)
                .frame(width: isRecording ? 32 : 60, height: isRecording ? 32 : 60)
        }
        .onTapGesture {
            onTap()
        }
        .onLongPressGesture(minimumDuration: 0.5) {
            onLongPress()
        }
    }
}

// MARK: - CameraPreviewView

struct CameraPreviewView: UIViewRepresentable {
    let session: AVCaptureSession

    func makeUIView(context: Context) -> UIView {
        let view = UIView(frame: .zero)
        let previewLayer = AVCaptureVideoPreviewLayer(session: session)
        previewLayer.videoGravity = .resizeAspectFill
        view.layer.addSublayer(previewLayer)
        context.coordinator.previewLayer = previewLayer
        return view
    }

    func updateUIView(_ uiView: UIView, context: Context) {
        DispatchQueue.main.async {
            context.coordinator.previewLayer?.frame = uiView.bounds
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    class Coordinator {
        var previewLayer: AVCaptureVideoPreviewLayer?
    }
}

// MARK: - CameraManager

class CameraManager: NSObject, ObservableObject {
    @Published var isRecording = false
    @Published var recordingSeconds = 0
    @Published var currentPosition: AVCaptureDevice.Position = .back
    @Published var flashModeState: AVCaptureDevice.FlashMode = .off

    let session = AVCaptureSession()
    private var photoOutput = AVCapturePhotoOutput()
    private var movieOutput = AVCaptureMovieFileOutput()
    private var currentDevice: AVCaptureDevice?
    private var photoCaptureCompletion: ((URL) -> Void)?
    private var videoCaptureCompletion: ((URL) -> Void)?
    private var timer: Timer?

    var flashIcon: String {
        switch flashModeState {
        case .on: return "⚡"
        case .auto: return "⚡A"
        default: return "⚡✕"
        }
    }

    var formattedTime: String {
        let m = recordingSeconds / 60
        let s = recordingSeconds % 60
        return String(format: "%d:%02d", m, s)
    }

    func startSession() {
        guard !session.isRunning else { return }

        session.beginConfiguration()
        session.sessionPreset = .high

        // Video input
        if let device = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .back) {
            currentDevice = device
            if let input = try? AVCaptureDeviceInput(device: device), session.canAddInput(input) {
                session.addInput(input)
            }
        }

        // Audio input
        if let audioDevice = AVCaptureDevice.default(for: .audio),
           let audioInput = try? AVCaptureDeviceInput(device: audioDevice),
           session.canAddInput(audioInput) {
            session.addInput(audioInput)
        }

        // Photo output
        if session.canAddOutput(photoOutput) {
            session.addOutput(photoOutput)
        }

        // Movie output
        if session.canAddOutput(movieOutput) {
            session.addOutput(movieOutput)
        }

        session.commitConfiguration()

        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            self?.session.startRunning()
        }
    }

    func stopSession() {
        stopRecording()
        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            self?.session.stopRunning()
        }
    }

    func switchCamera() {
        guard !isRecording else { return }

        let newPosition: AVCaptureDevice.Position = currentPosition == .back ? .front : .back

        session.beginConfiguration()

        // Remove current video input
        for input in session.inputs {
            if let deviceInput = input as? AVCaptureDeviceInput, deviceInput.device.hasMediaType(.video) {
                session.removeInput(deviceInput)
            }
        }

        // Add new input
        if let device = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: newPosition),
           let input = try? AVCaptureDeviceInput(device: device),
           session.canAddInput(input) {
            session.addInput(input)
            currentDevice = device
            currentPosition = newPosition
        }

        session.commitConfiguration()
    }

    func toggleFlash() {
        switch flashModeState {
        case .off: flashModeState = .on
        case .on: flashModeState = .auto
        default: flashModeState = .off
        }
    }

    func takePhoto(completion: @escaping (URL) -> Void) {
        guard photoOutput.connection(with: .video) != nil else {
            print("No active video connection for photo capture (simulator?)")
            return
        }
        photoCaptureCompletion = completion
        let settings = AVCapturePhotoSettings()
        if currentDevice?.hasFlash == true {
            settings.flashMode = flashModeState
        }
        photoOutput.capturePhoto(with: settings, delegate: self)
    }

    func startRecording(completion: @escaping (URL) -> Void) {
        guard !isRecording else { return }
        guard movieOutput.connection(with: .video) != nil else {
            print("No active video connection for recording (simulator?)")
            return
        }

        videoCaptureCompletion = completion
        let outputURL = URL(fileURLWithPath: NSTemporaryDirectory())
            .appendingPathComponent("video_\(Int(Date().timeIntervalSince1970)).mp4")

        // Enable torch for video if flash is on
        if flashModeState == .on, let device = currentDevice, device.hasTorch {
            try? device.lockForConfiguration()
            device.torchMode = .on
            device.unlockForConfiguration()
        }

        movieOutput.startRecording(to: outputURL, recordingDelegate: self)

        DispatchQueue.main.async {
            self.isRecording = true
            self.recordingSeconds = 0
            self.timer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { [weak self] _ in
                self?.recordingSeconds += 1
            }
        }
    }

    func stopRecording() {
        guard isRecording else { return }

        movieOutput.stopRecording()
        timer?.invalidate()
        timer = nil

        // Turn off torch
        if let device = currentDevice, device.hasTorch {
            try? device.lockForConfiguration()
            device.torchMode = .off
            device.unlockForConfiguration()
        }

        DispatchQueue.main.async {
            self.isRecording = false
            self.recordingSeconds = 0
        }
    }
}

// MARK: - AVCapturePhotoCaptureDelegate

extension CameraManager: AVCapturePhotoCaptureDelegate {
    func photoOutput(_ output: AVCapturePhotoOutput, didFinishProcessingPhoto photo: AVCapturePhoto, error: Error?) {
        guard error == nil, let data = photo.fileDataRepresentation() else {
            print("Photo capture error: \(error?.localizedDescription ?? "unknown")")
            return
        }

        let url = URL(fileURLWithPath: NSTemporaryDirectory())
            .appendingPathComponent("photo_\(Int(Date().timeIntervalSince1970)).jpg")

        do {
            try data.write(to: url)
            DispatchQueue.main.async {
                self.photoCaptureCompletion?(url)
                self.photoCaptureCompletion = nil
            }
        } catch {
            print("Failed to save photo: \(error)")
        }
    }
}

// MARK: - AVCaptureFileOutputRecordingDelegate

extension CameraManager: AVCaptureFileOutputRecordingDelegate {
    func fileOutput(_ output: AVCaptureFileOutput, didFinishRecordingTo outputFileURL: URL, from connections: [AVCaptureConnection], error: Error?) {
        if let error = error {
            print("Video recording error: \(error.localizedDescription)")
            return
        }

        DispatchQueue.main.async {
            self.videoCaptureCompletion?(outputFileURL)
            self.videoCaptureCompletion = nil
        }
    }
}
