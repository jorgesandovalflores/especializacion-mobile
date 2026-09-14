package com.example.example.features.mvvm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.example.core.theme.ExampleTheme

// Activity de arranque para demo MVVM
class AppMVVM : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ExampleTheme { ProductListScreenMVVM() } }
    }
}