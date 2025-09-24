package com.example.example.common.data

import com.example.example.common.model.Product
import kotlinx.coroutines.delay

// Repositorio falso en memoria
class FakeProductRepository {
    // Simula I/O
    suspend fun fetchProducts(): List<Product> {
        delay(300) // simular latencia
        return listOf(
            Product("1", "Keyboard TKL", 49.9, true),
            Product("2", "Wireless Mouse", 24.5, true),
            Product("3", "4K Monitor", 299.0, false),
            Product("4", "USB-C Hub", 39.0, true),
        )
    }
}