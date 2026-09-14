package com.example.example.common.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Header de la pantalla (ver design-m02-c01.pen): eyebrow + título + subtítulo,
// compartido por MVC, MVP y MVVM, arriba de ProductListContent.
@Composable
fun ProductListHeader(
    loading: Boolean,
    error: String?,
    resultCount: Int,
    modifier: Modifier = Modifier
) {
    val subtitle = when {
        loading -> "Cargando catálogo..."
        error != null -> "No se pudo conectar"
        else -> "$resultCount resultados de fakestoreapi.com"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "CATÁLOGO",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Productos",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, name = "ProductListHeader - Con resultados")
@Composable
private fun ProductListHeaderPreview() {
    ProductListHeader(loading = false, error = null, resultCount = 5)
}

@Preview(showBackground = true, name = "ProductListHeader - Cargando")
@Composable
private fun ProductListHeaderLoadingPreview() {
    ProductListHeader(loading = true, error = null, resultCount = 0)
}
