package com.imbaland.cinenigma.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
import com.imbaland.cinenigma.R
import com.imbaland.cinenigma.domain.model.hostLabel
import com.imbaland.cinenigma.domain.model.playerLabel

const val IN_GAME_ARG_GAME_ID = "gameId"
const val IN_GAME_ARG_GAME_NAME = "gameName"
fun NavGraphBuilder.lobbyScreen(route: String, navController: NavController) {
    composable(
        route = route,
        arguments = listOf(
            navArgument(IN_GAME_ARG_GAME_NAME) {
                type = NavType.StringType
                defaultValue = "Cinenigma Lobby"
            },
            navArgument(IN_GAME_ARG_GAME_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },)
    ) { backStackEntry ->
        LobbyScreen(
            hiltViewModel<LobbyViewModel>(),
        )
    }
}

@Composable
fun LobbyScreen(
    viewModel: LobbyViewModel,
) {
    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val uiState: LobbyUiState by viewModel.uiState.collectAsStateWithLifecycle()
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
            Text(text = stringResource(id = R.string.app_name))
            Text(
                text = when (uiState) {
                    is LobbyUiState.Creating -> {
                        (uiState as LobbyUiState.Creating).lobbyTitle
                    }

                    is LobbyUiState.Activated -> {
                        (uiState as LobbyUiState.Activated).lobby.title
                    }

                    else -> {
                        "Error"
                    }
                }
            )
            PlayerLobbySlot(
                uiState is LobbyUiState.Activated,
                (uiState as? LobbyUiState.Activated)?.lobby.hostLabel
            )
            PlayerLobbySlot(
                uiState is LobbyUiState.Activated,
                (uiState as? LobbyUiState.Activated)?.lobby.playerLabel
            )
//            when(uiState) {
//                is LobbyUiState.Creating -> {
//
//                }
//                is LobbyUiState.Error -> {
//
//                }
//                is LobbyUiState.Starting -> {
//
//                }
//                is LobbyUiState.Waiting -> {
//
//                }
//            }
        }
    }
}

@Preview
@Composable
fun PlayerLobbySlot(active: Boolean = false, name: String? = "PlayerA") {
    Box(
        modifier = Modifier
            .width(100.dp)
            .height(20.dp)
            .background(if (active) Color.Cyan else Color.Gray, RoundedCornerShape(10.dp))
    ) {
        Text(text = name ?: "")
    }
}