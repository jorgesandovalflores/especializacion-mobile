package com.example.example.common.data

import com.example.example.common.model.Product
import kotlinx.coroutines.delay

// Repositorio falso en memoria: implementa el mismo contrato que
// ProductRemoteRepository para poder sustituirse sin tocar Controller/
// Presenter/ViewModel (Strategy + Dependency Inversion). Útil para tests
// y para practicar sin conexión a internet.
class FakeProductRepository : ProductRepository {
    // Simula I/O
    override suspend fun fetchProducts(): List<Product> {
        delay(2000) // simular latencia
        return listOf(
            Product("1", "Keyboard TKL", 49.9, "electronics", true),
            Product("2", "Wireless Mouse", 24.5, "electronics", true),
            Product("3", "4K Monitor", 299.0, "electronics", false),
            Product("4", "USB-C Hub", 39.0, "electronics", true),
        )
    }
}