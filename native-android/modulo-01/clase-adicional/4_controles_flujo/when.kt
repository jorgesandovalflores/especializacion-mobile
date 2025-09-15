/*
2) When
when es el equivalente a switch en otros lenguajes, pero más poderoso.
*/

fun whenExample(x: Any) {
    when (x) {
        1 -> println("Uno")
        2, 3 -> println("Dos o tres")
        in 4..10 -> println("Entre 4 y 10")
        is String -> println("Cadena: $x")
        else -> println("Otro valor")
    }

    // When como expresión
    val text = when (x) {
        0 -> "Cero"
        in 1..5 -> "Pequeño"
        is String -> "Texto con ${x.length} caracteres"
        else -> "Desconocido"
    }
    println(text)
}
