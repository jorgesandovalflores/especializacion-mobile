package com.example.example.calculator

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Sección 9 del README.md — Coroutines.
 * Simula "guardar" una operación en un servidor: una función suspend que
 * se suspende sin bloquear el hilo que la llama (en la UI, se invoca desde
 * un launch { } sobre rememberCoroutineScope, el equivalente a
 * viewModelScope/lifecycleScope en una pantalla con ViewModel).
 */
suspend fun saveOperationRemotely(operation: Operation): Boolean =
    withContext(Dispatchers.IO) { // Dispatchers.IO: para trabajo que bloquea (aquí, red)
        delay(600) // simula la latencia de la llamada de red
        true
    }
