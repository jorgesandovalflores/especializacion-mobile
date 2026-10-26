package com.example.android.features.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.features.profile.domain.usecase.ChangePhotoUseCase
import com.example.android.features.profile.domain.usecase.GetProfileUseCase
import com.example.android.features.profile.domain.usecase.PhotoState
import com.example.android.features.profile.domain.usecase.ProfileState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfile: GetProfileUseCase,
    private val changePhoto: ChangePhotoUseCase
) : ViewModel() {

    private val _profile = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profile: StateFlow<ProfileState> = _profile.asStateFlow()

    private val _photo = MutableStateFlow<PhotoState?>(null)
    val photo: StateFlow<PhotoState?> = _photo.asStateFlow()

    private var upload: Job? = null

    init {
        load()
    }

    fun load() {
        getProfile()
            .onEach { _profile.value = it }
            .launchIn(viewModelScope)
    }

    fun onPhotoPicked(uri: String) {
        if (upload?.isActive == true) return
        upload = changePhoto(uri)
            .onEach { state ->
                _photo.value = state
                if (state is PhotoState.Success) _profile.value = ProfileState.Success(state.passenger)
            }
            .launchIn(viewModelScope)
    }

    fun onToastShown() {
        if (_photo.value is PhotoState.Success || _photo.value is PhotoState.Error) _photo.value = null
    }
}
