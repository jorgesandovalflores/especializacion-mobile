package com.example.example.pcreacionales

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