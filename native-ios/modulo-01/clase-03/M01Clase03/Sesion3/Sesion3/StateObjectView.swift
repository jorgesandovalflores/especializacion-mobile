//
//  StateObjectView.swift
//  Sesion3
//
//  Created by Gabriel Castillo Vizcarra on 2/02/26.
//

import SwiftUI
import Combine

class CounterViewModel: ObservableObject {
    
    @Published var value: Int = 0
    
    func increment() {
        value += 1
    }
}

struct StateObjectView: View {
    
    @ObservedObject private var viewModel = CounterViewModel()
    
    var body: some View {
        VStack(spacing: 20) {
            Text("Contador: \(viewModel.value)")
                .font(.title)
            Button("Incrementar") {
                viewModel.increment()
            }
        }
    }
}

#Preview {
    StateObjectView()
}
