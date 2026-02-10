//
//  ContentView.swift
//  MyFirstProject
//
//  Created by Gabriel Castillo Vizcarra on 21/01/26.
//

import SwiftUI


struct ContentView: View {
    
    @EnvironmentObject var settings: AppSettings

    var body: some View {
        Toggle("Modo oscuro", isOn: $settings.isDarkMode)
            .padding()
    }

    
}

#Preview {
    ContentView()
}
