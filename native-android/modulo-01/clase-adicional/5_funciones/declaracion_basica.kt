/*
Declaración básica de funciones
Se declaran con "fun", nombre, parámetros tipados y un tipo de
retorno explícito (aquí, String). El cuerpo entre llaves usa "return"
para devolver el valor, igual que en Java.
*/

fun greet(name: String): String {
    return "Hello, $name!"
}

fun main() {
    println(greet("Kotlin")) // Hello, Kotlin!
}
