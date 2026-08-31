/*
Parámetros nombrados (named arguments)
Permiten pasar argumentos indicando su nombre, sin depender del orden
de la firma. Muy útil cuando una función tiene varios parámetros del
mismo tipo (aquí, dos String y un Boolean) donde el orden es fácil de
confundir.
*/

fun createUser(name: String, age: Int, isActive: Boolean) {
    println("User: $name, Age: $age, Active: $isActive")
}

fun main() {
    createUser("Ana", 30, true)                            // orden posicional normal
    createUser(age = 25, name = "Luis", isActive = false)  // parámetros nombrados: el orden ya no importa
}
