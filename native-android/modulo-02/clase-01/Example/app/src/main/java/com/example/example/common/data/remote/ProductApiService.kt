package com.example.example.common.data.remote

import com.example.example.common.data.remote.dto.ProductDto
import retrofit2.http.GET

// Contrato Retrofit del endpoint real que consume esta clase (basePath: fakestoreapi.com)
interface ProductApiService {
    @GET("products")
    suspend fun getProducts(): List<ProductDto>
}
