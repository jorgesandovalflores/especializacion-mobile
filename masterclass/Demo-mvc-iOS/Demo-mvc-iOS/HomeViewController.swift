import UIKit

final class HomeViewController: UIViewController, HomeViewListener {

    private let model = HomeModel()
    private let homeView: HomeView = HomeViewImpl()
    private var loadTask: Task<Void, Never>?

    override func loadView() {
        view = homeView.rootView
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        homeView.setListener(self)
        loadHomeData()
    }

    deinit {
        loadTask?.cancel()
    }

    private func loadHomeData() {
        homeView.showLoading()
        loadTask = Task { [weak self] in
            guard let self else { return }
            do {
                let data = try await model.fetchHomeData()
                homeView.showData(data)
            } catch {
                homeView.showError("No se pudo cargar la información")
            }
        }
    }

    func onTransactionClicked(_ transactionId: String) {
        Task { [model] in
            await model.markTransactionSeen(transactionId: transactionId)
        }
    }

    func onRetryClicked() {
        loadHomeData()
    }
}
