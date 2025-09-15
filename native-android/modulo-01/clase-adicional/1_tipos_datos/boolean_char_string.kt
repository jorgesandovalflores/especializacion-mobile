fun booleansCharsStrings() {
    // Boolean
    val isActive = true       // Boolean

    // Char vs String
    val letter: Char = 'A'    // Char
    val name: String = "Kotlin"  // String

    // Templates de strings
    val version = 2
    val msg = "Hello, $name v$version"  // Interpolación

    // String multilínea (raw)
    val sql = """
        SELECT *
        FROM users
        WHERE active = 1
    """.trimIndent()

    println("$isActive | $letter | $msg")
    println(sql)
}
