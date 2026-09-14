package com.example.example.common.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Patrón Singleton (ver patrones-diseno-arquitecturas.md § Creacionales):
// una única instancia de Retrofit compartida por toda la app.
object RetrofitProvider {
    private const val BASE_URL = "https://fakestoreapi.com/"

    val api: ProductApiService by lazy { buildRetrofit().create(ProductApiService::class.java) }

    private fun buildRetrofit(): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
