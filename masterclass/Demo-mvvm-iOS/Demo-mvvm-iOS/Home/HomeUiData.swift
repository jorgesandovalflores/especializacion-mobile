import SwiftUI

struct Transaction: Identifiable, Equatable {
    let id: String
    let title: String
    let category: String
    let amount: String
    let time: String
    let iconSystemName: String
    let badgeColor: Color
    let rowBackgroundColor: Color
}

struct HomeUiData: Equatable {
    let userName: String
    let activityValues: [Int]
    let activityLabels: [String]
    let salesLastWeek: String
    let revenueLastWeek: String
    let transactions: [Transaction]
}
