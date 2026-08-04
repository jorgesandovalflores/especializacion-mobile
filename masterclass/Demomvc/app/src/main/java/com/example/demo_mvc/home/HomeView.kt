package com.example.demo_mvc.home

import android.view.View

/**
 * MVC View contract: an interface plus an implementation (HomeViewImpl).
 * The Controller (MainActivity) only depends on this interface, never on the
 * concrete widgets — the View could be swapped for a different layout/implementation
 * without the Controller or Model changing.
 */
interface HomeView {

    interface Listener {
        fun onTransactionClicked(transactionId: String)
        fun onRetryClicked()
    }

    fun getRootView(): View
    fun setListener(listener: Listener)
    fun showLoading()
    fun showData(data: HomeUiData)
    fun showError(message: String)
}
