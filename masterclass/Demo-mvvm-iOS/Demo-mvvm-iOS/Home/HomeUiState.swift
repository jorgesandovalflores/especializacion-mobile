import Foundation

enum HomeUiState: Equatable {
    case loading
    case success(HomeUiData)
    case error(String)
}
