//package com.imbaland.cinenigma.ui.game
//
//import android.os.Build
//import androidx.annotation.RequiresApi
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.material3.Button
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.unit.dp
//import androidx.constraintlayout.compose.ConstraintLayout
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import androidx.navigation.NavController
//import androidx.navigation.NavGraphBuilder
//import androidx.navigation.NavType
//import androidx.navigation.compose.composable
//import androidx.navigation.navArgument
//import com.imbaland.cinenigma.R
//import com.imbaland.cinenigma.ui.MainActivityUiState
//import com.imbaland.cinenigma.ui.menu.MenuUiState
//import com.imbaland.cinenigma.ui.menu.MenuViewModel
//import com.imbaland.cinenigma.ui.menu.navigateToLobbies
//import com.imbaland.cinenigma.ui.menu.navigateToPlay
//import com.imbaland.cinenigma.ui.menu.navigateToSettings
//import com.imbaland.common.ui.util.sharedViewModel
//import com.imbaland.movies.ui.widget.MoviePoster
//
//fun NavGraphBuilder.guessRoute(
//    route: String,
//    navController: NavController) {
//    composable(
//        route = route,
//        arguments = listOf(navArgument("gameId") { type = NavType.StringType })) {
//        GuessScreen(
//            hiltViewModel<GuessViewModel>())
//    }
//}
//
//@Composable
//fun GuessScreen(
//    viewModel: GuessViewModel
//) {
//    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
//        val uiState: GuessUiState by viewModel.uiState.collectAsStateWithLifecycle()
//        val (loading) = createRefs()
//        when(uiState) {
//            is GuessUiState.ErrorState -> TODO()
//            GuessUiState.Loading -> TODO()
//            GuessUiState.Playing -> TODO()
//            GuessUiState.Waiting -> TODO()
//        }
//        Column(
//            Modifier
//                .constrainAs(buttons) {
//                    top.linkTo(parent.top)
//                    bottom.linkTo(parent.bottom)
//                    start.linkTo(parent.start)
//                    end.linkTo(parent.end)
//                }
//                .fillMaxWidth(.5f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
//
//        }
//    }
//}