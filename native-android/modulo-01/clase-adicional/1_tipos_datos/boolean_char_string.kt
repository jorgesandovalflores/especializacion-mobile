/*
Boolean, Char y String
- Char usa comillas simples ('A'); String usa comillas dobles ("Kotlin").
- Los templates de string ($variable, ${expresion}) evitan la
  concatenación manual con "+".
- Las strings multilínea ("""...""") preservan saltos de línea y
  admiten trimIndent() para quitar la indentación del código fuente.
*/

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
