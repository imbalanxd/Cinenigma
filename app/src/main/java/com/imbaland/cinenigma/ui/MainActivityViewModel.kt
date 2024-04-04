package com.imbaland.cinenigma.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.imbaland.common.data.auth.firebase.FirebaseAuthenticator
import com.imbaland.common.data.utils.logDebug
import com.imbaland.common.domain.Error
import com.imbaland.common.domain.Result
import com.imbaland.common.domain.auth.AuthenticationError
import com.imbaland.movies.data.remote.CinenigmaFirestore
import com.imbaland.movies.domain.model.Movie
import com.imbaland.movies.domain.model.MovieDetails
import com.imbaland.movies.domain.repository.MoviesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    moviesRepository: MoviesRepository,
    cinenigmaFirestore: CinenigmaFirestore
) : ViewModel() {
    val uiState: StateFlow<MainActivityUiState> = flow {
        when(val authResult = FirebaseAuthenticator().login()) {
            is Result.Error -> {
                when(authResult.error) {
                    AuthenticationError.GENERAL_ERROR -> {}
                    AuthenticationError.NULL_USER -> {}
                }
                MainActivityUiState.ErrorState(authResult.error)
            }
            is Result.Success -> {
                emit(MainActivityUiState.Authenticated)
            }
        }
        val movie = moviesRepository.getMovies()[1]
        cinenigmaFirestore.createLobby("lol a lobby")
        when(val result = cinenigmaFirestore.getLobbies()) {
            is Result.Error -> {
                //logDebug("Error occured ${result.error}")
            }
            is Result.Success -> {
                //logDebug("Success ${result.data.size}")
            }
        }
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
    data class ErrorState(val error: Error) : MainActivityUiState
    data class Success(val movie: Movie, val details: MovieDetails? = null) : MainActivityUiState
}
