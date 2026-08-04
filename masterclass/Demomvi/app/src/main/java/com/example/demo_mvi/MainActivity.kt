package com.example.demo_mvi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.demo_mvi.home.HomeRoute
import com.example.demo_mvi.ui.theme.DemomviTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DemomviTheme {
                HomeRoute()
            }
        }
    }
}
