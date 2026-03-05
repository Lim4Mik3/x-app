import SwiftUI
import MapKit
import CoreLocation

// MARK: - Tappable MKMapView wrapper

private struct TappableMapView: UIViewRepresentable {
    @Binding var pinCoordinate: CLLocationCoordinate2D
    @Binding var region: MKCoordinateRegion
    var accentColor: Color
    var showsUserLocation: Bool
    var centerOnUser: Int

    func makeUIView(context: Context) -> MKMapView {
        let mapView = MKMapView()
        mapView.delegate = context.coordinator
        mapView.setRegion(region, animated: false)
        mapView.showsUserLocation = showsUserLocation

        let tap = UITapGestureRecognizer(target: context.coordinator, action: #selector(Coordinator.handleTap(_:)))
        mapView.addGestureRecognizer(tap)

        let annotation = MKPointAnnotation()
        annotation.coordinate = pinCoordinate
        mapView.addAnnotation(annotation)

        return mapView
    }

    func updateUIView(_ mapView: MKMapView, context: Context) {
        mapView.showsUserLocation = showsUserLocation

        if let existing = mapView.annotations.first(where: { $0 is MKPointAnnotation }) as? MKPointAnnotation {
            if existing.coordinate.latitude != pinCoordinate.latitude ||
               existing.coordinate.longitude != pinCoordinate.longitude {
                existing.coordinate = pinCoordinate
            }
        }

        if centerOnUser != context.coordinator.lastCenterOnUser {
            context.coordinator.lastCenterOnUser = centerOnUser
            if let userLocation = mapView.userLocation.location {
                let userRegion = MKCoordinateRegion(
                    center: userLocation.coordinate,
                    span: MKCoordinateSpan(latitudeDelta: 0.002, longitudeDelta: 0.002)
                )
                mapView.setRegion(userRegion, animated: true)
            }
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    class Coordinator: NSObject, MKMapViewDelegate {
        var parent: TappableMapView
        var lastCenterOnUser: Int = 0

        init(_ parent: TappableMapView) {
            self.parent = parent
        }

        @objc func handleTap(_ gesture: UITapGestureRecognizer) {
            guard let mapView = gesture.view as? MKMapView else { return }
            let point = gesture.location(in: mapView)
            let coordinate = mapView.convert(point, toCoordinateFrom: mapView)
            parent.pinCoordinate = coordinate
        }

        func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
            guard !(annotation is MKUserLocation) else { return nil }
            let identifier = "pin"
            var view = mapView.dequeueReusableAnnotationView(withIdentifier: identifier) as? MKMarkerAnnotationView
            if view == nil {
                view = MKMarkerAnnotationView(annotation: annotation, reuseIdentifier: identifier)
            } else {
                view?.annotation = annotation
            }
            view?.markerTintColor = UIColor(parent.accentColor)
            return view
        }

        func mapViewDidChangeVisibleRegion(_ mapView: MKMapView) {
            parent.region = mapView.region
        }
    }
}

// MARK: - MapScreen

struct MapScreen: View {
    var onDismiss: () -> Void
    var onLocationConfirmed: () -> Void

    @State private var pinCoordinate = CLLocationCoordinate2D(latitude: -23.5325, longitude: -46.7917)
    @State private var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: -23.5325, longitude: -46.7917),
        span: MKCoordinateSpan(latitudeDelta: 0.002, longitudeDelta: 0.002)
    )
    @State private var centerOnUserTrigger = 0
    @ObservedObject private var locationService = LocationService.shared
    @Environment(\.appColors) private var colors
    @Environment(\.appSpacing) private var spacing
    @EnvironmentObject var lang: LanguageManager

    private var distanceText: String? {
        guard let userLoc = locationService.location else { return nil }
        let pinCoord = CLLocationCoordinate2D(latitude: pinCoordinate.latitude, longitude: pinCoordinate.longitude)
        let meters = LocationService.distanceBetween(from: userLoc.coordinate, to: pinCoord)
        return LocationService.formatDistance(meters)
    }

