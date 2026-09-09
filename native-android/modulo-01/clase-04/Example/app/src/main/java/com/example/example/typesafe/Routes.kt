package com.example.example.typesafe

import kotlinx.serialization.Serializable

@Serializable
object ProductList

@Serializable
data class ProductDetail(val id: Int)
