package com.example.example.features.mvvm

import com.example.example.common.model.Product

// UiState único e inmutable (ver README.md §0, "puerta de entrada a MVI"):
// allProducts es lo que llega del repositorio; products es un valor derivado
// que aplica selectedCategory. La View nunca filtra por su cuenta, solo lee
// products/categories/selectedCategory ya resueltos.
data class ProductListUiState(
    val loading: Boolean = false,
    val allProducts: List<Product> = emptyList(),
    val categories: List<CategoryFilter> = emptyList(),
    val selectedCategory: String? = null,
    val error: String? = null
) {
    val products: List<Product>
        get() = selectedCategory
            ?.let { category -> allProducts.filter { it.category == category } }
            ?: allProducts
}

// Un chip del filtro: "value" es la categoría cruda de fakestoreapi.com
// (null representa "Todos", sin filtro); "label" es el texto ya traducido
// para mostrar (ver design-m02-c01.pen).
data class CategoryFilter(
    val value: String?,
    val label: String
)

private val CategoryLabels = mapOf(
    "electronics" to "Electrónica",
    "jewelery" to "Joyería",
    "men's clothing" to "Hombre",
    "women's clothing" to "Mujer"
)

fun String.toCategoryLabel(): String = CategoryLabels[this] ?: replaceFirstChar { it.uppercase() }
