package com.imbaland.cinenigma.domain.remote

import com.imbaland.common.domain.database.FirestoreError
import com.imbaland.common.domain.Result
import com.imbaland.cinenigma.domain.model.Lobby
import com.imbaland.common.domain.Error
import kotlinx.coroutines.flow.Flow

interface CinenigmaFirestore {
    suspend fun getLobbies(): Result<List<Lobby>, FirestoreError>
    suspend fun watchLobbies(filters: Map<String, Any> = mapOf()): Flow<Result<List<Lobby>, FirestoreError>>
    suspend fun getLobby(id: String): Result<Lobby, FirestoreError>
    suspend fun watchLobby(id: String): Flow<Result<Lobby?, FirestoreError>>
    suspend fun createLobby(title: String): Result<Lobby, FirestoreError>
    suspend fun joinLobby(id: String): Result<Unit, Error>
}

sealed class CinenigmaFirestoreError: FirestoreError() {
    data object InvalidUserError: CinenigmaFirestoreError()
}
