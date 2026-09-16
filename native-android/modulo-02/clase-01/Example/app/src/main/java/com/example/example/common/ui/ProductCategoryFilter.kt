package com.example.example.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Fila de chips de categoría (ver "Category Filter" en design-m02-c01.pen):
// el chip seleccionado usa el mismo color de acento que el eyebrow de
// ProductListHeader; el resto usa la superficie blanca con borde, como
// ProductCard. Componente puramente visual: no sabe de dónde vienen las
// categorías ni cuál está seleccionada, solo lo renderiza (usado por ahora
// solo desde ProductListScreenMVVM.kt).
@Composable
fun ProductCategoryFilter(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        categories.forEach { category ->
            val isSelected = category == selected
            val shape = RoundedCornerShape(16.dp)
            Text(
                text = category,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .background(color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White, shape = shape)
                    .border(width = 1.dp, color = if (isSelected) MaterialTheme.colorScheme.primary else CategoryBorderColor, shape = shape)
                    .clickable { onSelect(category) }
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            )
        }
    }
}

private val CategoryBorderColor = Color(0xFFEAEAEE)

@Preview(showBackground = true, name = "ProductCategoryFilter - Todos seleccionado")
@Composable
private fun ProductCategoryFilterPreview() {
    ProductCategoryFilter(
        categories = listOf("Todos", "Electrónica", "Joyería", "Hombre", "Mujer"),
        selected = "Todos",
        onSelect = {}
    )
}

@Preview(showBackground = true, name = "ProductCategoryFilter - Categoría seleccionada")
@Composable
private fun ProductCategoryFilterSelectedPreview() {
    ProductCategoryFilter(
        categories = listOf("Todos", "Electrónica", "Joyería", "Hombre", "Mujer"),
        selected = "Electrónica",
        onSelect = {}
    )
}
