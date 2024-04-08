package com.imbaland.cinenigma.ui.menu

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.settingsRoute(route: String) {
    composable(route = route) {
        SettingsScreen()
    }
}

@Composable
internal fun SettingsScreen() {

}