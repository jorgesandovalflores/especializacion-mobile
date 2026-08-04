package com.example.demo_mvp.home

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

/**
 * Mediator between View and Model. No Android Context, no widget references —
 * this is what makes the Presenter unit-testable with a fake View (see
 * references/ui-pattern-mvp.md in the android-development skill).
 */
class HomePresenter(
    private val model: HomeModel,
) : HomeContract.Presenter {

    private var view: HomeContract.View? = null
    private val presenterScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun attachView(view: HomeContract.View) {
        this.view = view
    }

    override fun detachView() {
        presenterScope.coroutineContext.cancelChildren()
        view = null
    }

    override fun loadData() {
        view?.showLoading()
        presenterScope.launch {
            try {
                val data = model.fetchHomeData()
                view?.hideLoading()
                view?.showData(data)
            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError(e.message ?: "Unknown error")
            }
        }
    }

    override fun onTransactionClicked(transactionId: String) {
        presenterScope.launch { model.markTransactionSeen(transactionId) }
    }
}
