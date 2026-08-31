/*
val (inmutable)
Una vez asignado, un "val" no puede reasignarse (equivale a una
referencia "final" de Java). Sí puede declararse sin valor inicial y
asignarse una única vez más adelante (inicialización diferida),
mientras el compilador pueda garantizar que ocurre antes de su uso.
*/

fun immutableVals() {
    val pi = 3.1416       // tipo inferido: Double
    println(pi)

    // pi = 3.14          // Error: reassignment not allowed

    val name: String
    name = "Kotlin"       // permitido: inicialización diferida
    println(name)
}
