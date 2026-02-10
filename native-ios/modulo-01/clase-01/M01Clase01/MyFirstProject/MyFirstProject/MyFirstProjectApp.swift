//
//  MyFirstProjectApp.swift
//  MyFirstProject
//
//  Created by Gabriel Castillo Vizcarra on 21/01/26.
//

import SwiftUI
import CoreData
import Combine

@main
struct MyFirstProjectApp: App {

    @StateObject private var settings = AppSettings()
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(settings)
        }
    }
}

class AppSettings: ObservableObject {
    @Published var isDarkMode = false
}
