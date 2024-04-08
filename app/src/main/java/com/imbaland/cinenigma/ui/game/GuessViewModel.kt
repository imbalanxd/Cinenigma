//package com.imbaland.cinenigma.ui.game
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.imbaland.common.domain.Error
//import com.imbaland.common.domain.Result
//import com.imbaland.cinenigma.data.remote.CinenigmaFirestore
//import com.imbaland.movies.domain.model.Lobby
//import com.imbaland.movies.domain.repository.MoviesRepository
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.SharingStarted
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.flow
//import kotlinx.coroutines.flow.stateIn
//import javax.inject.Inject
//
//@HiltViewModel
//class GuessViewModel @Inject constructor(
//    private val moviesRepository: MoviesRepository,
//    private val cinenigmaFirestore: CinenigmaFirestore
//) : ViewModel() {
//    val uiState: StateFlow<GuessUiState> = flow {
//        //UseCase join game
//        cinenigmaFirestore.watchLobby()
//        //
//    }.stateIn(
//        scope = viewModelScope,
//        initialValue = GuessUiState.Loading,
//        started = SharingStarted.WhileSubscribed(5_000),
//    )
//}
//
//sealed interface GuessUiState {
//    data object Loading : GuessUiState
//    data object Waiting : GuessUiState
//    data object Playing : GuessUiState
//    data class ErrorState(val error: Error) : GuessUiState
//}
