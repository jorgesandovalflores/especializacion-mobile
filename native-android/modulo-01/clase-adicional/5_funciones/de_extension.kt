/*
Funciones de extensión
Agregan un método nuevo a un tipo existente (aquí, String) sin
heredar de él ni modificar su código fuente. Dentro del cuerpo,
"this" hace referencia al receptor (el String sobre el que se llama).
Se resuelven en tiempo de compilación (no son polimórficas).
*/

fun String.lastChar(): Char = this[this.length - 1]

fun main() {
    println("Kotlin".lastChar()) // n
}
