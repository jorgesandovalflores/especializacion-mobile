package com.example.android_passenger.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_passenger.commons.domain.usecase.GetPassengerLocalState
import com.example.android_passenger.commons.domain.usecase.GetPassengerLocalUseCase
import com.example.android_passenger.core.domain.ErrorMapper
import com.example.android_passenger.core.presentation.toReadableMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPassengerLocalUseCase: GetPassengerLocalUseCase
) : ViewModel() {

    private val _userUi = MutableStateFlow<GetPassengerLocalState>(GetPassengerLocalState.Idle)
    val userUi: StateFlow<GetPassengerLocalState> = _userUi
    fun callGetUser() {
        viewModelScope.launch {
            try {
                getPassengerLocalUseCase().collect {
                    _userUi.value = it
                }
            } catch (t: Throwable) {
                val mapped = ErrorMapper.map(t)
                _userUi.value = GetPassengerLocalState.Error(message = mapped.toReadableMessage())
            }
        }
    }

}