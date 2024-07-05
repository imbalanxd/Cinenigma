package com.imbaland.cinenigma.ui.game

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import coil.compose.rememberAsyncImagePainter
import com.imbaland.cinenigma.R
import com.imbaland.cinenigma.domain.model.Guess
import com.imbaland.cinenigma.domain.model.Guess.Holder.poster
import com.imbaland.cinenigma.domain.model.Hint
import com.imbaland.cinenigma.domain.model.Hint.EmptyHint.toRectF
import com.imbaland.cinenigma.domain.model.HintType
import com.imbaland.cinenigma.domain.model.createDisplay
import com.imbaland.cinenigma.domain.model.range
import com.imbaland.cinenigma.domain.model.toType
import com.imbaland.common.tool.logDebug
import com.imbaland.common.ui.UrlImage
import com.imbaland.movies.domain.model.Movie
import com.imbaland.movies.domain.model.MovieDetails
import com.imbaland.movies.ui.util.poster
import com.imbaland.movies.ui.widget.KeywordDisplay
import com.imbaland.movies.ui.widget.MovieAutoComplete
import com.imbaland.movies.ui.widget.MovieCast
import com.imbaland.movies.ui.widget.MovieCastGuesser
import com.imbaland.movies.ui.widget.MovieOverview
import com.imbaland.movies.ui.widget.MoviePosterGuess
import com.imbaland.movies.ui.widget.MoviePosterHint

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun GameScreen(
    viewModel: GameViewModel
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val uiState: GameUiState by viewModel.uiState.collectAsStateWithLifecycle()
        when (val state = uiState) {
            GameUiState.Closing -> {

            }

            is GameUiState.Error -> {

            }

            GameUiState.Loading -> {

            }

            is Setup.Choosing -> {
                Text(modifier = Modifier.align(Alignment.TopCenter), text = "In Game!")
                MulliganMovie(modifier = Modifier.align(Alignment.Center).fillMaxWidth(), state.options, viewModel::newGame)
            }

            is Setup.Waiting -> {
                Text(modifier = Modifier.align(Alignment.TopCenter), text = "In Game!")
            }

            is ActiveGame -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val hints = state.currentGame.hints
                    var selectedCategory by remember(hints) {
                        mutableStateOf(
                            hints?.lastOrNull()?.toType() ?: HintType.Poster
                        )
                    }
                    CategorySelection(
                        current = selectedCategory,
                        onClick = { category -> selectedCategory = category },
                        count = hints?.groupBy { it.toType() }?.mapValues { it.value.size }
                            ?: mapOf()
                    )
                    when (state) {
                        is Guesser -> {
                            when (state) {
                                is Guesser.Waiting -> {
                                    Text("Waiting for the next hint")
                                }

                                is Guesser.Guessing -> {
                                    val movieOptions by viewModel.searchResults.collectAsState()
                                    MovieAutoComplete(
                                        modifier = Modifier.fillMaxWidth().padding(15.dp),
                                        options = movieOptions.map { movie ->
                                            Pair(
                                                movie.id,
                                                movie.name
                                            )
                                        },
                                        onTextChanged = viewModel::performMovieSearch,
                                        onSubmit = viewModel::submitGuess
                                    )
                                    when (selectedCategory) {
                                        HintType.Keyword -> {
                                            val keywordHints = remember(state.currentGame.hints) {
                                                state.currentGame.hints?.filter { it.hint() is Hint.KeywordHint }
                                                    ?.map { it.hint() as Hint.KeywordHint }
                                                    ?: listOf()
                                            }
                                            KeywordDisplay(
                                                fullText = state.currentGame.movie?.overview ?: "",
                                                keywords = keywordHints.map { it.range }
                                            )
                                        }

                                        HintType.Poster -> {
                                            state.currentGame.hints?.findLast { it.hint() is Hint.PosterHint }
                                                ?.hint()?.let {
                                                    MoviePosterGuess(
                                                        poster = state.currentGame.movie?.image
                                                            ?: "",
                                                        area = (it as Hint.PosterHint).toRectF()
                                                    )
                                                }
                                        }

                                        HintType.CastMovie -> {
                                            val castHints = remember(state.currentGame.hints) {
                                                state.currentGame.hints?.filter { it.hint() is Hint.CastMovieHint }
                                                    ?.map { it.hint() as Hint.CastMovieHint }
                                                    ?: listOf()
                                            }
                                            MovieCastGuesser(
                                                modifier = Modifier.fillMaxWidth(1f).height(200.dp),
                                                cast = castHints.createDisplay()
                                            )
                                        }

                                        else -> {

                                        }
                                    }
                                }
                            }
                        }

                        is Hinter -> {
                            val synopsisHint = remember { mutableListOf<Pair<String, IntRange>>() }
                            state.currentGame.movie?.let {
                                TargetDisplay(state.currentGame.movie, viewModel::newGame)
                            }
                            state.currentGame.guesses?.let { guesses ->
                                GuessesDisplay(guesses)
                            }

                            when (state) {
                                is Hinter.Waiting -> {
                                    Text("Waiting for a guess")
                                }

                                is Hinter.Hinting -> {
                                    when (selectedCategory) {
                                        HintType.Poster -> {
                                            MoviePosterHint(modifier = Modifier.fillMaxWidth(0.7f),
                                                poster = state.currentGame.movie?.image
                                                    ?: "", onSubmit = { rect, blur ->
                                                    viewModel.submitHint(
                                                        Hint.PosterHint(
                                                            x = rect.left.toFloat(),
                                                            y = rect.top.toFloat(),
                                                            size = rect.width().toFloat()
                                                        )
                                                    )
                                                })
                                        }

                                        HintType.Keyword -> {
                                            val keywordHints =
                                                remember(state.currentGame.hints) {
                                                    state.currentGame.hints?.filter { it.hint() is Hint.KeywordHint }
                                                        ?.map { it.hint() as Hint.KeywordHint }
                                                        ?.map { it.range } ?: listOf()
                                                }
                                            MovieOverview(modifier = Modifier.fillMaxWidth(0.8f),
                                                fullText = state.currentGame.movie?.overview
                                                    ?: "none lol", filter = { selection ->
                                                    state.currentGame.movie?.name?.contains(
                                                        selection
                                                    ) != true
                                                }, keywords = keywordHints, onSubmit = { keywords ->
                                                    keywords.lastOrNull()?.let {
                                                        viewModel.submitHint(
                                                            Hint.KeywordHint(
                                                                it.first,
                                                                it.second.first.toLong(),
                                                                it.second.last.toLong()
                                                            )
                                                        )
                                                    }
                                                })
                                        }

                                        HintType.CastMovie -> {
                                            state.currentGame.movie?.let { movie ->
                                                MovieCast(modifier = Modifier.fillMaxWidth(1f)
                                                    .height(200.dp),
                                                    cast = movie.actors,
                                                    onSubmit = { castId, movieId ->
                                                        movie.actors.getOrNull(castId)?.filmography?.getOrNull(
                                                            movieId
                                                        )?.let { selectedMovie ->
                                                            viewModel.submitHint(
                                                                Hint.CastMovieHint(
                                                                    castId.toLong(),
                                                                    movie.actors.size.toLong(),
                                                                    movieId.toLong(),
                                                                    movie.actors.first().filmography.size.toLong(),
                                                                    selectedMovie.title,
                                                                    selectedMovie.image
                                                                )
                                                            )
                                                        }
                                                            ?: logDebug("Error when selecting cast hint")
                                                    })
                                            }
                                        }

                                        HintType.Empty -> {

                                        }

                                    }
                                }
                            }
                        }

                        else -> {

                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun MulliganMovie(
    modifier: Modifier = Modifier,
    options: List<Movie> = List(3){Movie()},
    onSelect: (Movie) -> Unit = {_ -> }
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween) {
        options.forEach {
            UrlImage(modifier = Modifier.weight(1f).clickable {
                onSelect(it)
            }.poster().padding(10.dp).clip(RoundedCornerShape(5.dp)), url = it.image)
        }
    }
}

@Composable
fun TargetDisplay(
    movie: MovieDetails,
    refresh: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Movie: ${movie.name}")
        ConstraintLayout {
            val (newGame, image) = createRefs()
            Image(
                modifier = Modifier.height(140.dp).width(100.dp).constrainAs(image) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
                contentScale = ContentScale.Crop,
                contentDescription = "Poster", painter = rememberAsyncImagePainter(
                    model = movie.image
                )
            )
            IconButton(
                modifier = Modifier.size(30.dp).constrainAs(newGame) {
                    top.linkTo(image.top)
                    end.linkTo(image.start)
                },
                onClick = refresh
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.RotateLeft,
                    contentDescription = "Localized description"
                )
            }
        }
    }
}

@Composable
fun GuessesDisplay(guesses: List<Guess.MovieGuess>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Guesses")
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.LightGray), contentPadding = PaddingValues(horizontal = 10.dp)
        ) {
            items(guesses.reversed()) {
                Image(
                    modifier = Modifier.height(100.dp).aspectRatio(2 / 3f),
                    contentScale = ContentScale.Crop,
                    contentDescription = "Poster", painter = rememberAsyncImagePainter(
                        model = it.poster
                    )
                )
            }
        }
    }
}

