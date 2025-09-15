fun smartCastsDemo(value: Any) {
    // 'is' verifica tipo, Kotlin smart-castea dentro del bloque
    if (value is String) {
        println(value.length) // aquí es String
    } else if (value is Int) {
        println(value + 1)    // aquí es Int
    }

    // 'when' exhaustivo (útil con sealed classes también)
    when (value) {
        is String -> println(value.uppercase())
        is Int -> println(value * 2)
        else -> println("Unknown")
    }
}
