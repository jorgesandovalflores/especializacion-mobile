package com.example.example.rutasargumentos

object Routes {
    const val ProductList = "product_list"
    const val ProductDetail = "product_detail/{id}"

    // Helper para construir la ruta final con un Int
    fun productDetail(id: Int) = "product_detail/$id"
}