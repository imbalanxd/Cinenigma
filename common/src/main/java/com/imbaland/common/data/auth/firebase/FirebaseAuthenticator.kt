package com.imbaland.common.data.auth.firebase

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.imbaland.common.domain.auth.AnonymousAuthenticator
import com.imbaland.common.domain.auth.AuthenticationError
import com.imbaland.common.domain.Result
import com.imbaland.common.tool.logDebug
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
) : AnonymousAuthenticator {
    private val firebaseAuth: FirebaseAuth = Firebase.auth
    override val isAuthenticated: Boolean
        get() = firebaseAuth.currentUser != null
    override val account: FirebaseUser?
        get() = firebaseAuth.currentUser?.let { FirebaseUser(it) }

    override suspend fun login(): Result<FirebaseUser, AuthenticationError> =
        withContext(dispatcher) {
            if (isAuthenticated) {
                val currentUser = account
                if (currentUser == null) {
                    logout()
                } else {
                    Result.Success(currentUser)
                }
            }
            suspendCancellableCoroutine { cont ->
                firebaseAuth.signInAnonymously().addOnCompleteListener {
                    it.result.user?.let { user ->
                        user.updateProfile(
                            UserProfileChangeRequest.Builder().setDisplayName("Imbaland").build()
                        )
                        cont.resume(Result.Success(FirebaseUser(user)))
                    } ?: cont.resume(Result.Error(AuthenticationError.NULL_USER))
                }.addOnFailureListener { error ->
                    when (error) {
                        else -> cont.resume(Result.Error(AuthenticationError.GENERAL_ERROR))
                    }
                }
            }
        }

    override suspend fun logout(): Result<Unit, AuthenticationError> {
        firebaseAuth.signOut()
        return Result.Success(Unit)
    }
    override suspend fun changeName(name: String): Result<Unit, AuthenticationError> =
        withContext(dispatcher) {
            firebaseAuth.currentUser?.let { user ->
                suspendCancellableCoroutine { cont ->
                    user.updateProfile(
                        UserProfileChangeRequest.Builder().setDisplayName(name).build()
                    ).addOnSuccessListener {
                        logDebug("Name change successful: $name")
                        cont.resume(Result.Success(Unit))
                    }.addOnFailureListener {
                        logDebug("Name change failed: $name")
                        cont.resume(Result.Error(AuthenticationError.NAME_CHANGE_FAILED))
                    }
                }
            } ?: Result.Error(AuthenticationError.NULL_USER)
        }
}