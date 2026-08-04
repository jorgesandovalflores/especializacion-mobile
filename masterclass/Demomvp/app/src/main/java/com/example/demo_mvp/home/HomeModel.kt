package com.example.demo_mvp.home

import android.graphics.Color
import com.example.demo_mvp.R
import kotlinx.coroutines.delay

/**
 * Data/business logic for the Home screen — no Android Context, no UI references.
 * Follows the MVP pattern's Model layer (see references/ui-pattern-mvp.md in the
 * android-development skill): the Presenter is the only layer that calls this.
 *
 * Data is mocked here to simulate what would normally come from a Repository
 * (network + local database), including a simulated latency via `delay(...)`.
 */
class HomeModel {

    suspend fun fetchHomeData(): HomeUiData {
        delay(900) // simulates network/DB latency

        return HomeUiData(
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
                    iconRes = R.drawable.ic_tag_24,
                    badgeColor = Color.parseColor("#1A1A1A"),
                    rowBackgroundRes = R.drawable.bg_row_peach,
                ),
                Transaction(
                    id = "tx-2",
                    title = "Pepsi",
                    category = "Restaurantes y café",
                    amount = "-34.92",
                    time = "10:00 a.m.",
                    iconRes = R.drawable.ic_cup_24,
                    badgeColor = Color.parseColor("#E4002B"),
                    rowBackgroundRes = R.drawable.bg_row_mint,
                ),
            ),
        )
    }

    suspend fun markTransactionSeen(transactionId: String) {
        delay(150) // simulates persisting the "seen" flag
    }
}
