import Foundation

final class HomeInteractor: HomeInteractorProtocol {

    func fetchHomeData() async throws -> HomeUiData {
        try await Task.sleep(for: .milliseconds(900))

        return HomeUiData(
            userName: "John Smith",
            activityValues: [20, 40, 80, 40, 20],
            activityLabels: ["20", "40", "80", "40", "20"],
            salesLastWeek: "$280.99",
            revenueLastWeek: "$280.99",
            transactions: [
                Transaction(
                    id: "tx-1",
                    title: "Nike Store",
                    category: "Ropa y calzado",
                    amount: "-27.67",
                    time: "2:23 p.m.",
                    iconSystemName: "tag.fill",
                    badgeColor: Palette.badgeNike,
                    rowBackgroundColor: Palette.rowPeach
                ),
                Transaction(
                    id: "tx-2",
                    title: "Pepsi",
                    category: "Restaurantes y café",
                    amount: "-34.92",
                    time: "10:00 a.m.",
                    iconSystemName: "cup.and.saucer.fill",
                    badgeColor: Palette.badgePepsi,
                    rowBackgroundColor: Palette.rowMint
                ),
            ]
        )
    }

    func markTransactionSeen(transactionId: String) async {
        try? await Task.sleep(for: .milliseconds(150))
    }
}
