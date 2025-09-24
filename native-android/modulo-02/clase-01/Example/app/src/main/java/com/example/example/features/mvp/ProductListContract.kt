package com.example.example.features.mvp

import com.example.example.common.model.Product

// Contrato MVP entre View y Presenter
interface ProductListContract {
    interface View {
        // Mostrar estado de carga
        fun showLoading()
        // Mostrar data
        fun showProducts(list: List<Product>)
        // Mostrar error
        fun showError(message: String)
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun load()
    }
}