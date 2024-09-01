package com.imbaland.cinenigma.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.imbaland.cinenigma.ui.game.gameNavigationGraph
import com.imbaland.cinenigma.ui.menu.MENU_GRAPH
import com.imbaland.cinenigma.ui.menu.SETTINGS_ROUTE
import com.imbaland.cinenigma.ui.menu.menuNavigationGraph
import com.imbaland.cinenigma.ui.menu.settingsRoute
import com.imbaland.cinenigma.ui.theme.CinenigmaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        var uiState: MainActivityUiState by mutableStateOf(MainActivityUiState.Loading)

        // Update the uiState
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .onEach { uiState = it }
                    .collect()
            }
        }

        // Keep the splash screen on-screen until the UI state is loaded. This condition is
        // evaluated each time the app needs to be redrawn so it should be fast to avoid blocking
        // the UI.
        splashScreen.setOnExitAnimationListener {
            it.remove()
        }
        splashScreen.setKeepOnScreenCondition {
            when (uiState) {
                MainActivityUiState.Loading -> true
                is MainActivityUiState.Authenticated -> false
                is MainActivityUiState.ErrorState -> false
            }
        }
        setContent {
            CinenigmaApp()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CinenigmaApp() {
        CinenigmaTheme {
            Surface {
                val navController = rememberNavController()
                val canPop = remember { mutableStateOf(false) }

                navController.addOnDestinationChangedListener { controller, destination, _ ->
                    canPop.value = controller.previousBackStackEntry != null
                }

                Scaffold(topBar = {
                    TopAppBar(
                        modifier = Modifier,
                        title = { /*TODO*/ },
                        navigationIcon = {
                            if (canPop.value) {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Localized description"
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.width(48.dp))
                            }
                        },
                    )
                }) { padding ->
                    padding.calculateBottomPadding()
                    NavHost(
                        navController = navController,
                        startDestination = MENU_GRAPH,
                        modifier = Modifier.padding(top = padding.calculateTopPadding()),
                    ) {
                        menuNavigationGraph(navController)
                        gameNavigationGraph(navController)
                    }
                }
            }
        }
    }

    /**
     * The default light scrim, as defined by androidx and the platform:
     * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=35-38;drc=27e7d52e8604a080133e8b842db10c89b4482598
     */
    private val lightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)

    /**
     * The default dark scrim, as defined by androidx and the platform:
     * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=40-44;drc=27e7d52e8604a080133e8b842db10c89b4482598
     */
    private val darkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)
}
