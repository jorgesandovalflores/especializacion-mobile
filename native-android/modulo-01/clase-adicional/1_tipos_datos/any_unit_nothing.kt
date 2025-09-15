fun returnsUnit(): Unit {
    // hacer algo y no retornar valor
}

fun fail(message: String): Nothing {  // nunca retorna
    throw IllegalStateException(message)
}
