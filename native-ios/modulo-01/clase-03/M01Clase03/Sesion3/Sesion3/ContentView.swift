//
//  ContentView.swift
//  Sesion3
//
//  Created by Gabriel Castillo Vizcarra on 2/02/26.
//

import SwiftUI

struct ContentView: View {
    var body: some View {
        NavigationStack {
            VStack {
                Text("Hola en SwiftUI")
                CustomLabel(text: "Hola en UIKit")
                    .frame(height: 50)
                NavigationLink("Ir al ViewController") {
                    DemoViewControllerRepresentable(title: "Titulo de mi viewcontroller en UIKit")
                }
                NavigationLink("Ir a TextFieldExampleView") {
                    TextFieldExampleView()
                }
            }
        }
    }
}

struct DemoViewControllerRepresentable: UIViewControllerRepresentable {
    
    let title : String
    
    func makeUIViewController(context: Context) -> DemoViewController {
        return DemoViewController()
    }
    
    func updateUIViewController(_ uiViewController: DemoViewController, context: Context) {
        uiViewController.activityIndicator.startAnimating()
        uiViewController.lblTitle.text = title
    }
}

struct CustomLabel: UIViewRepresentable {
    
    let text: String
    
    func makeUIView(context: Context) -> UILabel {
        let label = UILabel()
        label.textAlignment = .center
        label.font = UIFont.systemFont(ofSize: 20, weight: .bold)
        label.textColor = .blue
        return label
    }
    
    func updateUIView(_ uiView: UILabel, context: Context) {
        uiView.text = text
    }
    
}

#Preview {
    ContentView()
}
