package com.imbaland.cinenigma.ui.game

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation

const val GAME_GRAPH = "game"
const val IN_GAME_ROUTE = "in_game"
const val GUESS_ROUTE = "guess"

fun NavGraphBuilder.gameNavigationGraph(
    navigationController: NavController,
    route: String = GAME_GRAPH
) {
    navigation(
        route = route,
        startDestination = "$IN_GAME_ROUTE/{$IN_GAME_ARG_GAME_ID}") {
        inGameScreen("$IN_GAME_ROUTE/{$IN_GAME_ARG_GAME_ID}", navigationController)
    }
}

fun NavController.navigateToInGame() {
    navigate(IN_GAME_ROUTE)
}
fun NavController.navigateToGuessScreen() {
    navigate(GUESS_ROUTE)
}