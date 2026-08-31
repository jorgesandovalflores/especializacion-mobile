/*
6) Funciones infix
Permiten llamadas con sintaxis "natural" (sin punto ni paréntesis).
Reglas:
- Deben ser funciones de extensión o funciones miembro.
- Deben tener exactamente un parámetro (sin valor por defecto, sin vararg).
*/

infix fun Int.times(str: String): String = str.repeat(this)

infix fun <A, B> A.toPair(that: B): Pair<A, B> = Pair(this, that)

fun main() {
    println(3 times "Hi ")        // Hi Hi Hi

    val p = "Key" toPair 123
    println(p)                    // (Key, 123)
}
