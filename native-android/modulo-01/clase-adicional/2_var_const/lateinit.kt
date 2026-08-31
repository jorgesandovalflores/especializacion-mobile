/*
lateinit var
Permite declarar una propiedad "var" no nula sin inicializarla de
inmediato, prometiendo asignarle un valor antes de usarla (típico en
inyección de dependencias o en onCreate() de un Activity). Solo
aplica a "var" (no "val") y a tipos no primitivos; acceder antes de
inicializar lanza UninitializedPropertyAccessException.
*/

lateinit var session: String

fun startSession() {
    session = "User123"    // inicialización tardía
    println(session)
}
