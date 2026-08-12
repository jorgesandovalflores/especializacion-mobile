import UIKit

final class HomeViewImpl: UIView, HomeView {

    var rootView: UIView { self }

    private weak var listener: HomeViewListener?

    private let greetingLabel = UILabel()
    private let salesValueLabel = UILabel()
    private let revenueValueLabel = UILabel()

    private var barHeightConstraints: [NSLayoutConstraint] = []
    private var barLabels: [UILabel] = []

    private let transactionsStack = UIStackView()
    private let loadingOverlay = UIView()
    private let errorOverlay = UIView()
    private let errorMessageLabel = UILabel()

    private let maxBarHeight: CGFloat = 64

    init() {
        super.init(frame: .zero)
        backgroundColor = Palette.bg
        buildHierarchy()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) { fatalError("init(coder:) is not supported") }

    func setListener(_ listener: HomeViewListener) {
        self.listener = listener
    }

    func showLoading() {
        loadingOverlay.isHidden = false
        errorOverlay.isHidden = true
    }

    func showData(_ data: HomeUiData) {
        loadingOverlay.isHidden = true
        errorOverlay.isHidden = true

        greetingLabel.text = "Hola \(data.userName)"
        salesValueLabel.text = data.salesLastWeek
        revenueValueLabel.text = data.revenueLastWeek

        renderActivityChart(values: data.activityValues, labels: data.activityLabels)

        transactionsStack.arrangedSubviews.forEach { $0.removeFromSuperview() }
        for transaction in data.transactions {
            let row = TransactionRowView(transaction: transaction) { [weak self] in
                self?.listener?.onTransactionClicked(transaction.id)
            }
            transactionsStack.addArrangedSubview(row)
        }
    }

    func showError(_ message: String) {
        loadingOverlay.isHidden = true
        errorOverlay.isHidden = false
        errorMessageLabel.text = message
    }

    private func renderActivityChart(values: [Int], labels: [String]) {
        let maxValue = CGFloat(max(values.max() ?? 1, 1))

        for (index, value) in values.enumerated() where index < barHeightConstraints.count {
            let height = max(CGFloat(value) / maxValue * maxBarHeight, 12)
            barHeightConstraints[index].constant = height
        }
        for (index, label) in labels.enumerated() where index < barLabels.count {
            barLabels[index].text = label
        }
        layoutIfNeeded()
    }

    private func buildHierarchy() {
        let scrollView = UIScrollView()
        scrollView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(scrollView)

        let content = UIStackView()
        content.axis = .vertical
        content.translatesAutoresizingMaskIntoConstraints = false
        scrollView.addSubview(content)

        let tabBar = buildTabBar()

        NSLayoutConstraint.activate([
            scrollView.topAnchor.constraint(equalTo: safeAreaLayoutGuide.topAnchor),
            scrollView.leadingAnchor.constraint(equalTo: leadingAnchor),
            scrollView.trailingAnchor.constraint(equalTo: trailingAnchor),
            scrollView.bottomAnchor.constraint(equalTo: tabBar.topAnchor),

            content.topAnchor.constraint(equalTo: scrollView.contentLayoutGuide.topAnchor, constant: 16),
            content.leadingAnchor.constraint(equalTo: scrollView.contentLayoutGuide.leadingAnchor, constant: 20),
            content.trailingAnchor.constraint(equalTo: scrollView.contentLayoutGuide.trailingAnchor, constant: -20),
            content.bottomAnchor.constraint(equalTo: scrollView.contentLayoutGuide.bottomAnchor, constant: -24),
            content.widthAnchor.constraint(equalTo: scrollView.frameLayoutGuide.widthAnchor, constant: -40),
        ])

        let header = buildHeader()
        content.addArrangedSubview(header)
        content.setCustomSpacing(24, after: header)

        let cardsRow = buildCardsRow()
        content.addArrangedSubview(cardsRow)
        content.setCustomSpacing(28, after: cardsRow)

        content.addArrangedSubview(buildTransactionsSection())

        buildLoadingOverlay(bottomAnchorView: tabBar)
        buildErrorOverlay(bottomAnchorView: tabBar)
    }

