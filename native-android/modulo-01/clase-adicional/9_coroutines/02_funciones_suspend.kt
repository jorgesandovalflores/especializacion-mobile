/*
2) Funciones suspend
Una función marcada con "suspend" puede suspenderse (pausar) sin
bloquear el hilo que la ejecuta. Solo puede llamarse desde:
- otra función suspend, o
- dentro de una coroutine (launch/async/runBlocking, etc.).
*/

import kotlinx.coroutines.*

suspend fun fetchData(): String {
    delay(1000L) // simula una llamada de red
    return "Data from server"
}

fun main() = runBlocking {
    println("Fetching...")
    val data = fetchData()
    println(data)
}
