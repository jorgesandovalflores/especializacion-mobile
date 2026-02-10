//
//  ProfileHeaderView.swift
//  Sesion2
//
//  Created by Gabriel Castillo Vizcarra on 28/01/26.
//

import SwiftUI

struct ProfileHeaderView: View {
    var body: some View {
        VStack {
            Text("Perfil")
            Image(systemName: "person")
            Text("Gabriel")
        }
    }
}

#Preview {
    ProfileHeaderView()
}
