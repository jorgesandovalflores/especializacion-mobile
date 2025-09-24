package com.example.example.features.mvc

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge

// Activity de arranque para demo MVC
class AppMVC : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val controller = ProductController()
        setContent { ProductListScreenMVC(controller) }
    }
}