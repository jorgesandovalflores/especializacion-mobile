fun greetUser(name: String = "Guest", greeting: String = "Hello") {
    println("$greeting, $name")
}

fun main() {
    greetUser()                     // Hello, Guest
    greetUser("Ana")                // Hello, Ana
    greetUser("Luis", "Welcome")    // Welcome, Luis
}