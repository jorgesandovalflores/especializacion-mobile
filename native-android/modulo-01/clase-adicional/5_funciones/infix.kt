/*
6) Funciones infix
Permiten llamadas con sintaxis “natural”.
Deben ser extensiones o miembros.
Deben tener un solo parámetro.
*/

infix fun Int.times(str: String): String = str.repeat(this)

fun main() {
    println(3 times "Hi ") // Hi Hi Hi
}

infix fun <A, B> A.toPair(that: B): Pair<A, B> = Pair(this, that)

fun main() {
    val p = "Key" toPair 123
    println(p) // (Key, 123)
}
