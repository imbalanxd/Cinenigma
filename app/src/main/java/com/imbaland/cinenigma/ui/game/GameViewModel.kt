package com.imbaland.cinenigma.ui.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imbaland.cinenigma.domain.model.Game
import com.imbaland.cinenigma.domain.model.Lobby
import com.imbaland.cinenigma.domain.model.LobbyState
import com.imbaland.cinenigma.domain.model.state
import com.imbaland.cinenigma.domain.model.timeRemaining
import com.imbaland.cinenigma.domain.remote.CinenigmaFirestore
import com.imbaland.cinenigma.ui.menu.IN_GAME_ARG_GAME_ID
import com.imbaland.common.data.auth.firebase.FirebaseAuthenticator
import com.imbaland.common.domain.Result
import com.imbaland.common.domain.auth.AuthenticatedUser
import com.imbaland.movies.domain.model.MovieDetails
import com.imbaland.movies.domain.repository.MoviesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val firebaseAuth: FirebaseAuthenticator,
    private val cinenigmaFirestore: CinenigmaFirestore,
    private val moviesRepository: MoviesRepository
) : ViewModel() {
    private val player: AuthenticatedUser = firebaseAuth.account!!
    private val gameId: String = savedStateHandle[IN_GAME_ARG_GAME_ID]!!
    private val Game.isHinter: Boolean
        get() = this.hinter?.id == player.id
    private val Lobby.firstHinter: Boolean
        get() = when (val key =
            (this.gameStartedAt!!.toInstant().epochSecond) % 2) {
            0L -> {
                this.host
            }

            else -> {
                this.player
            }
        }?.id == (firebaseAuth.account?.id ?: -1)

    val lobbyState: Flow<Lobby?> = flow {
        cinenigmaFirestore.watchLobby(gameId).collect { result ->
            when (result) {
                is Result.Error -> emit(null)
                is Result.Success -> {
                    emit(result.data)
                }
            }
        }
    }
    val lobbyGames: Flow<List<Game>> = flow {
        cinenigmaFirestore.watchGames(gameId).collect { result ->
            when (result) {
                is Result.Error -> emit(listOf())
                is Result.Success -> {
                    emit(result.data)
                }
            }
        }
    }

    val uiState: StateFlow<GameUiState> = combine(lobbyState, lobbyGames) { lobby, games ->
        when (lobby?.state) {
            LobbyState.Started -> {
                val validGames = games.filter { game -> game.startedAt.after(lobby.gameStartedAt) }
                val currentGame = validGames.lastOrNull()
                when (currentGame?.state) {
                    null, is Game.State.Completed -> {
                        val isHinter = lobby.firstHinter
                        if (isHinter) {
                            Setup.Choosing(lobby, validGames)
                        } else {
                            Setup.Waiting(lobby, validGames)
                        }
                    }

                    else -> {
                        val isHinter = currentGame.isHinter
                        when (currentGame.state) {
                            is Game.State.Hinting -> {
                                if (isHinter) {
                                    Hinter.Hinting(lobby, validGames)
                                } else {
                                    Guesser.Waiting(lobby, validGames)
                                }
                            }

                            is Game.State.Guessing -> {
                                if (isHinter) {
                                    Hinter.Waiting(lobby, validGames)
                                } else {
                                    Guesser.Guessing(lobby, validGames)
                                }
                            }

                            else -> {
                                GameUiState.Loading
                            }
                        }
                    }
                }
            }

            else -> {
                GameUiState.Closing
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GameUiState.Loading)

    fun newGame() {
        when (val state = uiState.value) {
            is Hinter.Hinting, is Setup.Choosing -> {
                viewModelScope.launch {
                    cinenigmaFirestore.startGame(gameId, (state as GameUiState.Playing).games.size, player)
                }
            }

            else -> {

            }
        }
    }
}

sealed class GameUiState {
    data class Error(val error: GameError) : GameUiState()
    data object Loading : GameUiState()
    sealed class Playing(open val lobby: Lobby, open val games: List<Game>) : GameUiState()
    data object Closing : GameUiState()
}

sealed class Setup(lobby: Lobby, games: List<Game>) : GameUiState.Playing(lobby, games) {
    data class Choosing(override val lobby: Lobby, override val games: List<Game>) :
        Setup(lobby, games)

    data class Waiting(override val lobby: Lobby, override val games: List<Game>) :
        Setup(lobby, games)
}

sealed class Guesser(
    lobby: Lobby, games: List<Game>,
    val currentGame: Game = games.last(),
    open val remaining: Int = currentGame.currentRound.timeRemaining
) : GameUiState.Playing(lobby, games) {
    data class Waiting(override val lobby: Lobby, override val games: List<Game>) :
        Guesser(lobby, games)

    data class Guessing(override val lobby: Lobby, override val games: List<Game>) :
        Guesser(lobby, games)
}

sealed class Hinter(
    lobby: Lobby, games: List<Game>,
    val currentGame: Game = games.last(),
    open val remaining: Int = currentGame.currentRound.timeRemaining
) : GameUiState.Playing(lobby, games) {
    data class Waiting(override val lobby: Lobby, override val games: List<Game>) :
        Hinter(lobby, games)

    data class Hinting(override val lobby: Lobby, override val games: List<Game>) :
        Hinter(lobby, games)
}

sealed class GameError {
    data object GeneralGameError : GameError()
}