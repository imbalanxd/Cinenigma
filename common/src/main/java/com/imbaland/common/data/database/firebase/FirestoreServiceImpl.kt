package com.imbaland.common.data.database.firebase

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.imbaland.common.domain.Result
import com.imbaland.common.domain.database.DatabaseError
import com.imbaland.common.domain.database.FirestoreError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

abstract class FirestoreServiceImpl(
    val dispatcher: CoroutineDispatcher = Dispatchers.IO) {

    val db: FirebaseFirestore = Firebase.firestore
    suspend fun <T: Any>writeDocument(
        collection: String,
        document: String?,
        data: T,
        merge: Boolean = false
    ): Result<Unit, FirestoreError> {
        return when (val writeResult = writeData(collection, document, data, merge)) {
            is Result.Error -> {
                Result.Error(
                    when (writeResult.error) {
                        DatabaseError.GENERAL_ERROR -> FirestoreError.GENERAL_ERROR
                        DatabaseError.NOT_FOUND -> FirestoreError.NOT_FOUND
                    }
                )
            }
            is Result.Success -> {
                Result.Success(writeResult.data)
            }
        }
    }

    protected suspend inline fun <reified T> readDocument(
        collection: String,
        document: String
    ): Result<T, FirestoreError> = withContext(dispatcher) {
        suspendCancellableCoroutine { cont ->
            val docRef = db.collection(collection).document(document)
            docRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    cont.resume(Result.Success(document.toObject(T::class.java)!!))
                } else {
                    cont.resume(Result.Error(FirestoreError.NOT_FOUND))
                }
            }
                .addOnFailureListener { exception ->
                    cont.resume(Result.Error(FirestoreError.GENERAL_ERROR))
                }
        }
    }
    suspend inline fun <reified T> watchDocument(
        collection: String,
        document: String
    ): Flow<Result<T?, FirestoreError>> =
        withContext(dispatcher) {
            callbackFlow {
                val docRef = db.collection(collection).document(document)
                val listener = docRef.addSnapshotListener { collection, _ ->
                    if (collection == null) {
                        trySend(Result.Error(FirestoreError.EMPTY))
                    }
                    else {
                        try {
                            trySend(Result.Success(collection.toObject(T::class.java)))
                        } catch (e: Throwable) {
                            trySend(Result.Error(FirestoreError.GENERAL_ERROR))
                            close(null)
                        }
                    }
                }
                awaitClose { listener.remove() }
            }
        }

    suspend inline fun <reified T> watchCollection(collection: String): Flow<Result<List<T>, FirestoreError>> =
        withContext(dispatcher) {
            callbackFlow {
                val docRef = db.collection(collection)
                val listener = docRef.addSnapshotListener { collection, _ ->
                    if (collection == null) {
                        trySend(Result.Error(FirestoreError.EMPTY))
                    }
                    else {
                        try {
                            trySend(Result.Success(collection.toObjects(T::class.java)))
                        } catch (e: Throwable) {
                            trySend(Result.Error(FirestoreError.GENERAL_ERROR))
                            close(null)
                        }
                    }
                }
                awaitClose { listener.remove() }
            }
        }


    suspend inline fun <reified T> readCollection(collection: String): Result<List<T>, FirestoreError> =
        withContext(dispatcher) {
            suspendCancellableCoroutine { cont ->
                val docRef = db.collection(collection)
                docRef.get().addOnSuccessListener { collection ->
                    if (collection != null) {
                        cont.resume(Result.Success(collection.toObjects(T::class.java)))
                    } else {
                        cont.resume(Result.Error(FirestoreError.NOT_FOUND))
                    }
                }
                    .addOnFailureListener { exception ->
                        cont.resume(Result.Error(FirestoreError.GENERAL_ERROR))
                    }
            }
        }

    private suspend fun <T: Any> writeData(
        destination: String,
        name: String?,
        data: T,
        merge: Boolean
    ): Result<Unit, DatabaseError> = withContext(dispatcher) {
        suspendCancellableCoroutine { cont ->
            db.collection(destination)
                .run {
                    if (name != null) {
                        document(name)
                            .run {
                                if (merge) {
                                    set(data, SetOptions.merge())
                                } else {
                                    set(data)
                                }
                            }
                    } else {
                        add(data)
                    }
                }.addOnSuccessListener {
                    cont.resume(Result.Success(Unit))
                }.addOnFailureListener {
                    cont.resume(Result.Error(DatabaseError.GENERAL_ERROR))
                }
        }
    }
//
//    override suspend fun <T> readData(): Result<T, DatabaseError> {
//        TODO("Not yet implemented")
//    }
}
