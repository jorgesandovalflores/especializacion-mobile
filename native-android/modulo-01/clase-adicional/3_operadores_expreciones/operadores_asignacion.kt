/*
| Operador | Ejemplo  | Equivale a  |
| -------- | -------- | ----------- |
| `+=`     | `x += 5` | `x = x + 5` |
| `-=`     | `x -= 2` | `x = x - 2` |
| `*=`     | `x *= 3` | `x = x * 3` |
| `/=`     | `x /= 2` | `x = x / 2` |
 */

fun assignment() {
    var x = 10
    x += 5    // 15
    x *= 2    // 30
    println(x)
}
