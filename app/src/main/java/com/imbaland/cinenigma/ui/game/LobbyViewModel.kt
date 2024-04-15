package com.imbaland.cinenigma.ui.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imbaland.cinenigma.domain.model.Lobby
import com.imbaland.cinenigma.domain.model.LobbyState
import com.imbaland.cinenigma.domain.model.state
import com.imbaland.common.domain.Error
import com.imbaland.common.domain.Result
import com.imbaland.cinenigma.domain.remote.CinenigmaFirestore
import com.imbaland.movies.domain.repository.MoviesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class LobbyViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val moviesRepository: MoviesRepository,
    private val cinenigmaFirestore: CinenigmaFirestore
) : ViewModel() {
    private val gameId: String? = savedStateHandle[IN_GAME_ARG_GAME_ID]
    private val gameName: String = checkNotNull(savedStateHandle[IN_GAME_ARG_GAME_NAME])
    val uiState: StateFlow<LobbyUiState> = flow {
        //UseCase join game
        val newGameId = gameId ?: when (val result = cinenigmaFirestore.createLobby(gameName!!)) {
            is Result.Error -> {
                emit(LobbyUiState.Error(LobbyError.LobbyCreationError))
                null
            }
            is Result.Success -> {
                result.data.id
            }
        }
        newGameId?.let { id ->
            cinenigmaFirestore.watchLobby(id).collect { result ->
                when (result) {
                    is Result.Error -> {
                        emit(LobbyUiState.Error(LobbyError.LobbyJoiningError))
                    }
                    is Result.Success -> {
                        val lobby = result.data
                        when (lobby.state) {
                            LobbyState.Invalid -> {
                                emit(LobbyUiState.Creating(gameName))
                            }
                            LobbyState.Open, LobbyState.Full -> {
                                emit(LobbyUiState.Waiting(lobby!!))
                            }
                            LobbyState.Starting, LobbyState.Waiting -> {
                                emit(LobbyUiState.Starting(lobby!!))
                            }
                            else -> {

                            }
                        }
                    }
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        initialValue = LobbyUiState.Creating(gameName),
        started = SharingStarted.WhileSubscribed(5_000),
    )
}

sealed interface LobbyUiState {
    data class Error(val error: LobbyError) : LobbyUiState
    data class Creating(val lobbyTitle: String) : LobbyUiState
    open class Activated(val lobby: Lobby) : LobbyUiState
    class Waiting(lobby: Lobby) : Activated(lobby)
    class Starting(lobby: Lobby) : Activated(lobby)
}

sealed class LobbyError {
    data object LobbyCreationError : LobbyError()
    data object LobbyJoiningError : LobbyError()
}

sealed interface InGameUiState {
    data object Loading : InGameUiState
    data object Waiting : InGameUiState
    data object Guessing : InGameUiState
    data object Hinting : InGameUiState
    data class ErrorState(val error: Error) : InGameUiState
}
