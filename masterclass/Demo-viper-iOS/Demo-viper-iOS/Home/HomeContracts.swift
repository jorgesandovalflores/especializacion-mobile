import UIKit

@MainActor
protocol HomeViewProtocol: AnyObject {
    func showLoading()
    func hideLoading()
    func showData(_ data: HomeUiData)
    func showError(_ message: String)
}

@MainActor
protocol HomePresenterProtocol: AnyObject {
    func viewDidLoad()
    func didTapRetry()
    func didSelectTransaction(_ transactionId: String)
}

@MainActor
protocol HomeInteractorProtocol: AnyObject {
    func fetchHomeData() async throws -> HomeUiData
    func markTransactionSeen(transactionId: String) async
}

@MainActor
protocol HomeRouterProtocol: AnyObject {
    static func createModule() -> UIViewController
}
