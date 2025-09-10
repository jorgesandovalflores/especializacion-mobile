package com.example.exampleandroid.declaracion

import com.example.exampleandroid.R
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DeclaracionXml : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_declaracion)
        val title = findViewById<TextView>(R.id.tvTitle)
        val btn = findViewById<Button>(R.id.btnTap)
        title.text = "Hello Compose!"
        btn.setOnClickListener { Log.d("Session02", "Tap") }
    }
}