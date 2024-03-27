package com.imbaland.common.domain.auth

interface Authenticator {
    val isAuthenticated: Boolean
    val account:AuthenticatedUser?
}
 interface AnonymousAuthenticator: Authenticator {
     suspend fun login(): Result<AuthenticatedUser, AuthenticationError>
     suspend fun logout(): Result<Unit, AuthenticationError>
 }

interface AuthenticatedUser {
    val id: String
    val name: String
}

enum class AuthenticationError: Error {
    GENERAL_ERROR,
    NULL_USER
}

//sealed class AuthenticationResult: Result<AuthenticatedUser, > {
//    data class Success(val user: AuthenticatedUser)
//    sealed class GeneralError()
//}