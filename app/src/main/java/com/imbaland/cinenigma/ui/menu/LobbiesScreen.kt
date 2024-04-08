package com.imbaland.cinenigma.ui.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditAttributes
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.imbaland.cinenigma.domain.model.Lobby

fun NavGraphBuilder.lobbiesRoute(route: String, navController: NavController) {
    return composable(route = route) {
        LobbiesScreen(hiltViewModel<MenuViewModel>())
    }
}

@Composable
internal fun LobbiesScreen(viewModel: MenuViewModel) {
    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val uiState: MenuUiState by viewModel.uiState.collectAsStateWithLifecycle()
        when (uiState) {
            is MenuUiState.ErrorState -> TODO()
            is MenuUiState.IdleWithData -> {
                val (lobbies) = createRefs()
                LazyColumn(modifier = Modifier
                    .constrainAs(lobbies) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .fillMaxWidth(.85f)) {
                    items((uiState as MenuUiState.IdleWithData).lobbies) {
                        LobbyItem(modifier = Modifier.fillMaxWidth(), lobby = it)
                    }
                }
            }

            MenuUiState.Preloading -> CircularProgressIndicator()
        }
    }
}

@Composable
fun LobbyItem(modifier: Modifier = Modifier, lobby: Lobby) {
    ExtendedFloatingActionButton(
        modifier = modifier,
        onClick = {  },
        icon = { Icon(Icons.Filled.EditAttributes, "Join Game!") },
        text = {
            Column {
                Text(text = lobby.title)
                Text(text = lobby.host)
            }
        },
    )
}


