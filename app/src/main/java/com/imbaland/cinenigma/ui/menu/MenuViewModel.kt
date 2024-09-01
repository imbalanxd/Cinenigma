package com.imbaland.cinenigma.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imbaland.cinenigma.domain.model.Lobby
import com.imbaland.cinenigma.domain.remote.CinenigmaFirestore
import com.imbaland.cinenigma.domain.usecase.CreateLobbyUseCase
import com.imbaland.common.data.auth.firebase.FirebaseAuthenticator
import com.imbaland.common.domain.Error
import com.imbaland.common.domain.Result
import com.imbaland.common.domain.auth.AuthenticatedUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val authenticator: FirebaseAuthenticator,
    private val cinenigmaFirestore: CinenigmaFirestore,
    private val createLobbyUseCase: CreateLobbyUseCase
) : ViewModel() {
    private val joinedLobbies:StateFlow<List<Lobby>> = flow<List<Lobby>> {
        cinenigmaFirestore.watchJoinedLobbies().collect { lobbies ->
            when(lobbies) {
                is Result.Error -> emit(listOf())
                is Result.Success -> emit(lobbies.data)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        initialValue = listOf(),
        started = WhileSubscribed(),
    )
    private val inLobby = MutableStateFlow<Boolean>(false)
    private val lobbyList: StateFlow<List<Lobby>> = flow<List<Lobby>> {
        cinenigmaFirestore.watchLobbies(exclude = "host" to null).collect { lobbies ->
            when(lobbies) {
                is Result.Error -> emit(listOf())
                is Result.Success -> emit(lobbies.data)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        initialValue = listOf(),
        started = WhileSubscribed(5_000),
    )
    val uiState: StateFlow<MenuUiState> = combine(joinedLobbies, inLobby, lobbyList) { joinedLobbies, inLobby, lobbyList ->
        when {
            authenticator.account == null -> {
                MenuUiState.Loading
            }
            joinedLobbies.isNotEmpty() -> {
                when(inLobby) {
                    true -> {
                        MenuUiState.InLobby(authenticator.account!!, joinedLobbies.first())
                    }
                    false -> {
                        leaveLobby()
                        MenuUiState.Loading
                    }
                }
            }
            lobbyList.isNotEmpty() -> {
                MenuUiState.Loaded(authenticator.account!!, lobbyList)
            }
            else -> {
                MenuUiState.Preloading(authenticator.account!!)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        initialValue = MenuUiState.Loading,
        started = SharingStarted.Eagerly,
    )
    fun joinLobby(lobby: Lobby) {
        viewModelScope.launch {
            inLobby.value = true
            when(val result = cinenigmaFirestore.joinLobby(lobby.id)) {
                is Result.Error -> {
                }
                is Result.Success -> {
//                    joinedLobby.value = lobby.copy(player = authenticator.account)
                }
            }
        }
    }

    fun leaveLobby() {
        inLobby.value = false
        viewModelScope.launch {
            joinedLobbies.value.firstOrNull()?.let { currentLobby ->
                when (cinenigmaFirestore.leaveLobby(currentLobby.id)) {
                    is Result.Error -> {

                    }
                    is Result.Success -> {

                    }
                }
            }
        }
    }

    fun createLobby() {
        viewModelScope.launch {
            inLobby.value = true
            when(val lobbyResult = createLobbyUseCase("game")) {
                is Result.Error -> {

                }
                is Result.Success -> {
//                    joinedLobby.value = lobbyResult.data
                }
            }
        }
    }

    fun changeName(name: String) {
        viewModelScope.launch {
            authenticator.changeName(name)
        }
    }
}

sealed class MenuUiState(open val user: AuthenticatedUser? = null) {
    data object Loading : MenuUiState()
    data class Preloading(override val user: AuthenticatedUser) : MenuUiState(user)
    data class Loaded(override val user: AuthenticatedUser, val lobbies: List<Lobby>) : MenuUiState(user)
    data class InLobby(override val user: AuthenticatedUser, val joinedLobby: Lobby) : MenuUiState(user)
    data class ErrorState(val error: Error) : MenuUiState()
}
