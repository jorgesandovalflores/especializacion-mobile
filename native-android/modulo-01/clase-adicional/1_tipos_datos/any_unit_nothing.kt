/*
Tipos especiales: Any, Unit y Nothing
- Unit: equivalente a "void", indica que la función no devuelve nada
  útil (se puede omitir, el compilador lo infiere).
- Nothing: tipo sin instancias; indica que la función nunca retorna
  normalmente (siempre lanza una excepción o entra en bucle infinito).
  Útil para que el compilador haga smart cast en el código que sigue.
*/

fun returnsUnit(): Unit {
    // hacer algo y no retornar valor
}

fun fail(message: String): Nothing {  // nunca retorna
    throw IllegalStateException(message)
}
