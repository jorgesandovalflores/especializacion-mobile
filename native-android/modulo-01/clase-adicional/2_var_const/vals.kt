fun immutableVals() {
    val pi = 3.1416       // tipo inferido: Double
    println(pi)

    // pi = 3.14          // Error: reassignment not allowed

    val name: String
    name = "Kotlin"       // permitido: inicialización diferida
    println(name)
}
