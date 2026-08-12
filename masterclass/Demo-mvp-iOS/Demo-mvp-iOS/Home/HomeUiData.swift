import UIKit

struct Transaction {
    let id: String
    let title: String
    let category: String
    let amount: String
    let time: String
    let iconSystemName: String
    let badgeColor: UIColor
    let rowBackgroundColor: UIColor
}

struct HomeUiData {
    let userName: String
    let activityValues: [Int]
    let activityLabels: [String]
    let salesLastWeek: String
    let revenueLastWeek: String
    let transactions: [Transaction]
}
