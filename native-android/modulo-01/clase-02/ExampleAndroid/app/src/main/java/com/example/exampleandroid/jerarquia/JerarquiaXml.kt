package com.example.exampleandroid.jerarquia

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.exampleandroid.R
import android.widget.TextView
import com.google.android.material.button.MaterialButton

class JerarquiaXml : AppCompatActivity() {

    // Comentario (ES): "Estado" imperativo que controla el texto mostrado
    private var count: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        // Comentario (ES): Restaurar estado tras rotación si aplica
        if (savedInstanceState != null) {
            count = savedInstanceState.getInt(KEY_COUNT, 0)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_jerarquia) // <-- enlaza el XML

        // Comentario: Referencias a vistas
        val tvCount = findViewById<TextView>(R.id.tvCount)
        val btnIncrement = findViewById<MaterialButton>(R.id.btnIncrement)
        val btnReset = findViewById<MaterialButton>(R.id.btnReset)

        // Comentario: Pintar el estado inicial
        renderCount(tvCount)

        // Comentario: “Eventos” que mutan el estado y actualizan la UI
        btnIncrement.setOnClickListener {
            count++
            renderCount(tvCount) // <-- “recomposición” manual: actualizas la vista dependiente
        }

        btnReset.setOnClickListener {
            count = 0
            renderCount(tvCount)
        }
    }

    // Comentario: Persistencia del "estado" básico entre recreaciones de Activity
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_COUNT, count)
        super.onSaveInstanceState(outState)
    }

    // Comentario: Renderiza el valor del contador en la vista destino
    private fun renderCount(tvCount: TextView) {
        tvCount.text = "Count: $count"
    }

    companion object {
        private const val KEY_COUNT = "count_key"
    }
}
