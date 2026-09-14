package com.example.example.common.data

import com.example.example.common.model.Product

// Contrato del Modelo: MVC, MVP y MVVM dependen de esta interfaz, no de una
// implementación concreta (Dependency Inversion). Ver README.md "0. Fundamentos".
interface ProductRepository {
    suspend fun fetchProducts(): List<Product>
}
