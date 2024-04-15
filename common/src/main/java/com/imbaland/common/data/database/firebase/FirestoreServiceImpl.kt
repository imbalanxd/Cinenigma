package com.imbaland.common.data.database.firebase

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.imbaland.common.domain.Error
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
    val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    val db: FirebaseFirestore = Firebase.firestore
    suspend fun <T : Any> writeDocument(
        collection: String,
        document: String?,
        data: T,
        merge: Boolean = false
    ): Result<Unit, FirestoreError> {
        return when (val writeResult = writeData(collection, document, data, merge)) {
            is Result.Error -> {
                Result.Error(
                    when (writeResult.error) {
                        DatabaseError.GENERAL_ERROR -> FirestoreError.GeneralFirestoreError
                        DatabaseError.NOT_FOUND -> FirestoreError.NotFoundError
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
                    cont.resume(Result.Error(FirestoreError.NotFoundError))
                }
            }
                .addOnFailureListener { exception ->
                    cont.resume(Result.Error(FirestoreError.GeneralFirestoreError))
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
                        trySend(Result.Error(FirestoreError.EmptyFirestoreError))
                    } else {
                        try {
                            trySend(Result.Success(collection.toObject(T::class.java)))
                        } catch (e: Throwable) {
                            trySend(Result.Error(FirestoreError.GeneralFirestoreError))
                            close(null)
                        }
                    }
                }
                awaitClose { listener.remove() }
            }
        }

    suspend inline fun <reified T> watchCollection(collection: String, filters: Map<String, Any> = mapOf()): Flow<Result<List<T>, FirestoreError>> =
        withContext(dispatcher) {
            callbackFlow {
                val docRef = db.collection(collection).apply {
                    filters.forEach { filter ->
                        whereEqualTo(filter.key, filter.value)
                    }
                }
                val listener = docRef.addSnapshotListener { collection, _ ->
                    if (collection == null) {
                        trySend(Result.Error(FirestoreError.EmptyFirestoreError))
                    } else {
                        try {
                            trySend(Result.Success(collection.toObjects(T::class.java)))
                        } catch (e: Throwable) {
                            trySend(Result.Error(FirestoreError.GeneralFirestoreError))
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
                        cont.resume(Result.Error(FirestoreError.NotFoundError))
                    }
                }
                    .addOnFailureListener { exception ->
                        cont.resume(Result.Error(FirestoreError.GeneralFirestoreError))
                    }
            }
        }

    private suspend fun <T : Any> writeData(
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

    suspend fun updateValues(
        destination: String,
        name: String,
        params: List<String>,
        expectedValue: List<Any?>? = null,
        targetValue: List<Any>,
        throws: List<Error>? = null
    ): Result<Unit, Error> = withContext(dispatcher) {
        suspendCancellableCoroutine { cont ->
            data class FirestoreUpdateError(val error: Error) : Exception()

            val document = db.collection(destination).document(name)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(document)
                val currentValues = List(params.size) { i -> snapshot.get(params[i]) }
                for (i in params.indices) {
                    if (expectedValue?.get(i) == null || currentValues?.get(i) == expectedValue?.get(
                            i
                        )
                    ) {
                        targetValue?.get(i)
                            ?.let { target -> transaction.update(document, params[i], target) }
                    } else if (throws?.get(i) != null) {
                        throw FirestoreUpdateError(throws[i])
                    }
                }
            }.addOnSuccessListener {
                cont.resume(Result.Success(Unit))
            }.addOnFailureListener { updateValueException ->
                cont.resume(
                    Result.Error(
                        when (updateValueException) {
                            is FirestoreUpdateError -> {
                                updateValueException.error
                            }

                            else -> {
                                DatabaseError.GENERAL_ERROR
                            }
                        }
                    )
                )
            }
        }
    }
//
//    override suspend fun <T> readData(): Result<T, DatabaseError> {
//        TODO("Not yet implemented")
//    }
}
