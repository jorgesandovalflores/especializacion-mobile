package com.example.example.common.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.example.common.model.Product

// Fondo gris de la pantalla (ver design-m02-c01.pen), compartido por los 3
// Scaffold de MVC/MVP/MVVM para que los ProductCard blancos resalten sobre él.
val ProductListBackground = Color(0xFFF7F7F7L)

// Cuerpo compartido por MVC, MVP y MVVM (ver ProductListScreenMVC/MVP/MVVM.kt):
// cada arquitectura solo decide cómo llenar estos parámetros; la View nunca
// decide de dónde vienen los datos (Facade/Mediator, ver
// patrones-diseno-arquitecturas.md).
@Composable
fun ProductListContent(
    loading: Boolean,
    error: String?,
    products: List<Product>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryInfo: String? = null
) {
    Box(modifier.fillMaxSize()) {
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            error != null -> Column(Modifier
                .padding(16.dp)
            ) {
                Text("Error: $error")
                retryInfo?.let { Text(it) }
                Button(onClick = onRetry) { Text("Reintentar") }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(products, key = { it.id }) { p -> ProductCard(p) }
            }
        }
    }
}

private val SampleProducts = listOf(
    Product("1", "Fjallraven - Foldsack No. 1 Backpack, Fits 15 Laptops", 109.95, true),
    Product("2", "Mens Casual Premium Slim Fit T-Shirts", 22.3, true),
    Product("3", "Mens Cotton Jacket", 55.99, true),
    Product("4", "Mens Casual Slim Fit", 15.99, false),
    Product("5", "John Hardy Women's Legends Naga Gold & Silver Dragon Station Chain Bracelet", 695.0, true)
)

@Preview(showBackground = true, name = "ProductListContent - Pantalla principal (5 items)")
@Composable
private fun ProductListContentSuccessPreview() {
    ProductListContent(loading = false, error = null, products = SampleProducts, onRetry = {})
}

@Preview(showBackground = true, name = "ProductListContent - Cargando")
@Composable
private fun ProductListContentLoadingPreview() {
    ProductListContent(loading = true, error = null, products = emptyList(), onRetry = {})
}

@Preview(showBackground = true, name = "ProductListContent - Error")
@Composable
private fun ProductListContentErrorPreview() {
    ProductListContent(
        loading = false,
        error = "Unable to resolve host",
        products = emptyList(),
        onRetry = {},
        retryInfo = "Reintentos: 2"
    )
}
