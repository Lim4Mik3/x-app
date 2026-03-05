import SwiftUI

enum OverlayRoute: Hashable {
    case createPost
}

class OverlayNavigator: ObservableObject {
    @Published var stack: [OverlayRoute] = []

    func navigate(to route: OverlayRoute) {
        guard stack.last != route else { return }
        stack.append(route)
    }

    func pop() {
        guard !stack.isEmpty else { return }
        stack.removeLast()
    }

    func popToRoot() {
        stack.removeAll()
    }

    func contains(_ route: OverlayRoute) -> Bool {
        stack.contains(route)
    }
}
