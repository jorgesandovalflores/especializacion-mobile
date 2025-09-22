package com.example.example.argumentos

object Routes {
    const val Home = "home"
    const val Detail = "detail/{id}"

    // Helper para construir la ruta final con Int
    fun detail(id: Int) = "detail/$id"
}