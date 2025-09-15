fun createUser(name: String, age: Int, isActive: Boolean) {
    println("User: $name, Age: $age, Active: $isActive")
}

fun main() {
    createUser("Ana", 30, true)  // orden normal
    createUser(age = 25, name = "Luis", isActive = false) // parámetros nombrados
}