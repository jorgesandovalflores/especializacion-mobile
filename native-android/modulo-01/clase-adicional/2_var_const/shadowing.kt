// Shadowing (sombras de variables)
//Puedes redefinir una variable en un scope más interno, ocultando la externa.

fun shadowingExample() {
    val number = 10
    println(number)     // 10

    run {
        val number = 20 // shadowing de la variable externa
        println(number) // 20
    }

    println(number)     // 10
}