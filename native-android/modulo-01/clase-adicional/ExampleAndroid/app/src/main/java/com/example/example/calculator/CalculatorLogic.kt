package com.example.example.calculator

/*
Lógica de la calculadora, sin Compose (Kotlin puro, fácil de leer y de
explicar en clase). Cada bloque está comentado con el número de sección
del README.md de la clase
(native-android/modulo-01/clase-adicional/README.md) que ilustra.
*/

// Sección 6 del README.md — POO en Kotlin: enum class
enum class Operator(val symbol: String) {
    SUMA("+"), RESTA("-"), MULTIPLICACION("×"), DIVISION("÷"),
}

// Sección 6 del README.md — POO en Kotlin: data class (equals/hashCode/
// toString/copy generados por el compilador)
data class Operation(
    val a: Double,
    val b: Double,
    val operator: Operator,
    val result: Double,
)

// Sección 6 del README.md — POO en Kotlin: sealed class (jerarquía cerrada,
// exhaustiva en un "when" sin necesitar "else")
sealed class CalcResult {
    data class Ok(val operation: Operation) : CalcResult()
    data class Error(val message: String) : CalcResult()
}

// Sección 2 del README.md — const val: constante fija en tiempo de compilación
private const val MAX_HISTORY = 20

// Sección 1 del README.md — Nothing: tipo sin instancias, para una función
// que nunca retorna con normalidad (aquí, error() de la stdlib es Nothing).
private fun requireNonNegativeDecimals(decimals: Int): Int =
    if (decimals >= 0) decimals else error("decimals no puede ser negativo: $decimals")

// Sección 5 del README.md — función de extensión + parámetro por defecto
fun Double.formatted(decimals: Int = 2): String =
    "%.${requireNonNegativeDecimals(decimals)}f".format(this)

// Sección 5 del README.md — función infix (extensión con un solo parámetro,
// se puede llamar como "20.0 percentOf 200.0")
infix fun Double.percentOf(total: Double): Double =
    if (total == 0.0) 0.0 else (this / total) * 100

/**
 * Sección 3 (operadores aritméticos y Elvis ?:), sección 4 (if/when como
 * expresión) y sección 8 (Null Safety) del README.md: valida los textos de
 * los inputs y calcula el resultado.
 */
fun calculate(aText: String, bText: String, operator: Operator): CalcResult {
    // Sección 8 — Null Safety: toDoubleOrNull() devuelve null en vez de
    // lanzar una excepción; el Elvis (?:) resuelve el caso nulo al toque.
    val a = aText.toDoubleOrNull() ?: return CalcResult.Error("\"$aText\" no es un número")
    val b = bText.toDoubleOrNull() ?: return CalcResult.Error("\"$bText\" no es un número")

    // Sección 4 — when como expresión: reemplaza un switch, y al ser sobre
    // un enum, el compilador exige que estén todos los casos (exhaustivo).
    val result = when (operator) {
        Operator.SUMA -> a + b
        Operator.RESTA -> a - b
        Operator.MULTIPLICACION -> a * b
        Operator.DIVISION ->
            // Sección 4 — if como expresión
            if (b == 0.0) return CalcResult.Error("No se puede dividir entre 0") else a / b
    }

    return CalcResult.Ok(Operation(a, b, operator, result))
}

// Sección 2 del README.md — scope function "run": agrupa código y devuelve
// un valor, aquí para limitar el historial a MAX_HISTORY elementos.
fun trimHistory(history: List<Operation>): List<Operation> = run {
    if (history.size > MAX_HISTORY) history.takeLast(MAX_HISTORY) else history
}

// Sección 7 del README.md — Colecciones y orden superior: sumOf/filter/map
fun historySummary(history: List<Operation>): String {
    val total = history.sumOf { it.result }
    val divisions = history.filter { it.operator == Operator.DIVISION }.size
    val labels = history.map { "${it.a.formatted(0)}${it.operator.symbol}${it.b.formatted(0)}" }
    val last = if (labels.isEmpty()) "" else " · última: ${labels.last()}"
    return "Operaciones: ${history.size} · divisiones: $divisions · acumulado: ${total.formatted()}$last"
}

// Sección 10 del README.md — Interoperabilidad con Java: así se expone un
// método utilitario para que un módulo Java lo llame como estático, por
// ejemplo CalcFormatter.percent(20.0, 200.0) o CalcFormatter.percent(20.0, 200.0, 0).
object CalcFormatter {
    @JvmStatic
    @JvmOverloads
    fun percent(value: Double, total: Double, decimals: Int = 1): String =
        (value percentOf total).formatted(decimals) + "%"
}
