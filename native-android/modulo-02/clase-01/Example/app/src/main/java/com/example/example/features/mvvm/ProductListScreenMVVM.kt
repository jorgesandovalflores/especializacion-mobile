package com.example.example.features.mvvm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// View (Compose) en MVVM: observa el StateFlow del ViewModel. Esta pantalla
// es también el ejemplo en vivo de "ViewModel, State, Flow y LiveData"
// (ver viewmodel-flow-state-livedata.md):
//   - ViewModel  -> vm, creado con una Factory sin reflexión.
//   - Flow       -> vm.ui (StateFlow), recolectado con collectAsStateWithLifecycle().
//   - LiveData   -> vm.lastUpdatedAt, observado con observeAsState().
//   - State      -> onlyInStock, remember { mutableStateOf(...) } local a la UI.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreenMVVM(
    vm: ProductListViewModel = viewModel(factory = ProductListViewModel.Factory)
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val lastUpdatedAt by vm.lastUpdatedAt.observeAsState()
    val retryCount by vm.retryCount.collectAsStateWithLifecycle()

    // State de Compose: filtro efímero de UI, no vive en el ViewModel ni sobrevive rotación
    var onlyInStock by remember { mutableStateOf(false) }
    val visibleProducts = remember(ui.data, onlyInStock) {
        if (onlyInStock) ui.data.filter { it.inStock } else ui.data
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Products (MVVM)") }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                ui.loading -> Box(Modifier.fillMaxSize()) { CircularProgressIndicator() }
                ui.error != null -> Column(Modifier.padding(16.dp)) {
                    Text("Error: ${ui.error}")
                    Text("Reintentos: $retryCount")
                    Button(onClick = { vm.retry() }) { Text("Reintentar") }
                }
                else -> Column(Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = onlyInStock, onCheckedChange = { onlyInStock = it })
                        Text("Solo en stock (State local)")
                        Spacer(Modifier.weight(1f))
                        lastUpdatedAt?.let { Text(formatUpdatedAt(it), style = MaterialTheme.typography.labelSmall) }
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(visibleProducts, key = { it.id }) { p ->
                            ElevatedCard(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(p.name, style = MaterialTheme.typography.titleMedium)
                                    Text("$${p.price}")
                                    Text(if (p.inStock) "In stock" else "Out of stock")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Formatea el timestamp del LiveData solo para mostrarlo en pantalla
private fun formatUpdatedAt(epochMillis: Long): String {
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return "Actualizado: ${formatter.format(Date(epochMillis))}"
}
