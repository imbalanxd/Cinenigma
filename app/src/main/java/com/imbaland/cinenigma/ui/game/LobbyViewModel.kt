package com.imbaland.cinenigma.ui.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imbaland.cinenigma.domain.model.Lobby
import com.imbaland.cinenigma.domain.model.LobbyState
import com.imbaland.cinenigma.domain.model.state
import com.imbaland.common.domain.Result
import com.imbaland.cinenigma.domain.remote.CinenigmaFirestore
import com.imbaland.cinenigma.ui.menu.IN_GAME_ARG_GAME_ID
import com.imbaland.cinenigma.ui.menu.IN_GAME_ARG_GAME_NAME
import com.imbaland.common.data.auth.firebase.FirebaseAuthenticator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LobbyViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val firebaseAuth: FirebaseAuthenticator,
    private val cinenigmaFirestore: CinenigmaFirestore
) : ViewModel() {
    private val gameId: String? = savedStateHandle[IN_GAME_ARG_GAME_ID]
    private val gameName: String = savedStateHandle[IN_GAME_ARG_GAME_NAME] ?: "${firebaseAuth.account?.name}'s Lobby"
    private val leavingLobby = MutableStateFlow<Boolean>(false)
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
                    when(isLeaving) {
                        true -> {
                            LobbyUiState.Leaving()
                        }
                        else -> {
                            when (lobby) {
                                is Result.Error -> {
                                    LobbyUiState.Error(LobbyError.LobbyJoiningError)
                                }

                                is Result.Success -> {
                                    val lobby = lobby.data
                                    when (lobby.state) {
                                        LobbyState.Invalid -> {
                                            LobbyUiState.Creating(gameName)
                                        }

                                        LobbyState.Open, LobbyState.Full -> {
                                            LobbyUiState.Waiting(lobby!!)
                                        }

                                        LobbyState.Starting, LobbyState.Waiting -> {
                                            LobbyUiState.Starting(lobby!!)
                                        }

                                        LobbyState.Loading -> {
                                            LobbyUiState.Starting(lobby!!)
                                        }

                                        LobbyState.Playing -> {
                                            LobbyUiState.Starting(lobby!!)
                                        }
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

    fun leaveLobby() {
        viewModelScope.launch {
            when (val state = uiState.value) {
                is LobbyUiState.Activated -> {
                    val currentLobby = state.lobby
                    if (currentLobby != null) {
                        when (cinenigmaFirestore.leaveLobby(currentLobby.id)) {
                            is Result.Error -> {
                            }

                            is Result.Success -> {
                                leavingLobby.value = true
                            }
                        }
                    }
                }

                else -> {

                }
            }
        }
    }
}

sealed interface LobbyUiState {
    data class Error(val error: LobbyError) : LobbyUiState
    data class Creating(val lobbyTitle: String) : LobbyUiState
    open class Activated(val lobby: Lobby) : LobbyUiState
    class Waiting(lobby: Lobby) : Activated(lobby)
    class Starting(lobby: Lobby) : Activated(lobby)
    open class Leaving() : LobbyUiState
}

sealed class LobbyError {
    data object LobbyCreationError : LobbyError()
    data object LobbyJoiningError : LobbyError()
}
