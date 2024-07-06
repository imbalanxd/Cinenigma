package com.imbaland.cinenigma.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imbaland.cinenigma.domain.model.Lobby
import com.imbaland.cinenigma.domain.remote.CinenigmaFirestore
import com.imbaland.common.data.auth.firebase.FirebaseAuthenticator
import com.imbaland.common.domain.Error
import com.imbaland.common.domain.Result
import com.imbaland.common.domain.auth.AuthenticatedUser
import com.imbaland.movies.domain.model.Movie
import com.imbaland.movies.domain.repository.MoviesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
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
class DebugViewModel @Inject constructor(
    private val moviesRepository: MoviesRepository
) : ViewModel() {
    val uiState: StateFlow<DebugUiState> = flow {
        when(val movie = moviesRepository.discoverRandom()) {
            is Result.Error -> {
                emit(DebugUiState.DebugEmptyState)
            }
            is Result.Success -> {
                emit(DebugUiState.DebugMovieState(movie.data.first()))
            }
        }
    }.stateIn(
        scope = viewModelScope,
        initialValue = DebugUiState.DebugEmptyState,
        started = SharingStarted.WhileSubscribed(5_000),
    )
}

sealed class DebugUiState() {
    data object DebugEmptyState:DebugUiState()
    data class DebugMovieState(val movie: Movie):DebugUiState()
}
