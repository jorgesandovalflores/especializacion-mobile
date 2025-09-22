package com.example.example.deeplink

object Routes {
    const val Home = "home"
    const val Detail = "detail/{id}"

    // Helper para construir ruta interna
    fun detail(id: Long) = "detail/$id"
}