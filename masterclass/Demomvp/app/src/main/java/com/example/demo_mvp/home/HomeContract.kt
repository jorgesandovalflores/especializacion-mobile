package com.example.demo_mvp.home

/**
 * MVP contract: defines View and Presenter through interfaces so neither depends
 * on the other's concrete type, and the Model never knows either exists.
 * See references/ui-pattern-mvp.md in the android-development skill.
 */
interface HomeContract {

    interface View {
        fun showLoading()
        fun hideLoading()
        fun showData(data: HomeUiData)
        fun showError(message: String)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadData()
        fun onTransactionClicked(transactionId: String)
    }
}
