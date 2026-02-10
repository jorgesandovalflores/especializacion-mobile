//
//  BindingView.swift
//  Sesion2
//
//  Created by Gabriel Castillo Vizcarra on 28/01/26.
//

import SwiftUI

struct BindingView: View {
    
    @Binding var value: Int
    
    var body: some View {
        VStack {
            Button("Sumar 2") {
                value += 2
            }
        }
    }
}

#Preview {
    BindingView(value: .constant(0))
}
