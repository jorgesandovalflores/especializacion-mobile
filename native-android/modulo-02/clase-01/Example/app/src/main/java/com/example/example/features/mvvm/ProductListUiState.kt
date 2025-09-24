package com.example.example.features.mvvm

import com.example.example.common.model.Product

data class ProductListUiState(
    val loading: Boolean = false,
    val data: List<Product> = emptyList(),
    val error: String? = null
)