/*
6) Ejemplo práctico: llamadas concurrentes
Dos llamadas suspend independientes (por ejemplo, a dos endpoints)
se lanzan en paralelo con async y se combinan con await(). El tiempo
total es ~2s (el máximo de las dos), no 3s (la suma), porque corren
al mismo tiempo.
*/

import kotlinx.coroutines.*

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
