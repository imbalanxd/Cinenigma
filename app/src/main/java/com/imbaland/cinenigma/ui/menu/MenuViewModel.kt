package com.imbaland.cinenigma.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imbaland.cinenigma.domain.model.Lobby
import com.imbaland.cinenigma.domain.remote.CinenigmaFirestore
import com.imbaland.common.data.auth.firebase.FirebaseAuthenticator
import com.imbaland.common.domain.Error
import com.imbaland.common.domain.Result
import com.imbaland.common.domain.auth.AuthenticatedUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val authenticator: FirebaseAuthenticator,
    private val cinenigmaFirestore: CinenigmaFirestore
) : ViewModel() {
    val joinedLobby = MutableStateFlow<Lobby?>(null)
    val uiState: StateFlow<MenuUiState> = flow {
        cinenigmaFirestore.watchLobbies(exclude = "id" to "${authenticator.account?.id}").collect { lobbies ->
            when(lobbies) {
                is Result.Error -> emit(MenuUiState.ErrorState(lobbies.error))
                is Result.Success -> emit(MenuUiState.IdleWithData(lobbies.data, authenticator.account))
            }
        }
    }.stateIn(
        scope = viewModelScope,
        initialValue = MenuUiState.Preloading,
        started = SharingStarted.WhileSubscribed(5_000),
    )
    fun joinLobby(lobby: Lobby) {
        viewModelScope.launch {
            when(val result = cinenigmaFirestore.joinLobby(lobby.id)) {
                is Result.Error -> {
                }
                is Result.Success -> {
                    joinedLobby.value = lobby.copy(player = authenticator.account)
                }
            }
        }
    }

    fun leftLobby() {
        joinedLobby.value = null
    }

    fun changeName(name: String) {
        viewModelScope.launch {
            authenticator.changeName(name)
        }
    }
}

sealed class MenuUiState(open val user: AuthenticatedUser?) {
    data object Preloading : MenuUiState(null)
    data class IdleWithData(val lobbies: List<Lobby>, override val user: AuthenticatedUser?) : MenuUiState(user)
//    data class JoinedLobby(val lobby: Lobby): MenuUiState
    data class ErrorState(val error: Error) : MenuUiState(null)
}
