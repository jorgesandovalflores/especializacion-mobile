/*
4) Cancelación
Las coroutines son cooperativas: no se detienen "a la fuerza", sino que
se cancelan cuando el propio código verifica el estado de cancelación
(por ejemplo, delay() y las funciones de kotlinx.coroutines ya lo hacen).
job.cancelAndJoin() cancela el Job y espera a que termine.
*/

import kotlinx.coroutines.*

fun main() = runBlocking {
    val job = launch {
        repeat(5) { i ->
            println("Working $i ...")
            delay(500L)
        }
    }
    delay(1200L)
    println("Cancel!")
    job.cancelAndJoin() // se cancela alrededor de la 3ra iteración
}
