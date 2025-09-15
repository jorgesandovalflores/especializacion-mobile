/*
7) Operador in
Verifica pertenencia en rangos o colecciones.
*/

fun inOperator() {
    val x = 3
    println(x in 1..5)              // true
    println(x !in listOf(1, 2, 4))  // true
}
