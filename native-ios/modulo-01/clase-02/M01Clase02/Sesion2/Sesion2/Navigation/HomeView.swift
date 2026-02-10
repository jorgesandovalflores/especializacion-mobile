//
//  HomeView.swift
//  Sesion2
//
//  Created by Gabriel Castillo Vizcarra on 28/01/26.
//

import SwiftUI

struct HomeView: View {
    
    @State private var path: [String] = []
    
    var body: some View {
        
        NavigationStack(path: $path) {
            VStack(spacing: 20) {
//                ProfileHeaderView()
//                LogoutButtonView()
                Text("Home")
                Button("Ver Productos") {
                    path.append("productos")
                }
            }
            .navigationDestination(for: String.self) { value in
                if value == "productos" {
                    ProductsView(path: $path)
                } else if value == "detalle" {
                    ProductDetailView()
                }
            }
        }
    }
}

#Preview {
    HomeView()
}
