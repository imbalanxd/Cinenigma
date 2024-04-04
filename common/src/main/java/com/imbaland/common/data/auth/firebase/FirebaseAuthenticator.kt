package com.imbaland.common.data.auth.firebase

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.imbaland.common.domain.auth.AnonymousAuthenticator
import com.imbaland.common.domain.auth.AuthenticationError
import com.imbaland.common.domain.Result
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class FirebaseAuthenticator(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
): AnonymousAuthenticator {
    private val firebaseAuth: FirebaseAuth = Firebase.auth
    override val isAuthenticated: Boolean
        get() = firebaseAuth.currentUser != null
    override val account: FirebaseUser?
        get() = firebaseAuth.currentUser?.let { FirebaseUser(it) }

    override suspend fun login(): Result<FirebaseUser, AuthenticationError> = withContext(dispatcher) {
        if(isAuthenticated) {
            val currentUser = account
            if(currentUser == null) {
                logout()
            } else {
                Result.Success(currentUser)
            }
        }
//        val deferred = CompletableDeferred<Result<FirebaseUser, AuthenticationError>>()
//        firebaseAuth.signInAnonymously().addOnCompleteListener {
//            it.result.user?.let { user ->
//                deferred.complete(Result.Success(FirebaseUser(user)))
//            } ?: deferred.complete(Result.Error(AuthenticationError.NULL_USER))
//        }.addOnFailureListener { error ->
//            when(error) {
//                else -> deferred.complete(Result.Error(AuthenticationError.GENERAL_ERROR))
//            }
//        }
//        deferred.await()
        suspendCancellableCoroutine { cont ->
            firebaseAuth.signInAnonymously().addOnCompleteListener {
                it.result.user?.let { user ->
                    cont.resume(Result.Success(FirebaseUser(user)))
                } ?: cont.resume(Result.Error(AuthenticationError.NULL_USER))
            }.addOnFailureListener { error ->
                when(error) {
                    else -> cont.resume(Result.Error(AuthenticationError.GENERAL_ERROR))
                }
            }
        }
    }

    override suspend fun logout(): Result<Unit, AuthenticationError> {
        firebaseAuth.signOut()
        return Result.Success(Unit)
    }
}