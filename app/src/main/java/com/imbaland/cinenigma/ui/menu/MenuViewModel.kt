package com.imbaland.cinenigma.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imbaland.common.domain.Error
import com.imbaland.common.domain.Result
import com.imbaland.cinenigma.domain.model.Lobby
import com.imbaland.cinenigma.domain.remote.CinenigmaFirestore
import com.imbaland.common.data.auth.firebase.FirebaseAuthenticator
import com.imbaland.movies.domain.repository.MoviesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val authenticator: FirebaseAuthenticator,
    private val cinenigmaFirestore: CinenigmaFirestore
) : ViewModel() {
//    private val lobbiesFlow = channelFlow {cinenigmaFirestore.watchLobbies().collect { result ->
//        when(result) {
//            is Result.Error -> emit(MenuUiState.ErrorState(result.error))
//            is Result.Success -> emit(MenuUiState.IdleWithData(result.data))
//        }
//    }
//    }.stateIn(
//        scope = viewModelScope,
//        initialValue = MenuUiState.Preloading,
//        started = SharingStarted.WhileSubscribed(5_000),
//    )
    val uiState: StateFlow<MenuUiState> = flow {
        combine(cinenigmaFirestore.watchLobbies(), cinenigmaFirestore.watchLobbies(mapOf("player" to authenticator.account!!))) { lobbies , joinedLobbies ->
            when {
                joinedLobbies is Result.Error || (joinedLobbies is Result.Success && joinedLobbies.data.isEmpty())  -> {
                    when(lobbies) {
                        is Result.Error -> MenuUiState.ErrorState(lobbies.error)
                        is Result.Success -> MenuUiState.IdleWithData(lobbies.data)
                    }
                }
                joinedLobbies is Result.Success -> {
                    MenuUiState.JoinedLobby(joinedLobbies.data.first())
                }
                else -> {MenuUiState.Preloading}
            }
        }.collect { state ->
            emit(state)
        }
    }.stateIn(
        scope = viewModelScope,
        initialValue = MenuUiState.Preloading,
        started = SharingStarted.WhileSubscribed(5_000),
    )

    fun joinLobby(lobby: Lobby) {
        viewModelScope.launch {
            when(cinenigmaFirestore.joinLobby(lobby.id)) {
                is Result.Error -> {

                }
                is Result.Success -> {

                }
            }
        }
    }
}

sealed interface MenuUiState {
    data object Preloading : MenuUiState
    data class IdleWithData(val lobbies: List<Lobby>) : MenuUiState
    data class JoinedLobby(val lobby: Lobby): MenuUiState
    data class ErrorState(val error: Error) : MenuUiState
}
