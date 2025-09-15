fun mutableVars() {
    var age = 25          // tipo inferido: Int
    println(age)

    age = 26              // se puede reasignar
    println(age)

    var city: String      // tipo declarado explícitamente
    city = "Lima"
    println(city)
}
