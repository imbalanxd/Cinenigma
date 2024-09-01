package com.imbaland.cinenigma.ui.menu

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toAndroidRectF
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.imbaland.cinenigma.R
import com.imbaland.cinenigma.domain.model.Lobby
import com.imbaland.cinenigma.ui.widget.MoviePosterr
import com.imbaland.common.ui.GLBitmapImageView
import com.imbaland.common.ui.widget.DragWindowLayout
import com.imbaland.common.ui.widget.rememberDragState
import com.imbaland.movies.ui.util.poster
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.sample

fun NavGraphBuilder.landingRoute(route: String, navController: NavController) {
    composable(route = route) {
        val viewModel = hiltViewModel<MenuViewModel>(remember(it) { navController.getBackStackEntry(it.destination.parent!!.route!!) })
        val uiState: MenuUiState by viewModel.uiState.collectAsStateWithLifecycle()
        when(val state = uiState) {
            is MenuUiState.ErrorState -> {}
            is MenuUiState.InLobby -> {
                navController.navigateToLobby(state.joinedLobby)
            }
            is MenuUiState.Loaded -> {}
            MenuUiState.Loading -> {}
            is MenuUiState.Preloading -> {}
        }
        LandingScreen(
            viewModel,
            navController::navigateToLobby,
            navController::navigateToLobbies,
            navController::navigateToSettings,
            navController::navigateToDebug,
            {})
    }
}

@Composable
fun LandingScreen(
    viewModel: MenuViewModel,
    onPlayClicked: (Lobby?) -> Unit,
    onLobbiesClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onDebugClicked: () -> Unit,
    onQuitClicked: () -> Unit
) {
    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
//        val uiState: MenuUiState by viewModel.uiState.collectAsStateWithLifecycle()
//        when(val state = uiState) {
//            is MenuUiState.ErrorState -> {}
//            is MenuUiState.InLobby -> {
//                onPlayClicked(state.joinedLobby)
//            }
//            is MenuUiState.Loaded -> {}
//            MenuUiState.Loading -> {}
//            is MenuUiState.Preloading -> {}
//        }
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
            Button(modifier = Modifier.fillMaxWidth(), onClick = { viewModel.createLobby() }) {
                Text(text = stringResource(id = R.string.menu_play))
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = onLobbiesClicked) {
                Text(text = stringResource(id = R.string.menu_lobbies))
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = onSettingsClicked) {
                Text(text = stringResource(id = R.string.menu_settings))
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = onDebugClicked) {
                Text(text = stringResource(id = R.string.menu_debug))
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = onQuitClicked) {
                Text(text = stringResource(id = R.string.menu_quit))
            }
        }
    }
}