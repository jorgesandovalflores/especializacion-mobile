// Inferencia local (variables)
val meaning = 42            // Int
var title = "Intro"         // String (mutable)

// Función con cuerpo expresión (retorno inferido)
fun add(a: Int, b: Int) = a + b  // Int

// Lambdas con inferencia por contexto
fun lengths(xs: List<String>): List<Int> =
    xs.map { it.length }  // 'it' es String, retorno Int

// Mejor práctica en API pública: anotar retorno
public fun publicSum(a: Int, b: Int): Int {
    // ... lógica
    return a + b
}