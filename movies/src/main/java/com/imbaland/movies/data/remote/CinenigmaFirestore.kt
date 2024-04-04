package com.imbaland.movies.data.remote

import com.imbaland.common.data.database.firebase.FirestoreServiceImpl
import com.imbaland.common.domain.Result
import com.imbaland.common.domain.database.FirestoreError
import com.imbaland.movies.domain.model.Lobby
import com.imbaland.movies.domain.remote.MoviesFirestore
import java.time.Instant
import java.time.ZonedDateTime
import java.util.Date
import java.util.UUID

class CinenigmaFirestore: FirestoreServiceImpl(), MoviesFirestore {
    override suspend fun getLobbies(): Result<List<Lobby>, FirestoreError> {
        return readCollection<Lobby>("lobbies")
    }

    override suspend fun getLobby(id: String): Result<Lobby, FirestoreError> {
        return readDocument<Lobby>("lobbies", id)
    }

    override suspend fun createLobby(title: String): Result<Lobby, FirestoreError> {
        val id = UUID.randomUUID()
        val lobby = Lobby(title, "lol", true, Date.from(Instant.now()))
        when(val result = writeDocument("lobbies", "title", lobby)) {
            is Result.Error -> return Result.Error(result.error)
            is Result.Success -> return Result.Success(lobby)
        }
    }
}