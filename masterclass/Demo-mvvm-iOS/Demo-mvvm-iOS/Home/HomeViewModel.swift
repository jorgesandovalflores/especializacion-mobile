import Combine
import Foundation

@MainActor
final class HomeViewModel: ObservableObject {

    @Published private(set) var uiState: HomeUiState = .loading

    private let model: HomeModel

    init(model: HomeModel = HomeModel()) {
        self.model = model
        loadData()
    }

    func loadData() {
        Task {
            uiState = .loading
            do {
                let data = try await model.fetchHomeData()
                uiState = .success(data)
            } catch {
                uiState = .error("No se pudo cargar la información")
            }
        }
    }

    func onTransactionClicked(_ transactionId: String) {
        Task {
            await model.markTransactionSeen(transactionId: transactionId)
        }
    }
}
