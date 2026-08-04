package com.example.demo_mvvm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.demo_mvvm.home.HomeRoute
import com.example.demo_mvvm.ui.theme.DemomvvmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DemomvvmTheme {
                HomeRoute()
            }
        }
    }
}
