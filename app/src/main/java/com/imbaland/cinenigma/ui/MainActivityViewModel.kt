package com.imbaland.cinenigma.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imbaland.movies.domain.model.Movie
import com.imbaland.movies.domain.model.MovieDetails
import com.imbaland.movies.domain.repository.MoviesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    moviesRepository: MoviesRepository
) : ViewModel() {
    val uiState: StateFlow<MainActivityUiState> = flow {
        val movie = moviesRepository.getMovies()[1]
        kotlinx.coroutines.delay(1000)
        emit(MainActivityUiState.Success(movie))
        kotlinx.coroutines.delay(5000)
        val details = moviesRepository.getMovieDetails(movie.id)
        emit(MainActivityUiState.Success(movie, details))
    }.stateIn(
        scope = viewModelScope,
        initialValue = MainActivityUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000),
    )
}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Success(val movie: Movie, val details: MovieDetails? = null) : MainActivityUiState
}
