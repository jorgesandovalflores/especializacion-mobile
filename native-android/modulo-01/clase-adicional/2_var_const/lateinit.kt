lateinit var session: String

fun startSession() {
    session = "User123"    // inicialización tardía
    println(session)
}
