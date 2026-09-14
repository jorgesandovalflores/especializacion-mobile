package com.example.example.features.mvp

import com.example.example.common.data.LoggingProductRepository
import com.example.example.common.data.ProductRemoteRepository
import com.example.example.common.data.ProductRepository
import kotlinx.coroutines.*

// Mediator/Proxy (ver patrones-diseno-arquitecturas.md): orquesta el Modelo
// (endpoint real por defecto) y empuja los resultados a la View mediante
// el contrato ProductListContract, siguiendo el Template Method
// showLoading -> fetch -> showProducts/showError.
class ProductListPresenter(
    private val repository: ProductRepository = LoggingProductRepository(ProductRemoteRepository())
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