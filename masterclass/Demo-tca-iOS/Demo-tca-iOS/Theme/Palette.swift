import SwiftUI

enum Palette {
    static let bg = Color(hex: 0xF9F9FB)
    static let textPrimary = Color(hex: 0x1A1A1E)
    static let textSecondary = Color(hex: 0x8B8B95)

    static let cardMint = Color(hex: 0xDCEFE6)
    static let cardPeach = Color(hex: 0xF6E3D3)
    static let cardLavender = Color(hex: 0xE5E1F5)

    static let rowPeach = Color(hex: 0xF8ECE2)
    static let rowMint = Color(hex: 0xE6F2EC)

    static let iconMint = Color(hex: 0x5FAE93)
    static let iconPeach = Color(hex: 0xD98B4F)
    static let iconLavender = Color(hex: 0x8E7FC7)

    static let badgeNike = Color(hex: 0x1A1A1A)
    static let badgePepsi = Color(hex: 0xE4002B)

    static let error = Color(hex: 0xD64545)
}

extension Color {

    init(hex: UInt32) {
        self.init(
            red: Double((hex >> 16) & 0xFF) / 255.0,
            green: Double((hex >> 8) & 0xFF) / 255.0,
            blue: Double(hex & 0xFF) / 255.0
        )
    }
}
