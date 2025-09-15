// Scope functions (let, run, apply, also, with)
// Kotlin tiene funciones que crean subscopes útiles para manipular objetos:

fun scopeFunctions() {
    val name: String? = "Kotlin"

    // let crea un scope solo si no es null
    name?.let {
        println("Length: ${it.length}") // aquí 'it' = name
    }

    val list = mutableListOf(1, 2, 3).apply {
        add(4)
        add(5)
    }
    println(list) // [1, 2, 3, 4, 5]
}
