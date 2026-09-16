package com.example.example.common.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.example.common.model.Product

// Componente centralizado (ver diseño en design-m02-c01.pen): la misma card
// se usa en MVC, MVP y MVVM (ver ProductListContent.kt), así las tres
// arquitecturas se ven idénticas y solo cambia cómo llega el estado hasta aquí.
// Card blanca con elevación ligera sobre el fondo gris de ProductListBackground.
@Composable
fun ProductCard(product: Product, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(product.name, style = MaterialTheme.typography.titleMedium)
            Text("$${product.price}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = if (product.inStock) "En stock" else "Agotado",
                style = MaterialTheme.typography.labelMedium,
                color = if (product.inStock) StockAvailableColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private val StockAvailableColor = Color(0xFF1F8A5A)

@Preview(showBackground = true, name = "ProductCard - En stock")
@Composable
private fun ProductCardInStockPreview() {
    ProductCard(Product("1", "Fjallraven - Foldsack No. 1 Backpack, Fits 15 Laptops", 109.95, "men's clothing", true))
}

@Preview(showBackground = true, name = "ProductCard - Agotado")
@Composable
private fun ProductCardOutOfStockPreview() {
    ProductCard(Product("4", "Mens Casual Slim Fit", 15.99, "men's clothing", false))
}
