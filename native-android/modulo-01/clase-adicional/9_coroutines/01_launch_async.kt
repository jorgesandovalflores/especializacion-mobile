/*
1) Builders: launch y async
Una coroutine se lanza con un "builder". Los dos más usados:
- launch { }  → fire and forget, no devuelve valor, retorna un Job.
- async { }   → devuelve un Deferred<T> (una "promesa"); su valor se
                obtiene con .await().

runBlocking { } bloquea el hilo actual hasta que su bloque termina.
Se usa para probar coroutines desde una función main() normal; en
Android real se usa lifecycleScope/viewModelScope en su lugar.
*/

import kotlinx.coroutines.*

fun main() = runBlocking {
    // launch: no devuelve nada, corre "en paralelo" dentro del scope
    launch {
        delay(500L)
        println("Task 1")
    }

    // async: devuelve un Deferred<Int>, su resultado se espera con await()
    val result = async {
        delay(1000L)
        42
    }

    println("Result = ${result.await()}") // 42
}
