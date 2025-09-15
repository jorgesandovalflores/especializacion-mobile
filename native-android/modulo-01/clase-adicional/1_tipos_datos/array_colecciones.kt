fun collectionsBasics() {
    // List inmutable
    val names = listOf("Ana", "Luis")     // List<String>

    // Si está vacío, a veces hay que ayudar con el tipo:
    val emptyStrings = emptyList<String>() // List<String>

    // MutableList
    val scores = mutableListOf(10, 20)    // MutableList<Int>
    scores.add(30)

    // Set y Map
    val letters = setOf('A', 'B', 'A')    // Set<Char> → {A, B}
    val ages = mapOf("Ana" to 30, "Luis" to 28)  // Map<String, Int>

    println(names)
    println(scores)
    println(letters)
    println(ages["Ana"])
}
