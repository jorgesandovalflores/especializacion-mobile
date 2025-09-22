package com.example.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // configuracion
        /*setContent {
            com.example.example.configuracion.AppNavRoot()
        }*/

        // rutas y argumentos
        /*setContent {
            com.example.example.rutasargumentos.AppNavRoot()
        }*/

        // navegacion anidada
        /*setContent {
            com.example.example.anidada.AppNavRoot()
        }*/

        // navegacion viewmodel
        /*setContent {
            com.example.example.viewmodel.AppNavRoot()
        }*/

        // navegacion y resultados
        /*setContent {
            com.example.example.argumentos.AppNavRoot()
        }*/

        // backstack
        /*setContent {
            com.example.example.backstack.AppNavRoot()
        }*/

        // deeplink
        setContent {
            com.example.example.deeplink.AppNavRoot()
        }

    }
}