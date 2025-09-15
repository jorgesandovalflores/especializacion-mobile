/*
1) If / Else
En Kotlin, if es una expresión, no solo una sentencia.
Devuelve un valor, igual que when o try.
*/

fun ifExample(a: Int, b: Int) {
    // Uso clásico
    if (a > b) {
        println("a es mayor")
    } else {
        println("b es mayor o igual")
    }

    // If como expresión
    val max = if (a > b) a else b
    println("El mayor es $max")

    // If con múltiples ramas
    val result = if (a > b) {
        println("a mayor")
        a
    } else if (a == b) {
        println("iguales")
        a
    } else {
        println("b mayor")
        b
    }
    println("Resultado: $result")
}