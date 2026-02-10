//
//  Sesion2App.swift
//  Sesion2
//
//  Created by Gabriel Castillo Vizcarra on 28/01/26.
//

import SwiftUI
import CoreData

@main
struct Sesion2App: App {
    let persistenceController = PersistenceController.shared

    var body: some Scene {
        WindowGroup {
//            ContentView()
//                .environment(\.managedObjectContext, persistenceController.container.viewContext)
            HomeView()
        }
    }
}
