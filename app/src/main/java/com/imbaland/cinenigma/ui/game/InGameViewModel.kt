package com.imbaland.cinenigma.ui.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import javax.inject.Inject

@HiltViewModel
class InGameViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val moviesRepository: MoviesRepository,
    private val cinenigmaFirestore: CinenigmaFirestore
) : ViewModel() {
    private val gameId: String = checkNotNull(savedStateHandle[IN_GAME_ARG_GAME_ID])
    val uiState: StateFlow<InGameUiState> = flow {
        //UseCase join game
        cinenigmaFirestore.watchLobby(gameId).collect { result ->
            when(result) {
                is Result.Error -> TODO()
                is Result.Success -> {
                    val lobby = result.data
                    when(lobby.state()) {
                        LobbyState.Invalid -> TODO()
                        LobbyState.Open -> TODO()
                        LobbyState.Full -> TODO()
                        LobbyState.Starting -> TODO()
                        LobbyState.Waiting -> TODO()
                        LobbyState.Loading -> TODO()
                        LobbyState.Playing -> TODO()
                    }
                }
            }
            emit(InGameUiState.Loading)
        }
        //
    }.stateIn(
        scope = viewModelScope,
        initialValue = InGameUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000),
    )
}

sealed interface InGameUiState {
    data object Loading : InGameUiState
    data object Waiting : InGameUiState
    data object Guessing : InGameUiState
    data object Hinting : InGameUiState
    data class ErrorState(val error: Error) : InGameUiState
}
