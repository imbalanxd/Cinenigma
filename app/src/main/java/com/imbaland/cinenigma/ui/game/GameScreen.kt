package com.imbaland.cinenigma.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.imbaland.cinenigma.R
import com.imbaland.cinenigma.domain.model.Guess
import com.imbaland.cinenigma.domain.model.Hint
import com.imbaland.movies.ui.widget.MovieAutoComplete
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
                Column(
                    modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    state.currentGame.hints?.forEach { round ->
                        when(val hint = round.hint()) {
                            Hint.EmptyHint -> TODO()
                            is Hint.KeywordHint -> {
                                KeywordHint(hint)
                            }
                            is Hint.PosterHint -> TODO()
                        }
                    }
                    when(state) {
                        is Guesser.Waiting -> {
                            Text("Waiting for the next hint")
                        }
                        is Guesser.Guessing -> {
                            val movieOptions by viewModel.searchResults.collectAsState()
                            MovieAutoComplete(
                                modifier = Modifier.fillMaxWidth().padding(15.dp),
                                options = movieOptions,
                                onTextChanged = viewModel::performMovieSearch,
                                onSubmit = viewModel::submitGuess)
                        }
                    }
                }
            }
            is Hinter -> {
                val synopsisHint = remember { mutableListOf<Pair<String, IntRange>>() }
                Column(
                    modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    state.currentGame.guesses?.forEach { round ->
                        GuessTitle(round)
                    }
                    Text(state.currentGame.movie?.title?:"")
                    IconButton(
                        modifier = Modifier.size(30.dp),
                        onClick = viewModel::newGame
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.RotateLeft,
                            contentDescription = "Localized description"
                        )
                    }
                    IconButton(
                        modifier = Modifier.size(30.dp),
                        onClick = { viewModel.submitHint(synopsisHint.last().let { Hint.KeywordHint(it.first, it.second.first.toLong(), it.second.last.toLong()) }) }
                    ) {
                        Icon(
                            painter = painterResource(com.imbaland.movies.R.drawable.ic_confirm),
                            contentDescription = "Localized description"
                        )
                    }
                    when (state) {
                        is Hinter -> {
                            when(state) {
                                is Hinter.Waiting -> {
                                    Text("Waiting for a guess")
                                }
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
                                            if (selected)
                                                synopsisHint.add(Pair(word, range))
                                            else
                                                synopsisHint.removeIf { pair -> pair.second == range }
                                        })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun GuessTitle(hint: Guess.TitleGuess) {
    Box(modifier = Modifier.height(40.dp).fillMaxWidth()) {
        Text(hint.title)
    }
}
@Composable
fun KeywordHint(hint: Hint.KeywordHint) {
    Box(modifier = Modifier.height(40.dp).fillMaxWidth()) {
        Text(hint.keyword)
    }
}