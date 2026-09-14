package pe.identity.examplem2c1.pcreacionales

class ExampleSingletonConsumer {

    val singleton: ExampleSingleton by lazy { ExampleSingleton() }

}