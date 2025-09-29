package com.example.android.features.travels.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.android.features.travels.domain.model.Trip

// pantalla principal del feature travel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelScreen(
    vm: TravelViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Pending trips") },
                scrollBehavior = scrollBehavior
            )
        },
        // Garantiza padding seguro bajo status/navigation bars
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when {
                ui.loading -> CircularProgressIndicator()
                ui.error != null -> Column {
                    Text(
                        "Error: ${ui.error}",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.padding(top = 8.dp))
                }
                else -> TripList(trips = ui.items)
            }
        }
    }
}

@Composable
private fun TripList(trips: List<Trip>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(
            items = trips,
            key = { it.id }
        ) { trip ->
            TripRow(trip)
            Divider()
        }
    }
}

@Composable
private fun TripRow(trip: Trip) {
    Column(Modifier.fillMaxWidth().padding(8.dp)) {
        Text(trip.passengerName, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text("From: ${trip.pickupAddress}")
        Text("To:   ${trip.dropoffAddress}")
        Spacer(Modifier.height(4.dp))
        Text("Requested: ${trip.requestedAtIso}", style = MaterialTheme.typography.bodySmall)
    }
}