    var body: some View {
        ZStack {
            // Fullscreen map — tap to move pin
            TappableMapView(
                pinCoordinate: $pinCoordinate,
                region: $region,
                accentColor: colors.accent,
                showsUserLocation: locationService.authorized,
                centerOnUser: centerOnUserTrigger
            )
            .ignoresSafeArea()

            // Header
            VStack(spacing: 0) {
                VStack(spacing: 0) {
                    HStack {
                        Button(action: onDismiss) {
                            Image(systemName: "arrow.left")
                                .font(.system(size: spacing.iconSmall, weight: .medium))
                                .foregroundColor(colors.textPrimary)
                                .frame(width: spacing.iconButton, height: spacing.iconButton)
                        }
                        .buttonStyle(.plain)

                        Spacer()

                        VStack(spacing: spacing.xxs) {
                            Text(lang.s("confirm_location"))
                                .font(.system(size: 17, weight: .semibold))
                                .foregroundColor(colors.textPrimary)
                                .tracking(0.2)
                            Text(lang.s("move_map_to_select"))
                                .font(.system(size: 13))
                                .foregroundColor(colors.textSecondary)
                                .tracking(0.2)
                        }

                        Spacer()

                        Color.clear
                            .frame(width: spacing.iconButton, height: spacing.iconButton)
                    }
                    .padding(.horizontal, spacing.xxxl)
                    .padding(.vertical, spacing.xl)

                    Rectangle()
                        .fill(colors.divider)
                        .frame(height: spacing.divider)
                }
                .background(colors.background)

                // Floating pill below header
                Text("Osasco, SP")
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(colors.textPrimary)
                    .tracking(0.2)
                    .padding(.horizontal, spacing.xxl)
                    .padding(.vertical, spacing.md)
                    .background(colors.surface)
                    .clipShape(Capsule())
                    .overlay(
                        Capsule()
                            .stroke(colors.divider, lineWidth: spacing.divider)
                    )
                    .shadow(color: .black.opacity(0.1), radius: 4, y: 2)
                    .padding(.top, spacing.lg)

                Spacer()
            }

            // FAB: center on user location
            VStack {
                Spacer()
                HStack {
                    Spacer()
                    Button(action: {
                        locationService.fetch(forceRefresh: true)
                        centerOnUserTrigger += 1
                    }) {
                        Image(systemName: "paperplane.fill")
                            .font(.system(size: spacing.iconSmall, weight: .medium))
                            .foregroundColor(colors.accent)
                            .frame(width: 48, height: 48)
                            .background(colors.surface)
                            .clipShape(Circle())
                            .shadow(color: .black.opacity(0.15), radius: 4, y: 2)
                    }
                    .buttonStyle(.plain)
                    .padding(.trailing, spacing.xxl)
                    .padding(.bottom, 220)
                }
            }

            // Bottom overlay: distance pill + card
            VStack(spacing: spacing.md) {
                Spacer()

                // Distance pill
                if let distance = distanceText {
                    Text(distance)
                        .font(.system(size: 13, weight: .medium))
                        .foregroundColor(colors.textSecondary)
                        .tracking(0.2)
                        .padding(.horizontal, spacing.xxl)
                        .padding(.vertical, spacing.sm)
                        .background(colors.surface)
                        .clipShape(Capsule())
                        .overlay(
                            Capsule()
                                .stroke(colors.divider, lineWidth: spacing.divider)
                        )
                }

                // Card with confirm button
                VStack(spacing: spacing.xxl) {
                    VStack(spacing: spacing.sm) {
                        Text(lang.s("location_precision_title"))
                            .font(.system(size: 15, weight: .semibold))
                            .foregroundColor(colors.textPrimary)
                            .multilineTextAlignment(.center)
                        Text(lang.s("location_precision_hint"))
                            .font(.system(size: 13))
                            .foregroundColor(colors.textSecondary)
                            .lineSpacing(4)
                            .multilineTextAlignment(.center)
                    }

                    Button(action: onLocationConfirmed) {
                        Text(lang.s("confirm_location"))
                            .font(.system(size: 15, weight: .semibold))
                            .frame(maxWidth: .infinity)
                            .frame(height: 48)
                            .background(colors.accent)
                            .foregroundColor(colors.onAccent)
                            .clipShape(RoundedRectangle(cornerRadius: spacing.lg))
                    }
                    .buttonStyle(.plain)
                }
                .padding(spacing.xxxl)
                .background(colors.surface)
                .clipShape(RoundedRectangle(cornerRadius: spacing.xxl))
                .shadow(color: .black.opacity(0.1), radius: 8, y: -2)
                .padding(.horizontal, spacing.xxl)
                .padding(.bottom, 40)
            }
        }
        .background(colors.background)
        .onAppear {
            locationService.requestPermission()
        }
    }
}

#Preview {
    MapScreen(onDismiss: {}, onLocationConfirmed: {})
}
