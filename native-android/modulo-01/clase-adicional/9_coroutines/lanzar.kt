/*
Lanzar una Coroutine
Las funciones principales son:
launch { } → no devuelve valor (similar a fire and forget).
async { } → devuelve un Deferred<T> (promesa).
*/

import kotlinx.coroutines.*

fun main() = runBlocking {
    launch {
        delay(1000L)
        println("World!")
    }
    println("Hello,")
}


/*
Builders: runBlocking, launch, async
*/
fun main() = runBlocking {
    // launch: no devuelve nada
    launch {
        delay(500L)
        println("Task 1")
    }

    // async: devuelve un valor
    val result = async {
        delay(1000L)
        42
    }

    println("Result = ${result.await()}") // 42
}

/*
Funciones suspend
Una función suspend puede suspenderse sin bloquear el hilo.
Solo puede ser llamada desde otra suspend o dentro de una coroutine.
*/

suspend fun fetchData(): String {
    delay(1000L) // simula llamada a red
    return "Data from server"
}

fun main() = runBlocking {
    println("Fetching...")
    val data = fetchData()
    println(data)
}

/*
Dispatchers (dónde corre la coroutine)
Dispatchers.Default → CPU-bound (procesamiento pesado).
Dispatchers.IO → operaciones de red/archivo.
Dispatchers.Main → UI (Android).
*/

fun main() = runBlocking {
    launch(Dispatchers.Default) {
        println("Running on ${Thread.currentThread().name}")
    }
    launch(Dispatchers.IO) {
        println("Running on ${Thread.currentThread().name}")
    }
}

/*
Cancelación
Coroutines son cooperativas: se cancelan verificando el estado.
*/

fun main() = runBlocking {
    val job = launch {
        repeat(5) { i ->
            println("Working $i ...")
            delay(500L)
        }
    }
    delay(1200L)
    println("Cancel!")
    job.cancelAndJoin()
}

/*
Estructura jerárquica (Structured Concurrency)
Una coroutine vive dentro de un scope.
Si el scope se cancela → todas sus coroutines hijas se cancelan.
*/

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
    println("Scope finished")
}

/*
Ejemplo práctico: llamadas concurrentes
*/

suspend fun getUser(): String {
    delay(1000L)
    return "User: Ana"
}

suspend fun getPosts(): String {
    delay(2000L)
    return "Posts: [A, B, C]"
}

fun main() = runBlocking {
    val user = async { getUser() }
    val posts = async { getPosts() }

    println("${user.await()} - ${posts.await()}")
}
