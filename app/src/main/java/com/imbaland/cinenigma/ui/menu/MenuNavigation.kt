package com.imbaland.cinenigma.ui.menu

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.imbaland.cinenigma.ui.game.GAME_GRAPH

const val MENU_GRAPH = "menu"
const val LANDING_ROUTE = "landing"
const val LOBBIES_ROUTE = "lobbies"
const val SETTINGS_ROUTE = "settings"

fun NavGraphBuilder.menuNavigationGraph(
    navigationController: NavController,
    route: String = MENU_GRAPH
) {
    navigation(
        route = route,
        startDestination = LANDING_ROUTE) {
        landingRoute(LANDING_ROUTE, navigationController)
        lobbiesRoute(LOBBIES_ROUTE, navigationController)
        settingsRoute(SETTINGS_ROUTE)
    }
}
fun NavController.navigateToPlay(gameName: String, gameId: String?) {
    navigate(GAME_GRAPH(gameName, gameId))
}
fun NavController.navigateToLobbies() {
    navigate(LOBBIES_ROUTE)
}
fun NavController.navigateToSettings() {
    navigate(SETTINGS_ROUTE)
}
