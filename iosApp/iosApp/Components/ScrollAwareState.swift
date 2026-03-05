import SwiftUI
import UIKit

// MARK: - Scroll View Observer (KVO on UIScrollView.contentOffset)

struct ScrollViewObserver: UIViewRepresentable {
    let onOffsetChange: (CGFloat) -> Void

    func makeCoordinator() -> Coordinator {
        Coordinator(onOffsetChange: onOffsetChange)
    }

    func makeUIView(context: Context) -> UIView {
        let view = UIView(frame: .zero)
        view.isUserInteractionEnabled = false
        return view
    }

    func updateUIView(_ uiView: UIView, context: Context) {
        context.coordinator.onOffsetChange = onOffsetChange
        DispatchQueue.main.async {
            guard context.coordinator.scrollView == nil else { return }
            if let scrollView = Self.findScrollView(from: uiView) {
                context.coordinator.observe(scrollView)
            }
        }
    }

    private static func findScrollView(from view: UIView) -> UIScrollView? {
        var current: UIView? = view
        while let v = current {
            if let sv = v as? UIScrollView { return sv }
            current = v.superview
        }
        return nil
    }

    class Coordinator: NSObject {
        var onOffsetChange: (CGFloat) -> Void
        weak var scrollView: UIScrollView?
        private var observation: NSKeyValueObservation?

        init(onOffsetChange: @escaping (CGFloat) -> Void) {
            self.onOffsetChange = onOffsetChange
        }

        func observe(_ scrollView: UIScrollView) {
            self.scrollView = scrollView
            observation = scrollView.observe(\.contentOffset, options: [.new]) { [weak self] _, change in
                guard let newOffset = change.newValue else { return }
                self?.onOffsetChange(newOffset.y)
            }
        }

        deinit {
            observation?.invalidate()
        }
    }
}

// MARK: - Scroll Aware State

class ScrollAwareState: ObservableObject {
    // 0 = fully visible, headerHeight = fully hidden
    @Published var topBarOffset: CGFloat = 0
    // 0 = fully visible, bottomBarHeight = fully hidden
    @Published var bottomBarOffset: CGFloat = 0

    @Published var headerHeight: CGFloat = 0
    @Published var bottomBarHeight: CGFloat = 0

    var safeAreaTop: CGFloat = 0
    var safeAreaBottom: CGFloat = 0

    private var lastOffset: CGFloat?
    private var directionBuffer: CGFloat = 0
    private var lastDirectionDown: Bool?
    private var thresholdCleared = false
    private let threshold: CGFloat = 200

    func onScrollOffsetChanged(_ offset: CGFloat) {
        // Ignore bounce at top (negative contentOffset)
        guard offset >= 0 else {
            lastOffset = nil
            return
        }

        guard let last = lastOffset else {
            lastOffset = offset
            return
        }

        let delta = offset - last // positive = scrolling down, negative = scrolling up
        guard abs(delta) > 0.5 else { return }
        lastOffset = offset

        let isDown = delta > 0

        // Max offset includes safe area so bars fully slide off screen
        let maxTop = headerHeight + safeAreaTop
        let maxBottom = bottomBarHeight + safeAreaBottom

        // Direction changed → reset dead zone
        if let wasDown = lastDirectionDown, wasDown != isDown {
            directionBuffer = 0
            // Skip dead zone if bars are mid-transition (not fully visible or fully hidden)
            let topAtRest = topBarOffset == 0 || topBarOffset >= maxTop
            let bottomAtRest = bottomBarOffset == 0 || bottomBarOffset >= maxBottom
            thresholdCleared = !(topAtRest && bottomAtRest)
        }
        lastDirectionDown = isDown

        if !thresholdCleared {
            directionBuffer += delta
            if abs(directionBuffer) < threshold { return }
            // Threshold just crossed — use only the excess
            thresholdCleared = true
            let excess = abs(directionBuffer) - threshold
            let effectiveDelta = isDown ? excess : -excess
            topBarOffset = min(maxTop, max(0, topBarOffset + effectiveDelta))
            bottomBarOffset = min(maxBottom, max(0, bottomBarOffset + effectiveDelta))
        } else {
            topBarOffset = min(maxTop, max(0, topBarOffset + delta))
            bottomBarOffset = min(maxBottom, max(0, bottomBarOffset + delta))
        }
    }

    func reset() {
        topBarOffset = 0
        bottomBarOffset = 0
        lastOffset = nil
        directionBuffer = 0
        lastDirectionDown = nil
        thresholdCleared = false
    }
}
