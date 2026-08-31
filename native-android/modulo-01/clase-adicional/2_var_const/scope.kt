/*
Scope (alcance) de variables y visibilidad
Dónde se declara algo determina desde dónde es visible: dentro de una
función (local), como propiedad de una clase (instancia), o a nivel
de archivo/top-level (accesible desde todo el módulo, sin necesidad
de una clase contenedora, algo que Java no permite).
*/

// a) Scope de función/local
fun localScope() {
    val x = 10   // solo visible aquí
    println(x)
}
// println(x)  // ❌ no visible fuera

// b) Scope de clase
class Person {
    var name: String = "No name"   // propiedad de instancia
    fun showName() {
        println(name)  // accesible dentro de métodos
    }
}

// c) Scope global / top-level
val appName = "MyApp"   // accesible desde cualquier archivo del mismo módulo
