//
//  ProductsView.swift
//  Sesion2
//
//  Created by Gabriel Castillo Vizcarra on 28/01/26.
//

import SwiftUI

struct ProductsView: View {
    
    @Binding var path: [String]
    
    var body: some View {
        VStack {
            Text("Lista de productos")
            
            Button("Ver producto #1") {
                path.append("detalle")
            }
        }
    }
}

#Preview {
    ProductsView(path: .constant([]))
}
