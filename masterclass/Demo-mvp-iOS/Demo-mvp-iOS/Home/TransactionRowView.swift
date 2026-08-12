import UIKit

final class TransactionRowView: UIControl {

    private let onTap: () -> Void

    init(transaction: Transaction, onTap: @escaping () -> Void) {
        self.onTap = onTap
        super.init(frame: .zero)

        backgroundColor = transaction.rowBackgroundColor
        layer.cornerRadius = 16
        isAccessibilityElement = true
        accessibilityLabel = "\(transaction.title), \(transaction.category), \(transaction.amount)"
        accessibilityTraits = .button

        let badge = UIView()
        badge.backgroundColor = transaction.badgeColor
        badge.layer.cornerRadius = 20
        badge.isUserInteractionEnabled = false
        badge.translatesAutoresizingMaskIntoConstraints = false

        let icon = UIImageView(image: UIImage(systemName: transaction.iconSystemName))
        icon.tintColor = Palette.white
        icon.contentMode = .scaleAspectFit
        icon.translatesAutoresizingMaskIntoConstraints = false
        badge.addSubview(icon)

        let titleLabel = UILabel()
        titleLabel.text = transaction.title
        titleLabel.font = .boldSystemFont(ofSize: 14)
        titleLabel.textColor = Palette.textPrimary

        let categoryLabel = UILabel()
        categoryLabel.text = transaction.category
        categoryLabel.font = .systemFont(ofSize: 12)
        categoryLabel.textColor = Palette.textSecondary

        let titles = UIStackView(arrangedSubviews: [titleLabel, categoryLabel])
        titles.axis = .vertical
        titles.spacing = 2

        let amountLabel = UILabel()
        amountLabel.text = transaction.amount
        amountLabel.font = .boldSystemFont(ofSize: 14)
        amountLabel.textColor = Palette.textPrimary
        amountLabel.textAlignment = .right

        let timeLabel = UILabel()
        timeLabel.text = transaction.time
        timeLabel.font = .systemFont(ofSize: 12)
        timeLabel.textColor = Palette.textSecondary
        timeLabel.textAlignment = .right

        let amounts = UIStackView(arrangedSubviews: [amountLabel, timeLabel])
        amounts.axis = .vertical
        amounts.alignment = .trailing
        amounts.spacing = 2

        let row = UIStackView(arrangedSubviews: [badge, titles, UIView(), amounts])
        row.axis = .horizontal
        row.alignment = .center
        row.spacing = 12
        row.isUserInteractionEnabled = false
        row.translatesAutoresizingMaskIntoConstraints = false
        addSubview(row)

        NSLayoutConstraint.activate([
            badge.widthAnchor.constraint(equalToConstant: 40),
            badge.heightAnchor.constraint(equalToConstant: 40),
            icon.centerXAnchor.constraint(equalTo: badge.centerXAnchor),
            icon.centerYAnchor.constraint(equalTo: badge.centerYAnchor),
            icon.widthAnchor.constraint(equalToConstant: 18),
            icon.heightAnchor.constraint(equalToConstant: 18),

            row.topAnchor.constraint(equalTo: topAnchor, constant: 14),
            row.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 14),
            row.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -14),
            row.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -14),
        ])

        addAction(UIAction { [onTap] _ in onTap() }, for: .touchUpInside)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) { fatalError("init(coder:) is not supported") }

    override var isHighlighted: Bool {
        didSet { alpha = isHighlighted ? 0.7 : 1.0 }
    }
}
