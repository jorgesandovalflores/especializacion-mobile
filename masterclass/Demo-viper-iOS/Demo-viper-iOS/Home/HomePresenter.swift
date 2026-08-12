import Foundation

final class HomePresenter: HomePresenterProtocol {

    private weak var view: HomeViewProtocol?
    private let interactor: HomeInteractorProtocol
    private let router: HomeRouterProtocol

    init(view: HomeViewProtocol, interactor: HomeInteractorProtocol, router: HomeRouterProtocol) {
        self.view = view
        self.interactor = interactor
        self.router = router
    }

    func viewDidLoad() {
        loadData()
    }

    func didTapRetry() {
        loadData()
    }

    func didSelectTransaction(_ transactionId: String) {
        Task { [interactor] in
            await interactor.markTransactionSeen(transactionId: transactionId)
        }
    }

    private func loadData() {
        view?.showLoading()
        Task { [weak self] in
            guard let self else { return }
            do {
                let data = try await interactor.fetchHomeData()
                view?.hideLoading()
                view?.showData(data)
            } catch {
                view?.hideLoading()
                view?.showError("No se pudo cargar la información")
            }
        }
    }
}
