/*
| Operador  | Ejemplo            | Resultado                 |
| --------- | ------------------ | ------------------------- |
| `==`      | `a == b`           | igualdad estructural      |
| `!=`      | `a != b`           | desigualdad               |
| `<` `>`   | `a < b`, `a > b`   | menor/mayor               |
| `<=` `>=` | `a <= b`, `a >= b` | menor/igual o mayor/igual |
 */

fun comparisons() {
    val a = 5
    val b = 7
    println(a == b)  // false
    println(a != b)  // true
    println(a < b)   // true
}