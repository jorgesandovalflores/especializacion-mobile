/*
Parámetros con valor por defecto
Permiten omitir argumentos al llamar la función; si no se pasan, se
usa el valor indicado en la firma. Reduce la necesidad de sobrecargas
(overloads) que en Java se usarían para cubrir los mismos casos.
*/

fun greetUser(name: String = "Guest", greeting: String = "Hello") {
    println("$greeting, $name")
}

fun main() {
    greetUser()                     // Hello, Guest
    greetUser("Ana")                // Hello, Ana
    greetUser("Luis", "Welcome")    // Welcome, Luis
}