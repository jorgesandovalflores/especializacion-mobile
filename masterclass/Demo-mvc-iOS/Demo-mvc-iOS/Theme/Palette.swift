import UIKit

enum Palette {
    static let bg = UIColor(hex: 0xF9F9FB)
    static let textPrimary = UIColor(hex: 0x1A1A1E)
    static let textSecondary = UIColor(hex: 0x8B8B95)

    static let cardMint = UIColor(hex: 0xDCEFE6)
    static let cardPeach = UIColor(hex: 0xF6E3D3)
    static let cardLavender = UIColor(hex: 0xE5E1F5)

    static let rowPeach = UIColor(hex: 0xF8ECE2)
    static let rowMint = UIColor(hex: 0xE6F2EC)

    static let iconMint = UIColor(hex: 0x5FAE93)
    static let iconPeach = UIColor(hex: 0xD98B4F)
    static let iconLavender = UIColor(hex: 0x8E7FC7)

    static let badgeNike = UIColor(hex: 0x1A1A1A)
    static let badgePepsi = UIColor(hex: 0xE4002B)

    static let error = UIColor(hex: 0xD64545)
    static let white = UIColor.white
}

extension UIColor {

    convenience init(hex: UInt32) {
        self.init(
            red: CGFloat((hex >> 16) & 0xFF) / 255.0,
            green: CGFloat((hex >> 8) & 0xFF) / 255.0,
            blue: CGFloat(hex & 0xFF) / 255.0,
            alpha: 1.0
        )
    }
}
