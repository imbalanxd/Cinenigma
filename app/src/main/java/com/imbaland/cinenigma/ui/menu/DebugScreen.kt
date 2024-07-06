package com.imbaland.cinenigma.ui.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.imbaland.cinenigma.ui.widget.MoviePosterr
import com.imbaland.movies.ui.util.poster
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

fun NavGraphBuilder.debugRoute(route: String, navController: NavController) {
    composable(route = route) {
        DebugScreen(hiltViewModel<DebugViewModel>(remember(it){navController.getBackStackEntry(it.destination.parent!!.route!!)}),)
    }
}

@Composable
internal fun DebugScreen(viewModel: DebugViewModel) {
    val uiState: DebugUiState by viewModel.uiState.collectAsStateWithLifecycle()
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when(val state = uiState) {
            DebugUiState.DebugEmptyState -> {

            }
            is DebugUiState.DebugMovieState -> {
                MoviePosterr(modifier = Modifier.fillMaxWidth(0.6f)
                    .poster(), posterUrl = state.movie.image)
            }
        }
    }
}