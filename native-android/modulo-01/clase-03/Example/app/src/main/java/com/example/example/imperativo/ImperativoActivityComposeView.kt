package com.example.example.imperativo

// Imports clave
import com.example.example.R
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy

class ImperativoActivityComposeView : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // usamos el layout XML clásico
        setContentView(R.layout.layout_imperativo_compose_view)

        // referencia al ComposeView del XML
        val composeView = findViewById<ComposeView>(R.id.composeContainer)

        // estrategia de ciclo de vida para evitar fugas de memoria
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindow
        )

        // montamos contenido Compose dentro del ComposeView
        composeView.setContent {
            MaterialTheme {
                UserBadge(
                    name = "Alex Johnson",
                    onLogout = {
                        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show()
                        performLogout()
                    }
                )
            }
        }
    }

    private fun performLogout() {
        finish()
    }
}
