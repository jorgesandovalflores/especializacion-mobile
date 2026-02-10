//
//  StateView.swift
//  Sesion2
//
//  Created by Gabriel Castillo Vizcarra on 28/01/26.
//

import SwiftUI

struct StateView: View {
    
    @State private var value = 0
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 10) {
                Text("El valor es \(value)")
                Button("Sumar 1") {
                    value += 1
                }
                NavigationLink("Ir a BindingView") {
                    BindingView(value: $value)
                }
            }
            .navigationTitle("StateView")
        }
    }
}

#Preview {
    StateView()
}
