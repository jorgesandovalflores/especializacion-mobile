import ComposableArchitecture
import SwiftUI

@main
struct Demo_tca_iOSApp: App {

    static let store = Store(initialState: HomeFeature.State()) {
        HomeFeature()
    }

    var body: some Scene {
        WindowGroup {
            HomeScreen(store: Self.store)
        }
    }
}