    private func buildHeader() -> UIView {
        let avatar = UIView()
        avatar.backgroundColor = Palette.cardPeach
        avatar.layer.cornerRadius = 22
        avatar.translatesAutoresizingMaskIntoConstraints = false
        avatar.isAccessibilityElement = true
        avatar.accessibilityLabel = "Foto de perfil"

        let avatarIcon = UIImageView(image: UIImage(systemName: "person.fill"))
        avatarIcon.tintColor = Palette.iconPeach
        avatarIcon.contentMode = .scaleAspectFit
        avatarIcon.translatesAutoresizingMaskIntoConstraints = false
        avatar.addSubview(avatarIcon)

        greetingLabel.font = .boldSystemFont(ofSize: 17)
        greetingLabel.textColor = Palette.textPrimary

        let welcomeLabel = UILabel()
        welcomeLabel.text = "¡Bienvenido de nuevo!"
        welcomeLabel.font = .systemFont(ofSize: 13)
        welcomeLabel.textColor = Palette.textSecondary

        let titles = UIStackView(arrangedSubviews: [greetingLabel, welcomeLabel])
        titles.axis = .vertical
        titles.spacing = 2

        let menuButton = UIView()
        menuButton.backgroundColor = Palette.white
        menuButton.layer.cornerRadius = 14
        menuButton.layer.shadowColor = UIColor.black.cgColor
        menuButton.layer.shadowOpacity = 0.06
        menuButton.layer.shadowRadius = 4
        menuButton.layer.shadowOffset = CGSize(width: 0, height: 2)
        menuButton.translatesAutoresizingMaskIntoConstraints = false
        menuButton.isAccessibilityElement = true
        menuButton.accessibilityLabel = "Menú"

        let menuIcon = UIImageView(image: UIImage(systemName: "square.grid.2x2.fill"))
        menuIcon.tintColor = Palette.textPrimary
        menuIcon.contentMode = .scaleAspectFit
        menuIcon.translatesAutoresizingMaskIntoConstraints = false
        menuButton.addSubview(menuIcon)

        let header = UIStackView(arrangedSubviews: [avatar, titles, UIView(), menuButton])
        header.axis = .horizontal
        header.alignment = .center
        header.spacing = 12

        NSLayoutConstraint.activate([
            avatar.widthAnchor.constraint(equalToConstant: 44),
            avatar.heightAnchor.constraint(equalToConstant: 44),
            avatarIcon.centerXAnchor.constraint(equalTo: avatar.centerXAnchor),
            avatarIcon.centerYAnchor.constraint(equalTo: avatar.centerYAnchor),
            avatarIcon.widthAnchor.constraint(equalToConstant: 22),
            avatarIcon.heightAnchor.constraint(equalToConstant: 22),

            menuButton.widthAnchor.constraint(equalToConstant: 44),
            menuButton.heightAnchor.constraint(equalToConstant: 44),
            menuIcon.centerXAnchor.constraint(equalTo: menuButton.centerXAnchor),
            menuIcon.centerYAnchor.constraint(equalTo: menuButton.centerYAnchor),
            menuIcon.widthAnchor.constraint(equalToConstant: 20),
            menuIcon.heightAnchor.constraint(equalToConstant: 20),
        ])
        return header
    }

    private func buildCardsRow() -> UIView {
        let activityCard = buildActivityCard()

        let salesCard = buildStatCard(
            title: "Ventas la semana pasada",
            valueLabel: salesValueLabel,
            background: Palette.cardPeach,
            iconSystemName: "tag.fill",
            iconTint: Palette.iconPeach
        )
        let revenueCard = buildStatCard(
            title: "Ingresos la semana pasada",
            valueLabel: revenueValueLabel,
            background: Palette.cardLavender,
            iconSystemName: "chart.pie.fill",
            iconTint: Palette.iconLavender
        )

        let statsColumn = UIStackView(arrangedSubviews: [salesCard, revenueCard])
        statsColumn.axis = .vertical
        statsColumn.spacing = 12

        let row = UIStackView(arrangedSubviews: [activityCard, statsColumn])
        row.axis = .horizontal
        row.alignment = .top
        row.spacing = 12

        activityCard.widthAnchor.constraint(equalTo: row.widthAnchor, multiplier: 0.6, constant: -7).isActive = true
        return row
    }

