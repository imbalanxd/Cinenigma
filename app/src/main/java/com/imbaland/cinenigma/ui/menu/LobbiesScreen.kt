package com.imbaland.cinenigma.ui.menu

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.JoinFull
import androidx.compose.material.icons.filled.JoinLeft
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.imbaland.cinenigma.domain.model.Lobby
import com.imbaland.cinenigma.domain.model.LobbyState
import com.imbaland.cinenigma.domain.model.hostLabel
import com.imbaland.cinenigma.domain.model.playerLabel
import com.imbaland.cinenigma.domain.model.state
import com.imbaland.cinenigma.ui.game.GAME_GRAPH
import kotlinx.coroutines.flow.collectLatest

fun NavGraphBuilder.lobbiesRoute(route: String, navController: NavController) {
    return composable(route = route) {
        LobbiesScreen(hiltViewModel<MenuViewModel>(remember(it){navController.getBackStackEntry(it.destination.parent!!.route!!)}),
            navController::navigateToLobby)
    }
}

@Composable
internal fun LobbiesScreen(viewModel: MenuViewModel,
                           onLobbyJoined: (Lobby) -> Unit,) {
    val uiState: MenuUiState by viewModel.uiState.collectAsStateWithLifecycle()
    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is MenuUiState.ErrorState -> {

            }
            is MenuUiState.Loaded -> {
                val (lobbies) = createRefs()
                LazyColumn(modifier = Modifier
                    .constrainAs(lobbies) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .fillMaxWidth(.85f)) {
                    items(state.lobbies) {
                        LobbyItem(modifier = Modifier.fillMaxWidth(), lobby = it, viewModel::joinLobby)
                    }
                }
            }
            is MenuUiState.InLobby -> {
                onLobbyJoined(state.joinedLobby)
            }
            else -> {

            }
        }
    }
}

@Composable
fun LobbyItem(
    modifier: Modifier = Modifier,
    lobby: Lobby,
    joinLobby: (Lobby) -> Unit) {
    ExtendedFloatingActionButton(
        modifier = modifier,
        onClick = { joinLobby(lobby) },
        icon = { Icon(if(lobby.state >= LobbyState.Full) Icons.Filled.JoinFull else Icons.Filled.JoinLeft, "Join Game!") },
        text = {
            Column {
                Text(text = lobby.title)
                Row {
                    Text(modifier = Modifier.padding(horizontal = 15.dp), text = lobby.hostLabel)
                    Text(modifier = Modifier.padding(horizontal = 15.dp), text = lobby.playerLabel)
                }
            }
        },
    )
}


