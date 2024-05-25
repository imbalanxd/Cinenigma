package com.imbaland.cinenigma.ui.menu

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.imbaland.cinenigma.R
import com.imbaland.cinenigma.domain.model.Lobby
import com.imbaland.cinenigma.ui.MainActivityUiState
import com.imbaland.common.ui.util.sharedViewModel
import com.imbaland.movies.ui.widget.MovieAutoComplete
import com.imbaland.movies.ui.widget.MoviePoster

fun NavGraphBuilder.landingRoute(route: String, navController: NavController) {
    composable(route = route) {
        LandingScreen(
            hiltViewModel<MenuViewModel>(remember(it){navController.getBackStackEntry(it.destination.parent!!.route!!)}),
            navController::navigateToLobby,
            navController::navigateToLobbies,
            navController::navigateToSettings,
            {})
    }
}

@Composable
fun LandingScreen(
    viewModel: MenuViewModel,
    onPlayClicked: (Lobby?) -> Unit,
    onLobbiesClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onQuitClicked: () -> Unit
) {
    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val uiState: MenuUiState by viewModel.uiState.collectAsStateWithLifecycle()
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
            Button(modifier = Modifier.fillMaxWidth(), onClick = { onPlayClicked(null) }) {
                Text(text = stringResource(id = R.string.menu_play))
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = onLobbiesClicked) {
                Text(text = stringResource(id = R.string.menu_lobbies))
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = onSettingsClicked) {
                Text(text = stringResource(id = R.string.menu_settings))
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = onQuitClicked) {
                Text(text = stringResource(id = R.string.menu_quit))
            }
        }
    }
}