package com.example.example.common.data

import android.util.Log
import com.example.example.common.model.Product

// Patrón Decorator (ver patrones-diseno-arquitecturas.md § Estructurales):
// añade logging alrededor de cualquier ProductRepository sin modificarlo
// ni duplicar su lógica. Se usa envolviendo a ProductRemoteRepository.
class LoggingProductRepository(
    private val delegate: ProductRepository
) : ProductRepository {

    override suspend fun fetchProducts(): List<Product> {
        Log.d(TAG, "fetchProducts() -> inicio")
        return runCatching { delegate.fetchProducts() }
            .onSuccess { Log.d(TAG, "fetchProducts() -> éxito (${it.size} productos)") }
            .onFailure { Log.e(TAG, "fetchProducts() -> error: ${it.message}") }
            .getOrThrow()
    }

    private companion object {
        const val TAG = "ProductRepository"
    }
}
