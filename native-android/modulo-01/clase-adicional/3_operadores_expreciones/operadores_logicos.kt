/*
Operadores lógicos
&&  AND lógico  → a > 0 && b > 0
||  OR lógico   → a > 0 || b > 0
!   Negación    → !(a > 0)

Igual que en Java, && y || son "short-circuit": si el primer operando
ya decide el resultado, el segundo ni siquiera se evalúa.
*/

fun logical() {
    val x = 10
    val y = -5
    println(x > 0 && y > 0) // false
    println(x > 0 || y > 0) // true
    println(!(x > 0))       // false
}