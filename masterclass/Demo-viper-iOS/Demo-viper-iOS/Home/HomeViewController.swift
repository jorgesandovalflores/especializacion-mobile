import UIKit

final class HomeViewController: UIViewController, HomeViewProtocol {

    var presenter: HomePresenterProtocol?

    private let rootView = HomeRootView()

    private let maxBarHeight: CGFloat = 64

    override func loadView() {
        view = rootView
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        rootView.retryButton.addAction(UIAction { [weak self] _ in
            self?.presenter?.didTapRetry()
        }, for: .touchUpInside)

        presenter?.viewDidLoad()
    }

    func showLoading() {
        rootView.loadingOverlay.isHidden = false
        rootView.errorOverlay.isHidden = true
    }

    func hideLoading() {
        rootView.loadingOverlay.isHidden = true
    }

    func showData(_ data: HomeUiData) {
        rootView.errorOverlay.isHidden = true

        rootView.greetingLabel.text = "Hola \(data.userName)"
        rootView.salesValueLabel.text = data.salesLastWeek
        rootView.revenueValueLabel.text = data.revenueLastWeek

        renderActivityChart(values: data.activityValues, labels: data.activityLabels)

        rootView.transactionsStack.arrangedSubviews.forEach { $0.removeFromSuperview() }
        for transaction in data.transactions {
            let row = TransactionRowView(transaction: transaction) { [weak self] in
                self?.presenter?.didSelectTransaction(transaction.id)
            }
            rootView.transactionsStack.addArrangedSubview(row)
        }
    }

    func showError(_ message: String) {
        rootView.errorOverlay.isHidden = false
        rootView.errorMessageLabel.text = message
    }

    private func renderActivityChart(values: [Int], labels: [String]) {
        let maxValue = CGFloat(max(values.max() ?? 1, 1))

        for (index, value) in values.enumerated() where index < rootView.barHeightConstraints.count {
            let height = max(CGFloat(value) / maxValue * maxBarHeight, 12)
            rootView.barHeightConstraints[index].constant = height
        }
        for (index, label) in labels.enumerated() where index < rootView.barLabels.count {
            rootView.barLabels[index].text = label
        }
        rootView.layoutIfNeeded()
    }
}
