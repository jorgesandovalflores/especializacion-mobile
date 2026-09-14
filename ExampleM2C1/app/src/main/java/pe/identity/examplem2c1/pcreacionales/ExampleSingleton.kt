package pe.identity.examplem2c1.pcreacionales

class ExampleSingleton {
    companion object {
        private var instance: ExampleSingleton? = null

        fun getInstance(): ExampleSingleton {
            if (instance == null) {
                instance = ExampleSingleton()
            }
            return instance!!
        }
    }

}