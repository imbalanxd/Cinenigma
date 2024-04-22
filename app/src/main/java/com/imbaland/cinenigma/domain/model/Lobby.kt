package com.imbaland.cinenigma.domain.model

import com.imbaland.common.domain.auth.AuthenticatedUser
import java.util.Date


/**
 * @param title The title displayed for the lobby
 * @param createdAt The time the lobby was created
 * @param hasStarted Is the game currently in progress
 * @param host Display name of the host
 * @param hostJoinedAt Time the host entered into the game
 * @param id ID of the lobby
 * @param player Display name of the joined player
 * @param playerJoinedAt Time the joined player entered into the game
 * @param startedAt Time the game moved to in progress
 */
data class Lobby(
    val id: String = "",
    val title: String = "",
    val host: AuthenticatedUser? = null,
    val hostUpdatedAt: Date? = null,
    val player: AuthenticatedUser? = null,
    val playerUpdatedAt: Date? = null,
    val hasStarted: Boolean = false,
    val createdAt: Date? = null,
    val startedAt: Date? = null,
    val games: List<Game> = listOf()
)
val Lobby?.hostLabel: String get() { return this?.host?.name?:"Waiting for host" }
val Lobby?.playerLabel: String get() { return this?.player?.name?:"Waiting for player" }

val Lobby?.state: LobbyState
    get() {
        if (this == null || this.id.isEmpty()) {
            return LobbyState.Invalid
        }
        val stateList = mutableListOf<Boolean>().apply {
            // 0 - Invalid
            if (!(this@state == null || this@state.id.isEmpty())) add(true)  // 1 - Open
            if (player != null) add(true)                            // 2 - Full
            if (startedAt != null) add(true)                                 // 3 - Starting
            if (hostUpdatedAt != null) add(true)                              // 4 - Waiting
            if (playerUpdatedAt != null) add(true)                            // 5 - Loading
            if (games.lastOrNull()?.let {
                    it.movie != null && !it.completed
                } == true) add(true)      // 6 - Playing
        }
        return LobbyState.values()[stateList.size]
    }

enum class LobbyState {
    Invalid,

    /**
     * Only one player is present in the lobby
     */
    Open,

    /**
     * Two players are present in the lobby
     */
    Full,

    /**
     * The lobbing is beginning but neither player is in game
     */
    Starting,

    /**
     * The host is in game but the joined player is not
     */
    Waiting,

    /**
     * Both players are in game but no movie is currently active
     */
    Loading,

    /**
     * Both players are in game and a movie is active
     */
    Playing,
}