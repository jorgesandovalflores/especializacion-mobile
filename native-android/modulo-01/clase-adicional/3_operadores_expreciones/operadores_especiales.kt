/*
8) Kotlin como lenguaje de expresiones
if, when e incluso try/catch pueden usarse como expresión: en vez de
solo ejecutar código, devuelven un valor que se puede asignar
directamente a un val/var o retornar.
*/

// If como expresión
fun ifExpression(a: Int, b: Int): Int {
    val max = if (a > b) a else b
    return max
}

// When como expresión
fun whenExpression(x: Int): String {
    return when (x) {
        1 -> "Uno"
        2 -> "Dos"
        in 3..5 -> "Entre 3 y 5"
        else -> "Otro"
    }
}

// Try-catch como expresión
fun parseIntOrZero(input: String): Int {
    val result = try {
        input.toInt()
    } catch (e: NumberFormatException) {
        0
    }
    return result
}
