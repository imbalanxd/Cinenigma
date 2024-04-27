package com.imbaland.cinenigma.ui.game

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.imbaland.cinenigma.ui.menu.IN_GAME_ARG_GAME_ID
import com.imbaland.cinenigma.ui.menu.IN_GAME_ARG_GAME_NAME
import com.imbaland.common.ui.util.NavRoute
import com.imbaland.common.ui.util.PathParams
import com.imbaland.common.ui.util.QueryParams
/**
 * IN_GAME_ARG_GAME_ID
 */
val GAME_GRAPH = NavRoute("game", pathParams = PathParams(
    IN_GAME_ARG_GAME_ID
)
)
/**
 * IN_GAME_ARG_GAME_ID
 */
private val GAME_ROUTE = NavRoute(base = "lobby", pathParams = PathParams(IN_GAME_ARG_GAME_ID))

fun NavGraphBuilder.gameNavigationGraph(
    navigationController: NavController,
    route: String = GAME_GRAPH()
) {
    navigation(
        route = route,
        startDestination = GAME_ROUTE()) {
        gameRoute(GAME_ROUTE(), navigationController)
    }
}

fun NavController.navigateToInGame(gameId: String) {
    navigate(GAME_ROUTE(gameId))
}