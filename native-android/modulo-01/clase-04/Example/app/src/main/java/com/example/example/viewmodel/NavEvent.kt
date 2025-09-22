package com.example.example.viewmodel

sealed class NavEvent {
    data object GoHome : NavEvent()
    data object GoRegister : NavEvent()
}