package com.example.exampleandroid.login

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.exampleandroid.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

private val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
private const val PASSWORD_MIN_LENGTH = 6
private const val SIMULATED_NETWORK_DELAY_MS = 900L

class LoginXml : AppCompatActivity() {

    // Comentario (ES): en el mundo imperativo el "estado" vive en variables sueltas
    // de la Activity; nadie observa estos cambios automáticamente.
    private var isLoading: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_login)

        val tilEmail = findViewById<TextInputLayout>(R.id.tilEmail)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilPassword)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val progressLogin = findViewById<ProgressBar>(R.id.progressLogin)
        val tvLoginResult = findViewById<TextView>(R.id.tvLoginResult)

        // Comentario (ES): cada tecla que el usuario escribe dispara este callback;
        // nosotros somos responsables de leer el nuevo valor y mutar la UI a mano.
        fun updateFormState() {
            val email = etEmail.text?.toString().orEmpty()
            val password = etPassword.text?.toString().orEmpty()

            val emailValid = EMAIL_PATTERN.matches(email)
            val passwordValid = password.length >= PASSWORD_MIN_LENGTH

            tilEmail.error = if (email.isNotEmpty() && !emailValid) {
                "Ingresa un email válido"
            } else null

            tilPassword.error = if (password.isNotEmpty() && !passwordValid) {
                "Mínimo $PASSWORD_MIN_LENGTH caracteres"
            } else null

            btnLogin.isEnabled = emailValid && passwordValid && !isLoading
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) = updateFormState()
        }
        etEmail.addTextChangedListener(watcher)
        etPassword.addTextChangedListener(watcher)

        btnLogin.setOnClickListener {
            isLoading = true
            btnLogin.isEnabled = false
            progressLogin.visibility = View.VISIBLE
            tvLoginResult.text = ""

            // Comentario (ES): sin coroutines todavía (se ven más adelante en el curso);
            // simulamos la llamada de red con un Handler y actualizamos la vista "a mano"
            // cuando la respuesta llega.
            Handler(Looper.getMainLooper()).postDelayed({
                isLoading = false
                progressLogin.visibility = View.GONE
                tvLoginResult.text = "Bienvenido, ${etEmail.text}"
                updateFormState()
            }, SIMULATED_NETWORK_DELAY_MS)
        }

        updateFormState()
    }
}
