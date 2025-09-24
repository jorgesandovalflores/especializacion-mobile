package com.example.example.features.mvp

import com.example.example.common.data.FakeProductRepository
import kotlinx.coroutines.*

// Presenter: orquesta carga y notifica a la View
class ProductListPresenter(
    private val repository: FakeProductRepository = FakeProductRepository()
) : ProductListContract.Presenter {

    private var view: ProductListContract.View? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun attach(view: ProductListContract.View) {
        this.view = view
    }

    override fun detach() {
        this.view = null
        scope.coroutineContext.cancelChildren()
    }

    override fun load() {
        view?.showLoading()
        scope.launch {
            runCatching { repository.fetchProducts() }
                .onSuccess { view?.showProducts(it) }
                .onFailure { view?.showError(it.message ?: "Unknown error") }
        }
    }
}