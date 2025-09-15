// a) For con rangos
fun forRange() {
    for (i in 1..5) {
        println(i)  // 1,2,3,4,5
    }
    for (i in 1 until 5) {
        println(i)  // 1,2,3,4
    }
    for (i in 10 downTo 1 step 3) {
        println(i)  // 10,7,4,1
    }
}

// b) For con colecciones
fun forCollections() {
    val names = listOf("Ana", "Luis", "Marta")
    for (name in names) {
        println(name)
    }

    // Con índices
    for ((index, value) in names.withIndex()) {
        println("[$index] $value")
    }
}

// c) While y Do-While
fun whileLoops() {
    var n = 3
    while (n > 0) {
        println("n = $n")
        n--
    }

    var m = 0
    do {
        println("m = $m")
        m++
    } while (m < 3)
}