    private func buildActivityCard() -> UIView {
        let card = UIView()
        card.backgroundColor = Palette.cardMint
        card.layer.cornerRadius = 20

        let chart = UIStackView()
        chart.axis = .horizontal
        chart.distribution = .fillEqually
        chart.translatesAutoresizingMaskIntoConstraints = false

        for _ in 0..<5 {
            let bar = UIView()
            bar.backgroundColor = Palette.iconMint
            bar.layer.cornerRadius = 4
            bar.layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
            bar.translatesAutoresizingMaskIntoConstraints = false

            let label = UILabel()
            label.font = .systemFont(ofSize: 10)
            label.textColor = Palette.textSecondary
            label.translatesAutoresizingMaskIntoConstraints = false
            barLabels.append(label)

            let column = UIView()
            column.addSubview(bar)
            column.addSubview(label)

            let barHeight = bar.heightAnchor.constraint(equalToConstant: 18)
            barHeightConstraints.append(barHeight)

            NSLayoutConstraint.activate([
                label.bottomAnchor.constraint(equalTo: column.bottomAnchor),
                label.centerXAnchor.constraint(equalTo: column.centerXAnchor),
                bar.bottomAnchor.constraint(equalTo: label.topAnchor, constant: -6),
                bar.centerXAnchor.constraint(equalTo: column.centerXAnchor),
                bar.widthAnchor.constraint(equalToConstant: 16),
                barHeight,
            ])
            chart.addArrangedSubview(column)
        }

        let titleLabel = UILabel()
        titleLabel.text = "Actividad"
        titleLabel.font = .boldSystemFont(ofSize: 16)
        titleLabel.textColor = Palette.textPrimary

        let subtitleLabel = UILabel()
        subtitleLabel.text = "de esta semana"
        subtitleLabel.font = .systemFont(ofSize: 12)
        subtitleLabel.textColor = Palette.textSecondary

        let titles = UIStackView(arrangedSubviews: [titleLabel, subtitleLabel])
        titles.axis = .vertical
        titles.spacing = 2
        titles.translatesAutoresizingMaskIntoConstraints = false

        card.addSubview(chart)
        card.addSubview(titles)

        NSLayoutConstraint.activate([
            chart.topAnchor.constraint(equalTo: card.topAnchor, constant: 18),
            chart.leadingAnchor.constraint(equalTo: card.leadingAnchor, constant: 18),
            chart.trailingAnchor.constraint(equalTo: card.trailingAnchor, constant: -18),
            chart.heightAnchor.constraint(equalToConstant: 90),

            titles.topAnchor.constraint(equalTo: chart.bottomAnchor, constant: 16),
            titles.leadingAnchor.constraint(equalTo: card.leadingAnchor, constant: 18),
            titles.trailingAnchor.constraint(lessThanOrEqualTo: card.trailingAnchor, constant: -18),
            titles.bottomAnchor.constraint(equalTo: card.bottomAnchor, constant: -18),
        ])
        return card
    }

    private func buildStatCard(
        title: String,
        valueLabel: UILabel,
        background: UIColor,
        iconSystemName: String,
        iconTint: UIColor
    ) -> UIView {
        let card = UIView()
        card.backgroundColor = background
        card.layer.cornerRadius = 20

        let badge = UIView()
        badge.backgroundColor = Palette.white
        badge.layer.cornerRadius = 10
        badge.translatesAutoresizingMaskIntoConstraints = false

        let icon = UIImageView(image: UIImage(systemName: iconSystemName))
        icon.tintColor = iconTint
        icon.contentMode = .scaleAspectFit
        icon.translatesAutoresizingMaskIntoConstraints = false
        badge.addSubview(icon)

        let titleLabel = UILabel()
        titleLabel.text = title
        titleLabel.font = .systemFont(ofSize: 11)
        titleLabel.textColor = Palette.textSecondary
        titleLabel.numberOfLines = 0

        valueLabel.font = .boldSystemFont(ofSize: 15)
        valueLabel.textColor = Palette.textPrimary

        let texts = UIStackView(arrangedSubviews: [titleLabel, valueLabel])
        texts.axis = .vertical
        texts.spacing = 2
        texts.translatesAutoresizingMaskIntoConstraints = false

        card.addSubview(badge)
        card.addSubview(texts)

        NSLayoutConstraint.activate([
            badge.topAnchor.constraint(equalTo: card.topAnchor, constant: 14),
            badge.leadingAnchor.constraint(equalTo: card.leadingAnchor, constant: 14),
            badge.widthAnchor.constraint(equalToConstant: 32),
            badge.heightAnchor.constraint(equalToConstant: 32),
            icon.centerXAnchor.constraint(equalTo: badge.centerXAnchor),
            icon.centerYAnchor.constraint(equalTo: badge.centerYAnchor),
            icon.widthAnchor.constraint(equalToConstant: 16),
            icon.heightAnchor.constraint(equalToConstant: 16),

            texts.topAnchor.constraint(equalTo: badge.bottomAnchor, constant: 10),
            texts.leadingAnchor.constraint(equalTo: card.leadingAnchor, constant: 14),
            texts.trailingAnchor.constraint(equalTo: card.trailingAnchor, constant: -14),
            texts.bottomAnchor.constraint(equalTo: card.bottomAnchor, constant: -14),
        ])
        return card
    }

    private func buildTransactionsSection() -> UIView {
        let titleLabel = UILabel()
        titleLabel.text = "Transacciones"
        titleLabel.font = .boldSystemFont(ofSize: 17)
        titleLabel.textColor = Palette.textPrimary

        let seeAllLabel = UILabel()
        seeAllLabel.text = "Ver todo"
        seeAllLabel.font = .boldSystemFont(ofSize: 13)
        seeAllLabel.textColor = Palette.textSecondary

        let header = UIStackView(arrangedSubviews: [titleLabel, UIView(), seeAllLabel])
        header.axis = .horizontal
        header.alignment = .center

        transactionsStack.axis = .vertical
        transactionsStack.spacing = 10

        let section = UIStackView(arrangedSubviews: [header, transactionsStack])
        section.axis = .vertical
        section.spacing = 14
        return section
    }

