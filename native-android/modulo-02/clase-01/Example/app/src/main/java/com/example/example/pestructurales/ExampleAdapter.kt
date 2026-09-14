package com.example.example.pestructurales

data class ExampleAdapterModel (
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val isStock: Boolean = false
)

data class ExampleAdapterEntity (
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val quantity: Int = 0
) {
    fun toModel() = ExampleAdapterModel(
        name = name,
        description = description,
        imageUrl = imageUrl,
        isStock = quantity > 0
    )
}