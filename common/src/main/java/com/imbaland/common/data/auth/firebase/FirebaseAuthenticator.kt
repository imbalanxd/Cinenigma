package com.imbaland.common.data.auth.firebase

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.imbaland.common.domain.auth.AnonymousAuthenticator
import com.imbaland.common.domain.auth.AuthenticationError
import com.imbaland.common.domain.auth.Result
import kotlinx.coroutines.CompletableDeferred

class FirebaseAuthenticator(): AnonymousAuthenticator {
    private val firebaseAuth: FirebaseAuth = Firebase.auth
    override val isAuthenticated: Boolean
        get() = firebaseAuth.currentUser != null
    override val account: FirebaseUser?
        get() = firebaseAuth.currentUser?.let { FirebaseUser(it) }

    override suspend fun login(): Result<FirebaseUser, AuthenticationError> {
        if(isAuthenticated) {
            val currentUser = account
            if(currentUser == null) {
                logout()
            } else {
                return Result.Success(currentUser)
            }
        }
        val deferred = CompletableDeferred<Result<FirebaseUser, AuthenticationError>>()
        firebaseAuth.signInAnonymously().addOnCompleteListener {
            it.result.user?.let { user ->
                deferred.complete(Result.Success(FirebaseUser(user)))
            } ?: deferred.complete(Result.Error(AuthenticationError.NULL_USER))
        }.addOnFailureListener { error ->
            when(error) {
                else -> deferred.complete(Result.Error(AuthenticationError.GENERAL_ERROR))
            }
        }
        return deferred.await()
    }

    override suspend fun logout(): Result<Unit, AuthenticationError> {
        firebaseAuth.signOut()
        return Result.Success(Unit)
    }
}