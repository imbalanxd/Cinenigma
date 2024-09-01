package com.imbaland.cinenigma.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imbaland.common.data.auth.firebase.FirebaseAuthenticator
import com.imbaland.common.domain.Error
import com.imbaland.common.domain.Result
import com.imbaland.common.domain.auth.AuthenticatedUser
import com.imbaland.common.domain.auth.AuthenticationError
import com.imbaland.cinenigma.domain.remote.CinenigmaFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    val cinenigmaFirestore: CinenigmaFirestore,
    val authenticator: FirebaseAuthenticator
) : ViewModel() {
    val uiState: StateFlow<MainActivityUiState> = flow {
        when(val authResult = authenticator.login()) {
            is Result.Error -> {
                when(authResult.error) {
                    else -> {

                    }
                }
                MainActivityUiState.ErrorState(authResult.error)
            }
            is Result.Success -> {
                emit(MainActivityUiState.Authenticated(authResult.data))
            }
        }
    }.stateIn(
        scope = viewModelScope,
        initialValue = MainActivityUiState.Loading,
        started = SharingStarted.Eagerly,
    )
}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Authenticated(val user: AuthenticatedUser) : MainActivityUiState
    data class ErrorState(val error: Error) : MainActivityUiState
}
