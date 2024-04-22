package com.imbaland.cinenigma.ui.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

fun NavGraphBuilder.settingsRoute(route: String, navController: NavController) {
    composable(route = route) {
        SettingsScreen(hiltViewModel<MenuViewModel>(remember(it){navController.getBackStackEntry(it.destination.parent!!.route!!)}),)
    }
}

@Composable
internal fun SettingsScreen(viewModel: MenuViewModel) {
    val uiState: MenuUiState by viewModel.uiState.collectAsStateWithLifecycle()
    when(val state = uiState) {
        else -> {
            var displayName = remember {
                mutableStateOf(state.user?.name?:"")
            }
            LaunchedEffect(displayName) {
                snapshotFlow {
                    displayName.value
                }
                    .debounce(1500L)
                    .collectLatest { name ->
                        viewModel.changeName(name)
                    }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                TextField(value = displayName.value, onValueChange = { value -> displayName.value = value})
            }
        }
    }
}