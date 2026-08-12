import Foundation

@MainActor
final class HomePresenter: HomePresenterProtocol {

    private let model: HomeModel
    private weak var view: HomeViewProtocol?
    private var tasks: [Task<Void, Never>] = []

    init(model: HomeModel) {
        self.model = model
    }

    func attachView(_ view: HomeViewProtocol) {
        self.view = view
    }

    func detachView() {
        tasks.forEach { $0.cancel() }
        tasks.removeAll()
        view = nil
    }

    func loadData() {
        view?.showLoading()
        tasks.append(Task { [weak self] in
            guard let self else { return }
            do {
                let data = try await model.fetchHomeData()
                view?.hideLoading()
                view?.showData(data)
            } catch {
                view?.hideLoading()
                view?.showError("No se pudo cargar la información")
            }
        })
    }

    func onTransactionClicked(_ transactionId: String) {
        tasks.append(Task { [model] in
            await model.markTransactionSeen(transactionId: transactionId)
        })
    }
}
