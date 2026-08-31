/*
var (mutable)
A diferencia de "val", un "var" admite reasignaciones posteriores.
Kotlin infiere el tipo en la declaración inicial; ese tipo queda fijo,
solo cambia el valor (no se puede asignar un tipo distinto después).
*/

fun mutableVars() {
    var age = 25          // tipo inferido: Int
    println(age)

    age = 26              // se puede reasignar
    println(age)

    var city: String      // tipo declarado explícitamente
    city = "Lima"
    println(city)
}
