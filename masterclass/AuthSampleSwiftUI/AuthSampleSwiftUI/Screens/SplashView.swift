//
//  SplashView.swift
//  AuthSampleSwiftUI
//
//  Created by Jorge Sandoval Flores on 12/08/25.
//

import SwiftUI

struct SplashView: View {
    // Comentario: Navegación por estado
    @State private var goToLogin = false

    var body: some View {
        Group {
            if goToLogin {
                LoginView()
            } else {
                ZStack {
                    AppColors.background.ignoresSafeArea()
                    VStack(spacing: 8) {
                        // Comentario: Placeholder de logo
                        Rectangle()
                            .fill(Color.clear)
                            .frame(width: 72, height: 72)
                        Text("Ruti")
                            .font(.system(size: 28, weight: .semibold))
                            .foregroundColor(AppColors.onBackground)
                        Text("Move smarter, move safe.")
                            .font(.body)
                            .foregroundColor(AppColors.onBackground.opacity(0.7))
                    }
                }
                .onAppear {
                    // Comentario: Retraso de 1.5s antes de ir a Login
                    DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
                        withAnimation { goToLogin = true }
                    }
                }
            }
        }
    }
}
