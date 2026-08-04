package com.example.demo_mvi.home

/**
 * Pure function: (State, Intent) -> State. No side effects here — side effects
 * (repository calls, navigation) live in the ViewModel (see
 * references/ui-pattern-mvi.md in the android-development skill).
 */
object HomeReducer {
    fun reduce(state: HomeState, intent: HomeIntent): HomeState =
        when (intent) {
            HomeIntent.LoadData -> state.copy(isLoading = true, errorMessage = null)
            is HomeIntent.TransactionClicked -> state // no state change; triggers a side effect instead
            HomeIntent.DismissError -> state.copy(errorMessage = null)
        }
}
