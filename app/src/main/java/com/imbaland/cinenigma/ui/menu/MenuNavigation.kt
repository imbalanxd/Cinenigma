package com.imbaland.cinenigma.ui.menu

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.imbaland.cinenigma.domain.model.Lobby
import com.imbaland.common.ui.util.NavRoute
import com.imbaland.common.ui.util.QueryParams

const val MENU_GRAPH = "menu"
const val LANDING_ROUTE = "landing"
const val LOBBIES_ROUTE = "lobbies"
const val SETTINGS_ROUTE = "settings"
/**
 * IN_GAME_ARG_GAME_NAME, IN_GAME_ARG_GAME_ID
 */
val LOBBY_ROUTE = NavRoute(base = "lobby", queryParams = QueryParams(
    IN_GAME_ARG_GAME_NAME,
    IN_GAME_ARG_GAME_ID
))

fun NavGraphBuilder.menuNavigationGraph(
    navigationController: NavController,
    route: String = MENU_GRAPH
) {
    navigation(
        route = route,
        startDestination = LANDING_ROUTE) {
        landingRoute(LANDING_ROUTE, navigationController)
        lobbiesRoute(LOBBIES_ROUTE, navigationController)
        lobbyRoute(LOBBY_ROUTE(), navigationController)
        settingsRoute(SETTINGS_ROUTE, navigationController)
    }

}
fun NavController.navigateToLobbies() {
    navigate(LOBBIES_ROUTE)
}
fun NavController.navigateToLobby(lobby: Lobby? = null) {
    navigate(LOBBY_ROUTE(lobby?.title, lobby?.id))
}
fun NavController.navigateToSettings() {
    navigate(SETTINGS_ROUTE)
}
