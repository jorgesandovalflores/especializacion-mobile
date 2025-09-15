/*
Operadores de incremento/decremento
Prefijo: ++x, --x → primero cambia, luego usa valor.
Postfijo: x++, x-- → primero usa valor, luego cambia.
 */
fun incDec() {
    var n = 5
    println(++n)  // 6 (incrementa y luego usa)
    println(n++)  // 6 (usa y luego incrementa a 7)
    println(n)    // 7
}