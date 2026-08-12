import Foundation

@MainActor
protocol HomeViewProtocol: AnyObject {
    func showLoading()
    func hideLoading()
    func showData(_ data: HomeUiData)
    func showError(_ message: String)
}

@MainActor
protocol HomePresenterProtocol: AnyObject {
    func attachView(_ view: HomeViewProtocol)
    func detachView()
    func loadData()
    func onTransactionClicked(_ transactionId: String)
}
