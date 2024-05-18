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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    private val Lobby.isHinter: Boolean
        get() = when(val key = (this.id.first().code + this.games.size) % 2) {
            0 -> {
                this.host
            }
            else -> {
                this.player
            }
        }?.id == (firebaseAuth.account?.id?:-1)
    val uiState: StateFlow<GameUiState> = flow {
        cinenigmaFirestore.watchLobby(gameId).collect { result ->
            when(result) {
                is Result.Error -> emit(GameUiState.Error(GameError.GeneralGameError))
                is Result.Success -> {
                    val lobby = result.data!!
                    when (lobby.state) {
                        LobbyState.Loading -> {
                            emit(if(lobby.isHinter) Setup.Choosing(lobby) else Setup.Waiting(lobby))
                        }
                        LobbyState.Playing -> {
                            val game = lobby.activeGame!!
                            val isHinter = game.isHinter(firebaseAuth.account!!.id)
                            when(game.state) {
                                is Game.State.Hinting -> {
                                    emit(if(isHinter) Hinter.Hinting(lobby, lobby.activeGame!!) else Guesser.Waiting(lobby, lobby.activeGame!!))
                                }
                                is Game.State.Guessing -> {
                                    emit(if(isHinter) Hinter.Waiting(lobby, lobby.activeGame!!) else Guesser.Guessing(lobby, lobby.activeGame!!))
                                }
                                else -> {
                                    emit(GameUiState.Loading)
                                }
                            }
                        }
                        else -> {
                            emit(GameUiState.Loading)
                        }
                    }
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        initialValue = GameUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000),
    )

    fun newGame() {
        when(val state = uiState.value) {
            is Hinter.Hinting, is Setup.Choosing -> {
                viewModelScope.launch {
                    cinenigmaFirestore.startGame(gameId, player)
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
    sealed class Playing(open val lobby: Lobby, open val game: Game) : GameUiState()
    data object Closing : GameUiState()
}

sealed class Setup(open val lobby: Lobby): GameUiState() {
    data class Choosing(override val lobby: Lobby): Setup(lobby)
    data class Waiting(override val lobby: Lobby): Setup(lobby)
}
sealed class Guesser(lobby: Lobby, game: Game, open val remaining: Int = game.currentRound.timeRemaining): GameUiState.Playing(lobby, game) {
    data class Waiting(override val lobby: Lobby, override val game: Game): Guesser(lobby, game)
    data class Guessing(override val lobby: Lobby, override val game: Game): Guesser(lobby, game)
}
sealed class Hinter(lobby: Lobby, game: Game, open val remaining: Int = game.currentRound.timeRemaining): GameUiState.Playing(lobby, game) {
    data class Waiting(override val lobby: Lobby, override val game: Game): Hinter(lobby, game)
    data class Hinting(override val lobby: Lobby, override val game: Game): Hinter(lobby, game)
}

sealed class GameError {
    data object GeneralGameError : GameError()
}