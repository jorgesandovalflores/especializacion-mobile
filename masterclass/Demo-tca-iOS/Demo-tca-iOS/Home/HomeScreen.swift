import ComposableArchitecture
import SwiftUI

struct HomeScreen: View {

    let store: StoreOf<HomeFeature>

    @State private var selectedNavIndex = 0

    private let tabs: [(icon: String, label: String)] = [
        ("house.fill", "Inicio"),
        ("creditcard.fill", "Billetera"),
        ("chart.bar.fill", "Estadísticas"),
        ("gearshape.fill", "Ajustes"),
    ]

    var body: some View {
        TabView(selection: $selectedNavIndex) {
            ForEach(tabs.indices, id: \.self) { index in
                Tab(value: index) {
                    if index == 0 {
                        mainContent
                    } else {
                        Palette.bg.ignoresSafeArea()
                    }
                } label: {
                    Image(systemName: tabs[index].icon)
                        .accessibilityLabel(tabs[index].label)
                }
            }
        }
        .tint(Palette.textPrimary)
        .onAppear {
            store.send(.loadData)
        }
    }

    private var mainContent: some View {
        Group {
            if store.isLoading {
                LoadingState()
            } else if let message = store.errorMessage {
                ErrorState(message: message) {
                    store.send(.loadData)
                }
            } else {
                HomeContent(
                    userName: store.userName,
                    activityValues: store.activityValues,
                    activityLabels: store.activityLabels,
                    salesLastWeek: store.salesLastWeek,
                    revenueLastWeek: store.revenueLastWeek,
                    transactions: store.transactions,
                    onTransactionTap: { store.send(.transactionClicked($0)) }
                )
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Palette.bg)
    }
}

private struct LoadingState: View {

    var body: some View {
        ProgressView()
            .controlSize(.large)
            .tint(Palette.iconMint)
    }
}

private struct ErrorState: View {

    let message: String
    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Text(message)
                .font(.system(size: 14))
                .foregroundStyle(Palette.error)
                .multilineTextAlignment(.center)

            Button(action: onRetry) {
                Text("Reintentar")
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundStyle(.white)
                    .padding(.horizontal, 20)
                    .padding(.vertical, 10)
                    .background(Palette.iconMint, in: RoundedRectangle(cornerRadius: 8))
            }
        }
        .padding(24)
    }
}

private struct HomeContent: View {

    let userName: String
    let activityValues: [Int]
    let activityLabels: [String]
    let salesLastWeek: String
    let revenueLastWeek: String
    let transactions: [Transaction]
    let onTransactionTap: (String) -> Void

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                HomeHeader(userName: userName)
                    .padding(.bottom, 24)

                HStack(alignment: .top, spacing: 12) {
                    ActivityCard(values: activityValues, labels: activityLabels)
                        .containerRelativeFrame(.horizontal) { length, _ in
                            (length - 52) * 0.6
                        }

                    VStack(spacing: 12) {
                        StatCard(
                            title: "Ventas la semana pasada",
                            value: salesLastWeek,
                            background: Palette.cardPeach,
                            iconSystemName: "tag.fill",
                            iconTint: Palette.iconPeach
                        )
                        StatCard(
                            title: "Ingresos la semana pasada",
                            value: revenueLastWeek,
                            background: Palette.cardLavender,
                            iconSystemName: "chart.pie.fill",
                            iconTint: Palette.iconLavender
                        )
                    }
                }
                .padding(.bottom, 28)

                TransactionsSection(transactions: transactions, onTransactionTap: onTransactionTap)
            }
            .padding(.horizontal, 20)
            .padding(.top, 16)
            .padding(.bottom, 24)
        }
    }
}

private struct HomeHeader: View {

    let userName: String

    var body: some View {
        HStack(spacing: 12) {
            ZStack {
                Circle()
                    .fill(Palette.cardPeach)
                    .frame(width: 44, height: 44)
                Image(systemName: "person.fill")
                    .font(.system(size: 18))
                    .foregroundStyle(Palette.iconPeach)
            }
            .accessibilityLabel("Foto de perfil")

            VStack(alignment: .leading, spacing: 2) {
                Text("Hola \(userName)")
                    .font(.system(size: 17, weight: .bold))
                    .foregroundStyle(Palette.textPrimary)
                Text("¡Bienvenido de nuevo!")
                    .font(.system(size: 13))
                    .foregroundStyle(Palette.textSecondary)
            }

            Spacer()

            ZStack {
                RoundedRectangle(cornerRadius: 14)
                    .fill(.white)
                    .frame(width: 44, height: 44)
                    .shadow(color: .black.opacity(0.06), radius: 4, y: 2)
                Image(systemName: "square.grid.2x2.fill")
                    .font(.system(size: 16))
                    .foregroundStyle(Palette.textPrimary)
            }
            .accessibilityLabel("Menú")
        }
    }
}

