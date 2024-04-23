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
 * IN_GAME_ARG_GAME_NAME, IN_GAME_ARG_GAME_ID
 */
val GAME_GRAPH = NavRoute("game", pathParams = PathParams(IN_GAME_ARG_GAME_NAME), queryParams = QueryParams(
    IN_GAME_ARG_GAME_ID
))
/**
 * IN_GAME_ARG_GAME_NAME, IN_GAME_ARG_GAME_ID
 */
private val LOBBY_ROUTE = NavRoute(base = "lobby", pathParams = PathParams(IN_GAME_ARG_GAME_NAME), queryParams = QueryParams(
    IN_GAME_ARG_GAME_ID
))
private val GUESS_ROUTE = NavRoute("guess")

fun NavGraphBuilder.gameNavigationGraph(
    navigationController: NavController,
    route: String = GAME_GRAPH()
) {
    navigation(
        route = route,
        startDestination = LOBBY_ROUTE()) {
//        lobbyScreen(LOBBY_ROUTE(), navigationController)
    }
}

fun NavController.navigateToInGame() {
    navigate(LOBBY_ROUTE())
}
fun NavController.navigateToGuessScreen() {
    navigate(GUESS_ROUTE())
}