@Preview
@Composable
fun CategorySelection(
    modifier: Modifier = Modifier,
    onClick: (HintType) -> Unit = { _ -> },
    current: HintType = HintType.Poster,
    count: Map<HintType, Int> = mapOf(
        HintType.CastMovie to 2,
        HintType.Poster to 1,
        HintType.Keyword to 11
    )
) {
    val categories = listOf(
        Pair(HintType.CastMovie, com.imbaland.movies.R.drawable.ic_cast),
        Pair(HintType.Poster, com.imbaland.movies.R.drawable.ic_poster),
        Pair(HintType.Keyword, com.imbaland.movies.R.drawable.ic_overview)
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(categories.size) { index ->
            val category = categories[index]
            ConstraintLayout(
                modifier = Modifier.wrapContentSize().background(
                    colorResource(if (category.first == current) R.color.mauve_selected else R.color.mauve),
                    CircleShape
                )
            ) {
                val (button, counter) = createRefs()
                IconButton(
                    modifier = Modifier
                        .constrainAs(button) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            end.linkTo(parent.end)
                            start.linkTo(parent.start)
                        },
                    onClick = { onClick(category.first) }
                ) {
                    Icon(
                        painter = painterResource(category.second),
                        tint = colorResource(R.color.white),
                        contentDescription = "Localized description"
                    )
                }
                count[category.first]?.let { count ->
                    Text(
                        modifier = Modifier.drawBehind {
                            drawCircle(
                                color = Color.Red,
                                radius = this.size.maxDimension / 2f
                            )
                        }.constrainAs(counter) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                        },
                        text = "$count", fontWeight = FontWeight.Black,
                        color = colorResource(R.color.white)
                    )
                }
            }
        }
    }
}