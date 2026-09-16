package com.example.example.common.data.remote

import com.example.example.common.data.remote.dto.ProductDto
import com.example.example.common.model.Product

// Patrón Adapter (ver patrones-diseno-arquitecturas.md § Estructurales):
// traduce el DTO remoto (forma de fakestoreapi.com) al modelo de dominio
// que ya conocen MVC/MVP/MVVM, sin que las capas superiores conozcan el JSON.
fun ProductDto.toDomain(): Product = Product(
    id = id.toString(),
    name = title,
    price = price,
    category = category,
    // Simplificación pedagógica: la API pública no modela stock; alternamos
    // por id para poder demostrar en la demo tanto "in stock" como "out of stock".
    inStock = id % 4 != 0
)
