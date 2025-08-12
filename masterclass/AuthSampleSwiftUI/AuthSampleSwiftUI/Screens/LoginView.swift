//
//  LoginView.swift
//  AuthSampleSwiftUI
//
//  Created by Jorge Sandoval Flores on 12/08/25.
//

import SwiftUI

struct LoginView: View {
    // Comentario: Estado para campos
    @State private var email: String = ""
    @State private var password: String = ""
    @State private var isSecure: Bool = true

    var body: some View {
        ZStack {
            AppColors.background.ignoresSafeArea()
            VStack(spacing: 0) {
                Spacer()
                VStack(alignment: .center, spacing: 8) {
                    Text("Welcome back")
                        .font(.system(size: 28, weight: .semibold))
                        .foregroundColor(AppColors.onBackground)
                    Text("Sign in to continue")
                        .font(.body)
                        .foregroundColor(AppColors.onBackground.opacity(0.7))
                }
                .padding(.bottom, 24)

                VStack(spacing: 12) {
                    // Email
                    TextField("Email", text: $email)
                        .padding()
                        .background(AppColors.fieldBg)
                        .cornerRadius(12)

                    // Password
                    Group {
                        if isSecure {
                            SecureField("Password", text: $password)
                        } else {
                            TextField("Password", text: $password)
                        }
                    }
                    .padding()
                    .background(AppColors.fieldBg)
                    .cornerRadius(12)
                }

                HStack {
                    Spacer()
                    Button("Forgot password?") {
                        // TODO
                    }
                    .foregroundColor(AppColors.primary)
                }
                .padding(.top, 8)

                Button(action: {
                    // TODO: sign in
                }) {
                    Text("Sign in")
                        .frame(maxWidth: .infinity, minHeight: 48)
                        .foregroundColor(AppColors.onPrimary)
                        .background(AppColors.primary)
                        .cornerRadius(12)
                }
                .padding(.top, 16)

                Button(action: {
                    // TODO: create account
                }) {
                    Text("Create account")
                        .foregroundColor(AppColors.primary)
                        .frame(maxWidth: .infinity, minHeight: 44)
                }
                .padding(.top, 12)

                Spacer()
            }
            .padding(24)
        }
    }
}
