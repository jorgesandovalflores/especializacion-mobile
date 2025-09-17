package com.example.example.declarativo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.example.declarativo.theme.ExampleTheme

class DeclarativoActivityViewModel : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreenViewModel(
                        modifier = Modifier.padding(innerPadding),
                        onSuccess = {

                        }
                    )
                }
            }
        }
    }

}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: Boolean = false,
    val passwordError: Boolean = false,
    val canSubmit: Boolean = false, // derivado
    val submitting: Boolean = false,
    val errorMessage: String? = null
)

class LoginViewModel : ViewModel() {

    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _ui.update { prev ->
            val emailError = newEmail.isNotEmpty() && !isValidEmail(newEmail)
            val canSubmit = !emailError && !prev.passwordError &&
                    newEmail.isNotBlank() && prev.password.length >= 6
            prev.copy(
                email = newEmail,
                emailError = emailError,
                canSubmit = canSubmit,
                errorMessage = null
            )
        }
    }

    fun onPasswordChange(newPass: String) {
        _ui.update { prev ->
            val pwdError = newPass.isNotEmpty() && newPass.length < 6
            val canSubmit = !prev.emailError && !pwdError &&
                    prev.email.isNotBlank() && newPass.length >= 6
            prev.copy(
                password = newPass,
                passwordError = pwdError,
                canSubmit = canSubmit,
                errorMessage = null
            )
        }
    }

    fun submit(onSuccess: () -> Unit = {}) {
        // Simula login async y manejo de errores
        viewModelScope.launch {
            _ui.update { it.copy(submitting = true, errorMessage = null) }
            delay(1200)
            val ok = _ui.value.email.endsWith("@example.com")
            _ui.update {
                if (ok) it.copy(submitting = false, errorMessage = null)
                else it.copy(submitting = false, errorMessage = "Invalid credentials")
            }
            if (ok) onSuccess()
        }
    }
}

private fun isValidEmail(email: String): Boolean =
    android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

@Composable
fun LoginScreenViewModel(
    modifier: Modifier = Modifier,
    vm: LoginViewModel = viewModel(),
    onSuccess: () -> Unit = {}
) {
    val ui = vm.ui.collectAsState().value
    // Si tienes lifecycle-runtime-compose, puedes usar collectAsStateWithLifecycle()

    Column (
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EmailField(
            value = ui.email,
            onValueChange = vm::onEmailChange,
            isError = ui.emailError
        )
        PasswordField(
            value = ui.password,
            onValueChange = vm::onPasswordChange,
            isError = ui.passwordError
        )

        if (ui.errorMessage != null) {
            Text(ui.errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = { vm.submit(onSuccess) },
            enabled = ui.canSubmit && !ui.submitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (ui.submitting) "Submitting..." else "Login")
        }
    }
}

@Composable
private fun EmailField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Email") },
        isError = isError,
        singleLine = true,
        supportingText = {
            if (isError) Text("Invalid email format")
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Password (min 6)") },
        isError = isError,
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        supportingText = {
            if (isError) Text("Password too short")
        },
        modifier = Modifier.fillMaxWidth()
    )
}