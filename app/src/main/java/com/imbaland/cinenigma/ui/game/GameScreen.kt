package com.imbaland.cinenigma.ui.game

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.imbaland.movies.ui.widget.MoviePoster

fun NavGraphBuilder.gameRoute(
    route: String,
    navController: NavController
) {
    composable(
        route = route,
        arguments = listOf(navArgument("gameId") { type = NavType.StringType })
    ) {
        GameScreen(hiltViewModel<GameViewModel>(it))
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun GameScreen(
    viewModel: GameViewModel
) {
    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val uiState: GameUiState by viewModel.uiState.collectAsStateWithLifecycle()
        val (loading) = createRefs()
        when (val state = uiState) {
            GameUiState.Closing -> {

            }

            is GameUiState.Error -> {

            }

            GameUiState.Loading -> {

            }

            is Guesser -> {
                when (state) {
                    is Guesser.Guessing -> {

                    }

                    is Guesser.Waiting -> {

                    }
                }
            }

            is Hinter -> {
                Column(
                    modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        modifier = Modifier.size(30.dp),
                        onClick = viewModel::newGame
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.RotateLeft,
                            contentDescription = "Localized description"
                        )
                    }
                    when (state) {
                        is Hinter.Hinting -> {
                            MoviePoster(
                                modifier = Modifier.fillMaxWidth(0.7f),
                                imageUrl = state.game.movie?.image ?: ""
                            )
                        }

                        is Hinter.Waiting -> {

                        }
                    }
                }
            }
        }
    }
}