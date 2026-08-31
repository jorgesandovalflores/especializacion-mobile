/*
Null Safety
Un tipo normal (String) nunca puede ser null; para permitirlo hay que
marcarlo explícitamente con "?" (String?). El compilador obliga a
manejar el caso nulo antes de usar el valor: con "?." (safe call),
"?:" (Elvis, valor por defecto) o "!!" (fuerza el acceso y lanza
NullPointerException si es null; evitar salvo certeza absoluta).
*/

fun nullSafety() {
    // val s: String = null     // no compila
    val s: String? = null      // puede ser null

    // Safe call y Elvis
    val lengthOrZero = s?.length ?: 0  // si s es null → 0
    println(lengthOrZero)

    // Evita !!
    // val n = s!!.length       // puede lanzar NullPointerException si s==null
}