private struct ActivityCard: View {

    let values: [Int]
    let labels: [String]

    private let maxBarHeight: CGFloat = 64

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(alignment: .bottom, spacing: 0) {
                ForEach(values.indices, id: \.self) { index in
                    VStack(spacing: 6) {
                        Spacer(minLength: 0)
                        UnevenRoundedRectangle(topLeadingRadius: 4, topTrailingRadius: 4)
                            .fill(Palette.iconMint)
                            .frame(width: 16, height: barHeight(for: values[index]))
                        Text(index < labels.count ? labels[index] : "")
                            .font(.system(size: 10))
                            .foregroundStyle(Palette.textSecondary)
                    }
                    .frame(maxWidth: .infinity)
                }
            }
            .frame(height: 90)
            .padding(.bottom, 16)

            Text("Actividad")
                .font(.system(size: 16, weight: .bold))
                .foregroundStyle(Palette.textPrimary)
            Text("de esta semana")
                .font(.system(size: 12))
                .foregroundStyle(Palette.textSecondary)
                .padding(.top, 2)
        }
        .padding(18)
        .background(Palette.cardMint, in: RoundedRectangle(cornerRadius: 20))
    }

    private func barHeight(for value: Int) -> CGFloat {
        let maxValue = CGFloat(max(values.max() ?? 1, 1))
        return max(CGFloat(value) / maxValue * maxBarHeight, 12)
    }
}

private struct StatCard: View {

    let title: String
    let value: String
    let background: Color
    let iconSystemName: String
    let iconTint: Color

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            ZStack {
                RoundedRectangle(cornerRadius: 10)
                    .fill(.white)
                    .frame(width: 32, height: 32)
                Image(systemName: iconSystemName)
                    .font(.system(size: 13))
                    .foregroundStyle(iconTint)
            }
            .padding(.bottom, 10)

            Text(title)
                .font(.system(size: 11))
                .foregroundStyle(Palette.textSecondary)
            Text(value)
                .font(.system(size: 15, weight: .bold))
                .foregroundStyle(Palette.textPrimary)
                .padding(.top, 2)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(background, in: RoundedRectangle(cornerRadius: 20))
    }
}

private struct TransactionsSection: View {

    let transactions: [Transaction]
    let onTransactionTap: (String) -> Void

    var body: some View {
        VStack(spacing: 14) {
            HStack {
                Text("Transacciones")
                    .font(.system(size: 17, weight: .bold))
                    .foregroundStyle(Palette.textPrimary)
                Spacer()
                Text("Ver todo")
                    .font(.system(size: 13, weight: .bold))
                    .foregroundStyle(Palette.textSecondary)
            }

            VStack(spacing: 10) {
                ForEach(transactions) { transaction in
                    TransactionRow(transaction: transaction) {
                        onTransactionTap(transaction.id)
                    }
                }
            }
        }
    }
}

private struct TransactionRow: View {

    let transaction: Transaction
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 12) {
                ZStack {
                    Circle()
                        .fill(transaction.badgeColor)
                        .frame(width: 40, height: 40)
                    Image(systemName: transaction.iconSystemName)
                        .font(.system(size: 15))
                        .foregroundStyle(.white)
                }

                VStack(alignment: .leading, spacing: 2) {
                    Text(transaction.title)
                        .font(.system(size: 14, weight: .bold))
                        .foregroundStyle(Palette.textPrimary)
                    Text(transaction.category)
                        .font(.system(size: 12))
                        .foregroundStyle(Palette.textSecondary)
                }

                Spacer()

                VStack(alignment: .trailing, spacing: 2) {
                    Text(transaction.amount)
                        .font(.system(size: 14, weight: .bold))
                        .foregroundStyle(Palette.textPrimary)
                    Text(transaction.time)
                        .font(.system(size: 12))
                        .foregroundStyle(Palette.textSecondary)
                }
            }
            .padding(14)
            .background(transaction.rowBackgroundColor, in: RoundedRectangle(cornerRadius: 16))
        }
        .buttonStyle(.plain)
    }
}

#Preview {
    HomeScreen(
        store: Store(initialState: HomeFeature.State()) {
            HomeFeature()
        }
    )
}
