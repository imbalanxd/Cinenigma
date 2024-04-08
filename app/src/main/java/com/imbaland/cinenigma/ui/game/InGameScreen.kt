package com.imbaland.cinenigma.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

const val IN_GAME_ARG_GAME_ID = "gameId"
fun NavGraphBuilder.inGameScreen(route: String, navController: NavController) {
    composable(
        route = route,
        arguments = listOf(navArgument(IN_GAME_ARG_GAME_ID) { type = NavType.StringType })) { backStackEntry ->
        InGameScreen(
            hiltViewModel<InGameViewModel>(),)
    }
}

@Composable
fun InGameScreen(
    viewModel: InGameViewModel,
) {
    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val uiState: InGameUiState by viewModel.uiState.collectAsStateWithLifecycle()
        val (buttons) = createRefs()
        Column(
            Modifier
                .constrainAs(buttons) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .fillMaxWidth(.5f), verticalArrangement = Arrangement.spacedBy(10.dp)) {

        }
    }
}