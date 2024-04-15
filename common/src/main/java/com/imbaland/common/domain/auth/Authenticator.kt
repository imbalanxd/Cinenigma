package com.imbaland.common.domain.auth

import com.imbaland.common.domain.Error
import com.imbaland.common.domain.Result

interface Authenticator {
    val isAuthenticated: Boolean
    val account:AuthenticatedUser?
}
 interface AnonymousAuthenticator: Authenticator {
     suspend fun login(): Result<AuthenticatedUser, AuthenticationError>
     suspend fun logout(): Result<Unit, AuthenticationError>
 }

open class AuthenticatedUser(val id: String = "", val name: String = "") {
}

enum class AuthenticationError: Error {
    GENERAL_ERROR,
    NULL_USER
}