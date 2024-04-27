package com.imbaland.cinenigma.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.imbaland.cinenigma.R
import com.imbaland.cinenigma.domain.model.hostLabel
import com.imbaland.cinenigma.domain.model.playerLabel
import com.imbaland.cinenigma.ui.game.navigateToInGame
import kotlinx.coroutines.delay

const val IN_GAME_ARG_GAME_ID = "gameId"
const val IN_GAME_ARG_GAME_NAME = "gameName"
fun NavGraphBuilder.lobbyRoute(route: String, navController: NavController) {
    composable(
        route = route,
        arguments = listOf(
            navArgument(IN_GAME_ARG_GAME_NAME) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument(IN_GAME_ARG_GAME_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
        )
    ) {
        val menuViewModel =
            hiltViewModel<MenuViewModel>(remember(it) { navController.getBackStackEntry(it.destination.parent!!.route!!) })
        LobbyScreen(
            hiltViewModel<LobbyViewModel>(it),
            navController
        ) {
            menuViewModel.leftLobby()
            navController.navigateUp()
        }
    }
}

@Composable
fun LobbyScreen(
    viewModel: LobbyViewModel,
    navController: NavController = rememberNavController(),
    leftLobby: () -> Unit
) {
    val uiState: LobbyUiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val state = uiState) {
        is LobbyUiState.Closing -> {
            leftLobby()
        }
        is LobbyUiState.Started -> {
            navController.navigateToInGame(state.lobby.id)
        }
        else -> {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (buttons, countdown) = createRefs()
                (uiState as? LobbyUiState.Countdown)?.timeRemaining?.let { timeRemaining ->
                    var timer by remember { mutableIntStateOf(timeRemaining) }
                    LaunchedEffect(key1 = timer) {
                        if (timer > 0) {
                            delay(1_000)
                            timer -= 1
                        } else {
                            viewModel.startGame()
                        }
                    }
                    Text(modifier = Modifier.wrapContentSize()
                        .constrainAs(countdown) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }, text = "$timer")
                }
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
                        text = when (val state = uiState) {
                            is LobbyUiState.Created -> {
                                state.lobby.title
                            }
                            is LobbyUiState.Creating -> {
                                "Creating"
                            }
                            else -> {
                                "Error"
                            }
                        }
                    )
                    PlayerLobbySlot(
                        uiState is LobbyUiState.Created,
                        (uiState as? LobbyUiState.Created)?.lobby.hostLabel
                    )
                    PlayerLobbySlot(
                        uiState is LobbyUiState.Created,
                        (uiState as? LobbyUiState.Created)?.lobby.playerLabel
                    )
                    Spacer(modifier = Modifier.height(100.dp))
                    Row {
                        when(uiState) {
                            is Host.Full -> {
                                TextButton(onClick = viewModel::startLobby, enabled = true) {
                                    Text(text = stringResource(id = R.string.lobby_start))
                                }
                            }
                            is Host.Waiting -> {
                                TextButton(onClick = viewModel::startLobby, enabled = false) {
                                    Text(text = stringResource(id = R.string.lobby_start))
                                }
                            }
                            else -> {

                            }
                        }
                        TextButton(onClick = viewModel::leaveLobby) {
                            Text(text = stringResource(id = R.string.lobby_leave))
                        }
                    }
                }
            }
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