import UIKit

protocol HomeViewListener: AnyObject {
    func onTransactionClicked(_ transactionId: String)
    func onRetryClicked()
}

protocol HomeView: AnyObject {

    var rootView: UIView { get }

    func setListener(_ listener: HomeViewListener)
    func showLoading()
    func showData(_ data: HomeUiData)
    func showError(_ message: String)
}
