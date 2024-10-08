package com.imbaland.cinenigma.domain.remote

import com.imbaland.cinenigma.domain.model.Game
import com.imbaland.cinenigma.domain.model.Guess
import com.imbaland.cinenigma.domain.model.HintRound
import com.imbaland.common.domain.database.FirestoreError
import com.imbaland.common.domain.Result
import com.imbaland.cinenigma.domain.model.Lobby
import com.imbaland.common.domain.Error
import com.imbaland.common.domain.auth.AuthenticatedUser
import com.imbaland.movies.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface CinenigmaFirestore {
    /**
     * Menu Navs
     */
    suspend fun getLobbies(): Result<List<Lobby>, FirestoreError>
    suspend fun watchLobbies(filters: Map<String, Any?> = mapOf(), exclude: Pair<String, Any?>? = null): Flow<Result<List<Lobby>, FirestoreError>>
    /**
     * Watch all lobbies that user is currently a member of
     */
    suspend fun watchJoinedLobbies(): Flow<Result<List<Lobby>, FirestoreError>>
    suspend fun getLobby(id: String): Result<Lobby, FirestoreError>
    suspend fun watchLobby(id: String): Flow<Result<Lobby?, FirestoreError>>
    suspend fun createLobby(title: String): Result<Lobby, FirestoreError>
    suspend fun joinLobby(id: String): Result<Unit, Error>
    suspend fun leaveLobby(id: String): Result<Unit, Error>
    suspend fun coordinateLobby(id: String, isHost: Boolean): Result<Unit, Error>
    suspend fun startLobby(id: String): Result<Unit, Error>
    suspend fun startGame(id: String, gameNumber: Int,  hinter: AuthenticatedUser): Result<Unit, Error>
    suspend fun startGame(movie: Movie, id: String, gameNumber: Int,  hinter: AuthenticatedUser): Result<Unit, Error>
    /**
     * Game Navs
     */
    suspend fun watchGames(lobbyId: String): Flow<Result<List<Game>, FirestoreError>>
    suspend fun submitHint(lobbyId: String, gameNumber: Int, hint: HintRound): Result<Unit, Error>
    suspend fun submitGuess(lobbyId: String, gameNumber: Int, guess: Guess): Result<Unit, Error>
}

sealed class CinenigmaFirestoreError: FirestoreError() {
    data object InvalidUserError: CinenigmaFirestoreError()
    data object GameStartError: CinenigmaFirestoreError()
}
