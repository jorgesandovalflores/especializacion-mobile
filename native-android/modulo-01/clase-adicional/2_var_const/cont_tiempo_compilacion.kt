/*
Constantes en tiempo de compilación (const val)
"const val" solo se puede usar a nivel top-level o dentro de un
"object", y solo con tipos primitivos o String cuyo valor se conoce
en tiempo de compilación. Se diferencia de "val" (que puede asignarse
en tiempo de ejecución) en que su valor queda inlineado por el
compilador, como un "static final" de Java.
*/

const val API_URL = "https://api.example.com"
const val MAX_USERS = 100

object Config {
    const val TIMEOUT = 5000
}