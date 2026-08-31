/*
Funciones de una sola expresión
Cuando el cuerpo es una única expresión, se puede omitir "{ return }"
y usar "=". El tipo de retorno puede inferirse (isEven) o declararse
explícitamente (square); ambas formas son equivalentes en tiempo de
ejecución.
*/

fun square(x: Int): Int = x * x
fun isEven(n: Int) = n % 2 == 0  // tipo inferido: Boolean