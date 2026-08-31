/*
5) Structured concurrency (concurrencia estructurada)
Toda coroutine vive dentro de un scope (ámbito). coroutineScope { }
crea un scope hijo que espera a que TODAS sus coroutines internas
terminen antes de continuar; si una falla o el scope se cancela,
sus hijas se cancelan con él. Esto evita "coroutines huérfanas".
*/

import kotlinx.coroutines.*

fun main() = runBlocking {
    coroutineScope {
        launch {
            delay(1000L)
            println("Task 1 finished")
        }
        launch {
            delay(2000L)
            println("Task 2 finished")
        }
    }
    println("Scope finished") // se imprime solo cuando ambas tareas terminaron
}
