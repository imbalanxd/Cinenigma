package com.imbaland.cinenigma.data.remote

import com.imbaland.cinenigma.domain.CinenigmaError
import com.imbaland.cinenigma.domain.model.Lobby
import com.imbaland.cinenigma.domain.remote.CinenigmaFirestore
import com.imbaland.cinenigma.domain.remote.CinenigmaFirestoreError
import com.imbaland.common.data.auth.firebase.FirebaseUser
import com.imbaland.common.data.database.firebase.FirestoreServiceImpl
import com.imbaland.common.domain.Error
import com.imbaland.common.domain.Result
import com.imbaland.common.domain.database.FirestoreError
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.UUID

class CinenigmaFirestoreImpl constructor(
    val user: FirebaseUser?
): FirestoreServiceImpl(),
    CinenigmaFirestore {
    override suspend fun getLobbies(): Result<List<Lobby>, FirestoreError> {
        return readCollection<Lobby>("lobbies")
    }

    override suspend fun watchLobbies(filters: Map<String, Any>): Flow<Result<List<Lobby>, FirestoreError>> {
        return watchCollection<Lobby>("lobbies", filters)
    }

    override suspend fun getLobby(id: String): Result<Lobby, FirestoreError> {
        return readDocument<Lobby>("lobbies", id)
    }

    override suspend fun watchLobby(id: String): Flow<Result<Lobby?, FirestoreError>> {
        return watchDocument<Lobby>("lobbies", id)
    }

    override suspend fun createLobby(title: String): Result<Lobby, FirestoreError> {
        user?.let { user ->
            val id = UUID.randomUUID().toString()
            val lobby = Lobby(
                id = id,
                title = title,
                createdAt = Date(),
                host = user,
                hostJoinedAt = Date()
            )
            when(val result = writeDocument("lobbies", id, lobby)) {
                is Result.Error -> return Result.Error(result.error)
                is Result.Success -> return Result.Success(lobby)
            }
        }?: return Result.Error(CinenigmaFirestoreError.InvalidUserError)
    }
    override suspend fun joinLobby(id: String): Result<Unit, Error> {
        user?.let { user ->
            return updateValues("lobbies", id,
                listOf("player", "playerJoinedAt"),
                listOf(null, null),
                listOf(user, Date()),
                listOf()
            )
        }?: return Result.Error(CinenigmaFirestoreError.InvalidUserError)
    }
}