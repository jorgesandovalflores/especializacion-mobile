package com.example.demo_mvi.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI ViewModel for the Home screen (see references/ui-pattern-mvi.md in the
 * android-development skill). Owns the single [HomeState], applies every
 * dispatched [HomeIntent] through the pure [HomeReducer], then runs whatever
 * side effect that intent implies (repository calls, etc.) — side effects
 * never mutate state directly, only through another `_state.update`.
 *
 * This demo instantiates the Model manually (no Hilt) to keep the MVI pattern
 * itself front and center; a production app would inject it instead.
 */
class HomeViewModel(
    private val model: HomeModel = HomeModel(),
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        dispatch(HomeIntent.LoadData)
    }

    fun dispatch(intent: HomeIntent) {
        _state.update { HomeReducer.reduce(it, intent) }
        handleSideEffect(intent)
    }

    private fun handleSideEffect(intent: HomeIntent) {
        when (intent) {
            HomeIntent.LoadData -> viewModelScope.launch {
                try {
                    val data = model.fetchHomeData()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            userName = data.userName,
                            activityValues = data.activityValues,
                            activityLabels = data.activityLabels,
                            salesLastWeek = data.salesLastWeek,
                            revenueLastWeek = data.revenueLastWeek,
                            transactions = data.transactions,
                        )
                    }
                } catch (e: Exception) {
                    _state.update { it.copy(isLoading = false, errorMessage = e.message ?: "Unknown error") }
                }
            }

            is HomeIntent.TransactionClicked -> viewModelScope.launch {
                model.markTransactionSeen(intent.id)
            }

            HomeIntent.DismissError -> Unit // pure state change only, already handled by the Reducer
        }
    }
}
