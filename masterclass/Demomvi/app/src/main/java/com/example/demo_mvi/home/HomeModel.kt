package com.example.demo_mvi.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.LocalDrink
import com.example.demo_mvi.ui.theme.BadgeNike
import com.example.demo_mvi.ui.theme.BadgePepsi
import com.example.demo_mvi.ui.theme.RowMint
import com.example.demo_mvi.ui.theme.RowPeach
import kotlinx.coroutines.delay

/**
 * Data/business logic for the Home screen — the Model in MVI (see
 * references/ui-pattern-mvi.md in the android-development skill). The
 * ViewModel is the only layer that calls this; the Reducer never touches it.
 *
 * Data is mocked here to simulate what would normally come from a Repository
 * (network + local database), including a simulated latency via `delay(...)`.
 */
class HomeModel {

    suspend fun fetchHomeData(): HomeData {
        delay(900) // simulates network/DB latency

        return HomeData(
            userName = "John Smith",
            activityValues = listOf(20, 40, 80, 40, 20),
            activityLabels = listOf("20", "40", "80", "40", "20"),
            salesLastWeek = "$280.99",
            revenueLastWeek = "$280.99",
            transactions = listOf(
                Transaction(
                    id = "tx-1",
                    title = "Nike Store",
                    category = "Ropa y calzado",
                    amount = "-27.67",
                    time = "2:23 p.m.",
                    icon = Icons.Rounded.Checkroom,
                    badgeColor = BadgeNike,
                    rowColor = RowPeach,
                ),
                Transaction(
                    id = "tx-2",
                    title = "Pepsi",
                    category = "Restaurantes y café",
                    amount = "-34.92",
                    time = "10:00 a.m.",
                    icon = Icons.Rounded.LocalDrink,
                    badgeColor = BadgePepsi,
                    rowColor = RowMint,
                ),
            ),
        )
    }

    suspend fun markTransactionSeen(transactionId: String) {
        delay(150) // simulates persisting the "seen" flag
    }
}
