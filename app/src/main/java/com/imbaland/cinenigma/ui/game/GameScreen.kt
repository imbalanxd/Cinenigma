package com.imbaland.cinenigma.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.imbaland.movies.ui.widget.TextSelector

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

//@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun GameScreen(
    viewModel: GameViewModel
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val uiState: GameUiState by viewModel.uiState.collectAsStateWithLifecycle()
        Text(modifier = Modifier.align(Alignment.TopCenter), text = "In Game!")
        when (val state = uiState) {
            GameUiState.Closing -> {

            }

            is GameUiState.Error -> {

            }

            GameUiState.Loading -> {

            }

            is Setup.Choosing -> {
                Column(
                    modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextButton(onClick = viewModel::newGame) {
                        Text(text = "Start Round")
                    }
                }
            }

            is Setup.Waiting -> {

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
                val synopsisHint = remember { hashMapOf<String, Unit>() }
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
//                            MoviePoster(
//                                modifier = Modifier.fillMaxWidth(0.7f),
//                                imageUrl = state.game.movie?.image ?: ""
//                            )
                            TextSelector(
                                modifier = Modifier.fillMaxWidth(0.7f),
                                text = state.currentGame.movie?.overview ?: "none lol",
                                style = TextStyle.Default.copy(
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 26.sp,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                ),
                                maxScale = 1.8f,
                                filter = { selection -> state.currentGame.movie?.title?.contains(selection) != true },
                                onSelected = { range, word, selected ->
                                    if (selected) synopsisHint[word] =
                                        Unit else synopsisHint.remove(word)
                                })
                        }

                        is Hinter.Waiting -> {

                        }
                    }
                }
            }
        }
    }
}