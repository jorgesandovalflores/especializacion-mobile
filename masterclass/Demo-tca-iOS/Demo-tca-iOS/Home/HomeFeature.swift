import ComposableArchitecture
import Foundation

@Reducer
nonisolated struct HomeFeature {

    @ObservableState
    struct State: Equatable {
        var userName = ""
        var activityValues: [Int] = []
        var activityLabels: [String] = []
        var salesLastWeek = ""
        var revenueLastWeek = ""
        var transactions: [Transaction] = []
        var isLoading = true
        var errorMessage: String?
    }

    enum Action {
        case loadData
        case homeDataResponse(HomeUiData)
        case homeDataFailed
        case transactionClicked(String)
    }

    private let model = HomeModel()

    var body: some ReducerOf<Self> {
        Reduce { state, action in
            switch action {
            case .loadData:
                state.isLoading = true
                state.errorMessage = nil
                return .run { [model] send in
                    do {
                        let data = try await model.fetchHomeData()
                        await send(.homeDataResponse(data))
                    } catch {
                        await send(.homeDataFailed)
                    }
                }

            case .homeDataResponse(let data):
                state.isLoading = false
                state.userName = data.userName
                state.activityValues = data.activityValues
                state.activityLabels = data.activityLabels
                state.salesLastWeek = data.salesLastWeek
                state.revenueLastWeek = data.revenueLastWeek
                state.transactions = data.transactions
                return .none

            case .homeDataFailed:
                state.isLoading = false
                state.errorMessage = "No se pudo cargar la información"
                return .none

            case .transactionClicked(let id):
                return .run { [model] _ in
                    await model.markTransactionSeen(transactionId: id)
                }
            }
        }
    }
}
