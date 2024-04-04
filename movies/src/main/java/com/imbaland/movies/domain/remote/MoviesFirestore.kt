package com.imbaland.movies.domain.remote

import com.imbaland.common.domain.database.FirestoreError
import com.imbaland.common.domain.Result
import com.imbaland.movies.domain.model.Lobby

interface MoviesFirestore {
    suspend fun getLobbies(): Result<List<Lobby>, FirestoreError>
    suspend fun getLobby(id: String): Result<Lobby, FirestoreError>
    suspend fun createLobby(title: String): Result<Lobby, FirestoreError>
}