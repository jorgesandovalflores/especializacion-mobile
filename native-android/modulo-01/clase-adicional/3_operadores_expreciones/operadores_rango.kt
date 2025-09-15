/*
6) Operadores de rango
..: rango inclusivo (Incluye el inicio y el fin.)
until: rango exclusivo (Incluye el inicio, pero no el fin.)
downTo: rango decreciente (Cuenta hacia atrás desde el inicio hasta el fin.)
step: saltos (Permite avanzar de más de uno en uno.)
*/

fun ranges() {
    for (i in 1..5) println(i)       // 1,2,3,4,5
    for (i in 1 until 5) println(i)  // 1,2,3,4
    for (i in 5 downTo 1 step 2) println(i) // 5,3,1
}
