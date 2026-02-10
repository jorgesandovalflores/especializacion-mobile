//
//  Sesion3App.swift
//  Sesion3
//
//  Created by Gabriel Castillo Vizcarra on 2/02/26.
//

import SwiftUI

@main
struct Sesion3App: App {
    @Environment(\.scenePhase) var scenePhase
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
        .onChange(of: scenePhase) { oldValue, newValue in
            switch newValue {
            case .active:
                print("🟢 App activa")
            case .inactive:
                print("🟡 App inactiva")
            case .background:
                print("🔵 App en background")
            @unknown default:
                break
            }
        }
    }
}
