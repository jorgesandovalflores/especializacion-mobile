/*
Tipos numéricos
Int, Long, Float y Double son los más usados. A diferencia de Java,
Kotlin no hace conversiones implícitas entre tipos numéricos (ni
siquiera de Int a Long): siempre hay que convertir explícitamente
con toLong(), toDouble(), etc.
*/

fun numbersBasics() {
    // Int por defecto
    val a = 10              // Int
    val b = 10L             // Long
    val c = 10.0            // Double
    val d = 10f             // Float

    // Conversión explícita
    val x: Long = a.toLong()   // conversión necesaria
    // val y: Long = a          // error: requiere toLong()

    println("$a | $b | $c | $d | $x")
}