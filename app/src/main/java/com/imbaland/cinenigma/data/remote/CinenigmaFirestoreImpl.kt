package com.imbaland.cinenigma.data.remote

import com.imbaland.cinenigma.domain.model.Lobby
import com.imbaland.cinenigma.domain.remote.CinenigmaFirestore
import com.imbaland.common.data.database.firebase.FirestoreServiceImpl
import com.imbaland.common.domain.Result
import com.imbaland.common.domain.database.FirestoreError
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class CinenigmaFirestoreImpl: FirestoreServiceImpl(),
    CinenigmaFirestore {
    override suspend fun getLobbies(): Result<List<Lobby>, FirestoreError> {
        return readCollection<Lobby>("lobbies")
    }

    override suspend fun watchLobbies(): Flow<Result<List<Lobby>, FirestoreError>> {
        return watchCollection<Lobby>("lobbies")
    }

    override suspend fun getLobby(id: String): Result<Lobby, FirestoreError> {
        return readDocument<Lobby>("lobbies", id)
    }

    override suspend fun watchLobby(id: String): Flow<Result<Lobby?, FirestoreError>> {
        return watchDocument<Lobby>("lobbies", id)
    }

    override suspend fun createLobby(title: String): Result<Lobby, FirestoreError> {
        val id = UUID.randomUUID().toString()
        val lobby = Lobby(
            id = id,
            title = title
        )
        when(val result = writeDocument("lobbies", "title", lobby)) {
            is Result.Error -> return Result.Error(result.error)
            is Result.Success -> return Result.Success(lobby)
        }
    }
}