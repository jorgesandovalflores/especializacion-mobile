//
//  TextFieldExampleView.swift
//  Sesion3
//
//  Created by Gabriel Castillo Vizcarra on 2/02/26.
//

import SwiftUI

struct TextFieldExampleView: View {
    
    @State private var name = ""
    
    var body: some View {
        VStack(spacing: 20) {
            Text("Nombre:")
            CustomTextField(text: $name, placeholder: "Ingresar el nombre")
                .frame(height: 50)
            Text("Escribiste: \(name)")
        }
        .padding()
    }
    
}

struct CustomTextField: UIViewRepresentable {

    @Binding var text: String
    var placeholder: String

    func makeUIView(context: Context) -> UITextField {
        let textField = UITextField()
        textField.placeholder = placeholder
        textField.borderStyle = .roundedRect
        textField.delegate = context.coordinator
        return textField
    }

    func updateUIView(_ uiView: UITextField, context: Context) {
        uiView.text = text
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    // MARK: - Coordinator
    class Coordinator: NSObject, UITextFieldDelegate {

        var parent: CustomTextField

        init(_ parent: CustomTextField) {
            self.parent = parent
        }

        func textFieldDidChangeSelection(_ textField: UITextField) {
            parent.text = textField.text ?? ""
        }
        
        func textFieldShouldReturn(_ textField: UITextField) -> Bool {
            textField.resignFirstResponder()
        }
    }
}

#Preview {
    TextFieldExampleView()
}
