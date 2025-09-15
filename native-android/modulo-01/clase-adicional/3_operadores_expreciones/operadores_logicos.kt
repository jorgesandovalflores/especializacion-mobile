/*
| Operador | Descripción | Ejemplo          |           |         |   |         |
| -------- | ----------- | ---------------- | --------- | ------- | - | ------- |
| `&&`     | AND lógico  | `a > 0 && b > 0` |           |         |   |         |
| \`       |             | \`               | OR lógico | \`a > 0 |   | b > 0\` |
| `!`      | Negación    | `!(a > 0)`       |           |         |   |         |
*/

fun logical() {
    val x = 10
    val y = -5
    println(x > 0 && y > 0) // false
    println(x > 0 || y > 0) // true
    println(!(x > 0))       // false
}