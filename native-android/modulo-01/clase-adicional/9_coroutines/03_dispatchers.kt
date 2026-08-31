/*
3) Dispatchers (en qué hilo corre la coroutine)
- Dispatchers.Default → trabajo CPU-bound (cálculos, parsing pesado).
- Dispatchers.IO      → operaciones de I/O que bloquean (red, disco, DB).
- Dispatchers.Main    → hilo de UI (solo disponible en Android/UI apps;
                        requiere el artefacto kotlinx-coroutines-android,
                        por eso no se usa en este ejemplo de consola).
*/

import kotlinx.coroutines.*

fun main() = runBlocking {
    launch(Dispatchers.Default) {
        println("Default -> running on ${Thread.currentThread().name}")
    }
    launch(Dispatchers.IO) {
        println("IO -> running on ${Thread.currentThread().name}")
    }
}
