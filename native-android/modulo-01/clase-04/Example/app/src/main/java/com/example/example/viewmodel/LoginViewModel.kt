package com.example.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// usamos Channel para eventos one-shot (sin replay)
class LoginViewModel : ViewModel() {

    // Canal privado de eventos; la UI escucha como Flow
    private val _navEvents = Channel<NavEvent>(capacity = Channel.BUFFERED)
    val navEvents = _navEvents.receiveAsFlow()

    // lógica simplificada de login
    fun onLogin(email: String, pass: String) {
        viewModelScope.launch {
            // Simulación de validación
            if (email == "a@b.com" && pass == "1234") {
                _navEvents.send(NavEvent.GoHome)
            }
        }
    }

    fun onClickRegister() {
        viewModelScope.launch {
            _navEvents.send(NavEvent.GoRegister)
        }
    }
}