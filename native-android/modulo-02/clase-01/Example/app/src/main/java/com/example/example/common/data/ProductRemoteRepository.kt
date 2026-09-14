package com.example.example.common.data

import com.example.example.common.data.remote.ProductApiService
import com.example.example.common.data.remote.RetrofitProvider
import com.example.example.common.data.remote.toDomain
import com.example.example.common.model.Product

// Implementación real del Modelo: consume GET https://fakestoreapi.com/products
// y adapta la respuesta a Product. Es el repositorio que usan por defecto
// MVC, MVP y MVVM (ver README.md y comparacion-arquitecturas.md).
class ProductRemoteRepository(
    private val api: ProductApiService = RetrofitProvider.api
) : ProductRepository {
    override suspend fun fetchProducts(): List<Product> =
        api.getProducts().map { it.toDomain() }
}
