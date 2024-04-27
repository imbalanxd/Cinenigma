package com.imbaland.cinenigma.ui.menu

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imbaland.cinenigma.domain.model.Lobby
import com.imbaland.cinenigma.domain.model.LobbyState
import com.imbaland.cinenigma.domain.model.state
import com.imbaland.common.domain.Result
import com.imbaland.cinenigma.domain.remote.CinenigmaFirestore
import com.imbaland.common.data.auth.firebase.FirebaseAuthenticator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.temporal.ChronoUnit
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class LobbyViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val firebaseAuth: FirebaseAuthenticator,
    private val cinenigmaFirestore: CinenigmaFirestore
) : ViewModel() {
    private val gameId: String? = savedStateHandle[IN_GAME_ARG_GAME_ID]
    private val gameName: String =
        savedStateHandle[IN_GAME_ARG_GAME_NAME] ?: "${firebaseAuth.account?.name}'s Lobby"
    private val leavingLobby = MutableStateFlow(false)
    private val isHost = gameId == null
    val uiState = MutableStateFlow<LobbyUiState>(LobbyUiState.Creating(gameName))

    init {
        viewModelScope.launch {
            //UseCase join game
            val newGameId =
                gameId ?: when (val result = cinenigmaFirestore.createLobby(gameName)) {
                    is Result.Error -> {
                        uiState.value = LobbyUiState.Error(LobbyError.LobbyCreationError)
                        null
                    }

                    is Result.Success -> {
                        result.data.id
                    }
                }

            newGameId?.let { id ->
                combine(cinenigmaFirestore.watchLobby(id), leavingLobby) { lobby, isLeaving ->
                    if (isLeaving) {
                        LobbyUiState.Closing
                    } else {
                        when (lobby) {
                            is Result.Error -> {
                                LobbyUiState.Error(LobbyError.LobbyJoiningError)
                            }
                            is Result.Success -> {
                                val lobby = lobby.data
                                when (lobby?.state) {
                                    LobbyState.Invalid -> {
                                        if(isHost) LobbyUiState.Creating(gameName) else LobbyUiState.Closing
                                    }
                                    LobbyState.Open -> {
                                        if(isHost) Host.Waiting(lobby) else Joiner.Waiting(lobby)
                                    }
                                    LobbyState.Full -> {
                                        if(isHost) Host.Full(lobby) else Joiner.Waiting(lobby)
                                    }
                                    LobbyState.Starting -> {
                                        if(isHost) {
                                            Host.Starting(lobby)
                                        } else {
                                            startLobby()
                                            Joiner.Starting(lobby)
                                        }
                                    }
                                    LobbyState.Confirmed -> {
                                        LobbyUiState.Countdown(lobby)
                                    }
                                    LobbyState.Loading, LobbyState.Playing -> {
                                        LobbyUiState.Started(lobby)
                                    }
                                    else -> {
                                        LobbyUiState.Error(LobbyError.LobbyJoiningError)
                                    }
                                }
                            }
                        }
                    }
                }.collect { state ->
                    uiState.value = state
                }
            }
        }
    }
    fun startGame() {
        if(!isHost)
            return
        viewModelScope.launch {
            when (val state = uiState.value) {
                is LobbyUiState.Countdown -> {
                    when (cinenigmaFirestore.startGame(state.lobby.id, state.lobby.host!!)) {
                        is Result.Error -> {

                        }
                        is Result.Success -> {

                        }
                    }
                }
                else -> {

                }
            }
        }
    }
    fun startLobby() {
        viewModelScope.launch {
            when (val state = uiState.value) {
                is LobbyUiState.Created -> {
                    when (cinenigmaFirestore.startLobby(state.lobby.id, isHost)) {
                        is Result.Error -> {

                        }
                        is Result.Success -> {

                        }
                    }
                }
                else -> {

                }
            }
        }
    }
    fun leaveLobby() {
        viewModelScope.launch {
            when (val state = uiState.value) {
                is LobbyUiState.Created -> {
                    when (cinenigmaFirestore.leaveLobby(state.lobby.id, isHost)) {
                        is Result.Error -> {

                        }
                        is Result.Success -> {
                            leavingLobby.value = true
                        }
                    }
                }

                else -> {

                }
            }
        }
    }
}

sealed class LobbyUiState {
    data class Error(val error: LobbyError) : LobbyUiState()
    data class Creating(val name: String) : LobbyUiState()
    sealed class Created(open val lobby: Lobby) : LobbyUiState()
    data class Countdown(val lobby: Lobby) : LobbyUiState() {
        val timeRemaining: Int
            get() = ChronoUnit.SECONDS.between(Date().toInstant(), lobby.playerStartedAt?.toInstant()).toInt()
    }
    data class Started(val lobby: Lobby) : LobbyUiState()
    data object Closing : LobbyUiState()
}

sealed class Host(lobby: Lobby) : LobbyUiState.Created(lobby) {
    data class Waiting(override val lobby: Lobby) : Host(lobby)
    data class Full(override val lobby: Lobby) : Host(lobby)
    data class Starting(override val lobby: Lobby) : Host(lobby)
}

sealed class Joiner(lobby: Lobby) : LobbyUiState.Created(lobby) {
    data class Waiting(override val lobby: Lobby) : Host(lobby)
    data class Starting(override val lobby: Lobby) : Host(lobby)
}

sealed class LobbyError {
    data object LobbyCreationError : LobbyError()
    data object LobbyJoiningError : LobbyError()
}
