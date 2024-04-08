package com.imbaland.cinenigma.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imbaland.common.domain.Error
import com.imbaland.common.domain.Result
import com.imbaland.cinenigma.domain.model.Lobby
import com.imbaland.cinenigma.domain.remote.CinenigmaFirestore
import com.imbaland.movies.domain.repository.MoviesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val moviesRepository: MoviesRepository,
    private val cinenigmaFirestore: CinenigmaFirestore
) : ViewModel() {
    val uiState: StateFlow<MenuUiState> = flow {
        cinenigmaFirestore.watchLobbies().collect { result ->
            when(result) {
                is Result.Error -> TODO()
                is Result.Success -> emit(MenuUiState.IdleWithData(result.data))
            }
        }
    }.stateIn(
        scope = viewModelScope,
        initialValue = MenuUiState.Preloading,
        started = SharingStarted.WhileSubscribed(5_000),
    )
}

sealed interface MenuUiState {
    data object Preloading : MenuUiState
    data class IdleWithData(val lobbies: List<Lobby>) : MenuUiState
    data class ErrorState(val error: Error) : MenuUiState
}
