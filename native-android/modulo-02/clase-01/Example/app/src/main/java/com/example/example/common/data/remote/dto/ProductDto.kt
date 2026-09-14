package com.example.example.common.data.remote.dto

// Forma exacta del JSON de https://fakestoreapi.com/products (Data Transfer Object)
data class ProductDto(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String,
    val rating: RatingDto? = null
)
