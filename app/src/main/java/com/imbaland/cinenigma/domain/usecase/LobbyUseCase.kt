package com.imbaland.cinenigma.domain.usecase

import com.imbaland.cinenigma.domain.model.Lobby
import com.imbaland.cinenigma.domain.remote.CinenigmaFirestore
import com.imbaland.common.domain.Result
import com.imbaland.common.domain.RootError
import javax.inject.Inject

class CreateLobbyUseCase @Inject constructor(
    private val cinenigmaFirestore: CinenigmaFirestore
) {
    suspend operator fun invoke(gameName: String, gameId: String? = null): Result<Lobby, LobbyError.LobbyCreationError> {
        return when (val result = cinenigmaFirestore.createLobby(gameName)) {
                is Result.Error -> {
                    Result.Error(LobbyError.LobbyCreationError)
                }
                is Result.Success -> {
                   Result.Success(result.data)
                }
            }
    }
}



sealed class LobbyError: RootError {
    data object LobbyCreationError : LobbyError()
    data object LobbyJoiningError : LobbyError()
}