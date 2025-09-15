fun nullSafety() {
    // val s: String = null     // no compila
    val s: String? = null      // puede ser null

    // Safe call y Elvis
    val lengthOrZero = s?.length ?: 0  // si s es null → 0
    println(lengthOrZero)

    // Evita !!
    // val n = s!!.length       // puede lanzar NullPointerException si s==null
}