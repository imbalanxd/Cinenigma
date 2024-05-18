package com.imbaland.cinenigma.data.remote

import com.imbaland.cinenigma.domain.model.Game
import com.imbaland.cinenigma.domain.model.Guess
import com.imbaland.cinenigma.domain.model.Hint
import com.imbaland.cinenigma.domain.model.Lobby
import com.imbaland.cinenigma.domain.remote.CinenigmaFirestore
import com.imbaland.cinenigma.domain.remote.CinenigmaFirestoreError
import com.imbaland.common.data.auth.firebase.FirebaseUser
import com.imbaland.common.data.database.firebase.FirestoreServiceImpl
import com.imbaland.common.domain.Error
import com.imbaland.common.domain.Result
import com.imbaland.common.domain.auth.AuthenticatedUser
import com.imbaland.common.domain.database.FirestoreError
import com.imbaland.movies.domain.repository.MoviesRepository
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date
import kotlin.random.Random

class CinenigmaFirestoreImpl constructor(
    val user: FirebaseUser?,
    val moviesRepository: MoviesRepository
) : FirestoreServiceImpl(),
    CinenigmaFirestore {
    override suspend fun getLobbies(): Result<List<Lobby>, FirestoreError> {
        return readCollection<Lobby>("lobbies")
    }

    override suspend fun watchLobbies(
        filters: Map<String, Any>,
        exclude: Pair<String, Any?>?
    ): Flow<Result<List<Lobby>, FirestoreError>> {
        return watchCollection<Lobby>("lobbies", filters, exclude)
    }

    override suspend fun getLobby(id: String): Result<Lobby, FirestoreError> {
        return readDocument<Lobby>("lobbies", id)
    }

    override suspend fun watchLobby(id: String): Flow<Result<Lobby?, FirestoreError>> {
        return watchDocument<Lobby>("lobbies", id)
    }

    override suspend fun createLobby(title: String): Result<Lobby, FirestoreError> {
        user?.let { user ->
            val id = user.id
            val lobby = Lobby(
                id = id,
                title = title,
                host = user,
                hostUpdatedAt = Date()
            )
            when (val result = writeDocument("lobbies", id, lobby)) {
                is Result.Error -> {
                    return Result.Error(result.error)
                }
                is Result.Success -> {
                    return Result.Success(lobby)
                }
            }
        } ?: return Result.Error(CinenigmaFirestoreError.InvalidUserError)
    }

    override suspend fun joinLobby(id: String): Result<Unit, Error> {
        user?.let { user ->
            return updateValues(
                "lobbies", id,
                listOf("player", "playerJoinedAt"),
                listOf(null, null),
                listOf(user, Date()),
                listOf()
            )
        } ?: return Result.Error(CinenigmaFirestoreError.InvalidUserError)
    }

    override suspend fun leaveLobby(id: String, isHost: Boolean): Result<Unit, Error> {
        user?.let { user ->
            return updateValues(
                "lobbies", id,
                params = if (isHost) listOf("host", "hostUpdatedAt") else listOf(
                    "player",
                    "playerUpdatedAt"
                ),
                targetValue = listOf(null, null),
                throws = listOf()
            )
        } ?: return Result.Error(CinenigmaFirestoreError.InvalidUserError)
    }
    override suspend fun coordinateLobby(id: String, isHost: Boolean): Result<Unit, Error> {
        return updateValues(
            "lobbies", id,
            listOf(if(isHost) "hostStartedAt" else "playerStartedAt"),
            listOf(null),
            listOf(Calendar.getInstance().run {
                add(Calendar.SECOND, 5)
                time
            }),
            listOf()
        )
    }
    override suspend fun startLobby(id: String): Result<Unit, Error> {
        return updateValues(
            "lobbies", id,
            listOf("gameStartedAt"),
            listOf(null),
            listOf(Date()),
            listOf()
        )
    }
    override suspend fun startGame(id: String, hinter: AuthenticatedUser): Result<Unit, Error> {
        when(val result = moviesRepository.discover(Random.nextInt(1, 100))) {
            is Result.Success -> {
                val movie = result.data[Random.nextInt(0, 20)]
                when(val detailsResult = moviesRepository.getMovieDetails(movie.id)) {
                    is Result.Error -> {
                        return Result.Error(CinenigmaFirestoreError.GameStartError)
                    }
                    is Result.Success -> {
                        val newGame = Game(
                            movie = detailsResult.data,
                            hinter = hinter)
                        val result = updateValues(
                            "lobbies", id,
                            listOf("gameStartedAt", "activeGame"),
                            listOf(null, null),
                            listOf(Date(), newGame),
                            listOf()
                        )
                        when(result) {
                            is Result.Success -> {
                                return result
                            }
                            is Result.Error -> {
                                return result
                            }
                        }
                    }
                }
            }
            is Result.Error -> {
                return Result.Error(CinenigmaFirestoreError.GameStartError)
            }
        }
    }
    override suspend fun startHint(lobby: Lobby, hint: Hint): Result<Unit, Error> {
        val rounds = lobby.activeGame?.rounds
        val result = updateValues(
            "lobbies", lobby.id,
            listOf("activeGame.completed"),
            listOf(false),
            listOf(true),
            listOf()
        )
        return result
    }

    override suspend fun startGuess(lobby: Lobby, hint: Guess): Result<Unit, Error> {
        TODO("Not yet implemented")
    }
}