    private func buildTabBar() -> UITabBar {
        let tabBar = UITabBar()
        tabBar.translatesAutoresizingMaskIntoConstraints = false
        tabBar.backgroundColor = Palette.white
        tabBar.tintColor = Palette.textPrimary
        tabBar.unselectedItemTintColor = Palette.textSecondary

        let items = [
            UITabBarItem(title: nil, image: UIImage(systemName: "house.fill"), tag: 0),
            UITabBarItem(title: nil, image: UIImage(systemName: "creditcard.fill"), tag: 1),
            UITabBarItem(title: nil, image: UIImage(systemName: "chart.bar.fill"), tag: 2),
            UITabBarItem(title: nil, image: UIImage(systemName: "gearshape.fill"), tag: 3),
        ]
        items[0].accessibilityLabel = "Inicio"
        items[1].accessibilityLabel = "Billetera"
        items[2].accessibilityLabel = "Estadísticas"
        items[3].accessibilityLabel = "Ajustes"
        tabBar.items = items
        tabBar.selectedItem = items[0]
        addSubview(tabBar)

        NSLayoutConstraint.activate([
            tabBar.leadingAnchor.constraint(equalTo: leadingAnchor),
            tabBar.trailingAnchor.constraint(equalTo: trailingAnchor),
            tabBar.bottomAnchor.constraint(equalTo: bottomAnchor),
        ])
        return tabBar
    }

    private func buildLoadingOverlay(bottomAnchorView: UIView) {
        loadingOverlay.backgroundColor = Palette.bg
        loadingOverlay.translatesAutoresizingMaskIntoConstraints = false
        addSubview(loadingOverlay)

        let spinner = UIActivityIndicatorView(style: .large)
        spinner.color = Palette.iconMint
        spinner.startAnimating()
        spinner.translatesAutoresizingMaskIntoConstraints = false
        loadingOverlay.addSubview(spinner)

        NSLayoutConstraint.activate([
            loadingOverlay.topAnchor.constraint(equalTo: topAnchor),
            loadingOverlay.leadingAnchor.constraint(equalTo: leadingAnchor),
            loadingOverlay.trailingAnchor.constraint(equalTo: trailingAnchor),
            loadingOverlay.bottomAnchor.constraint(equalTo: bottomAnchorView.topAnchor),
            spinner.centerXAnchor.constraint(equalTo: loadingOverlay.centerXAnchor),
            spinner.centerYAnchor.constraint(equalTo: loadingOverlay.centerYAnchor),
        ])
    }

    private func buildErrorOverlay(bottomAnchorView: UIView) {
        errorOverlay.backgroundColor = Palette.bg
        errorOverlay.isHidden = true
        errorOverlay.translatesAutoresizingMaskIntoConstraints = false
        addSubview(errorOverlay)

        errorMessageLabel.font = .systemFont(ofSize: 14)
        errorMessageLabel.textColor = Palette.error
        errorMessageLabel.textAlignment = .center
        errorMessageLabel.numberOfLines = 0

        var configuration = UIButton.Configuration.filled()
        configuration.title = "Reintentar"
        configuration.baseBackgroundColor = Palette.iconMint
        configuration.baseForegroundColor = Palette.white
        configuration.contentInsets = NSDirectionalEdgeInsets(top: 10, leading: 20, bottom: 10, trailing: 20)
        let retryButton = UIButton(configuration: configuration)
        retryButton.addAction(UIAction { [weak self] _ in
            self?.listener?.onRetryClicked()
        }, for: .touchUpInside)

        let stack = UIStackView(arrangedSubviews: [errorMessageLabel, retryButton])
        stack.axis = .vertical
        stack.alignment = .center
        stack.spacing = 16
        stack.translatesAutoresizingMaskIntoConstraints = false
        errorOverlay.addSubview(stack)

        NSLayoutConstraint.activate([
            errorOverlay.topAnchor.constraint(equalTo: topAnchor),
            errorOverlay.leadingAnchor.constraint(equalTo: leadingAnchor),
            errorOverlay.trailingAnchor.constraint(equalTo: trailingAnchor),
            errorOverlay.bottomAnchor.constraint(equalTo: bottomAnchorView.topAnchor),
            stack.centerXAnchor.constraint(equalTo: errorOverlay.centerXAnchor),
            stack.centerYAnchor.constraint(equalTo: errorOverlay.centerYAnchor),
            stack.leadingAnchor.constraint(greaterThanOrEqualTo: errorOverlay.leadingAnchor, constant: 24),
            stack.trailingAnchor.constraint(lessThanOrEqualTo: errorOverlay.trailingAnchor, constant: -24),
        ])
    }
}
