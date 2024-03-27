package com.imbaland.cinenigma.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imbaland.common.data.auth.firebase.FirebaseAuthenticator
import com.imbaland.common.domain.auth.Result
import com.imbaland.movies.domain.model.Movie
import com.imbaland.movies.domain.model.MovieDetails
import com.imbaland.movies.domain.repository.MoviesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    moviesRepository: MoviesRepository
) : ViewModel() {
    val uiState: StateFlow<MainActivityUiState> = flow {
        when(val authResult = FirebaseAuthenticator().login()) {
            is Result.Error -> MainActivityUiState.Error
            is Result.Success -> {
                Log.d("WTF", "heres da data: ${authResult.data.id} ${authResult.data.name}")
                emit(MainActivityUiState.Authenticated)
            }
        }
        val movie = moviesRepository.getMovies()[1]
        emit(MainActivityUiState.Success(movie))
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
    data object Authenticated : MainActivityUiState
    data object Error : MainActivityUiState
    data class Success(val movie: Movie, val details: MovieDetails? = null) : MainActivityUiState
}
