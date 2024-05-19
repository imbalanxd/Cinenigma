package com.imbaland.common.data.database.firebase

import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.imbaland.common.domain.Error
import com.imbaland.common.domain.Result
import com.imbaland.common.domain.database.DatabaseError
import com.imbaland.common.domain.database.FirestoreError
import com.imbaland.common.tool.logDebug
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
    suspend inline fun <reified T : Any> writeDocument(
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
                val listener = docRef.addSnapshotListener { result, _ ->
                    if (result == null) {
                        logDebug("Firestore document stream ($collection/$document) couldn't start")
                        trySend(Result.Error(FirestoreError.EmptyFirestoreError))
                    } else {
                        try {
                            val obj = result.toObject(T::class.java)
                            trySend(Result.Success(obj))
                            logDebug("Firestore document stream ($collection/$document) received\n--${obj.toString().replace(",","\n\t")}")
                        } catch (e: Throwable) {
                            trySend(Result.Error(FirestoreError.GeneralFirestoreError))
                            logDebug("Firestore document stream ($collection/$document) received error\n--$e\n--Closing")
                            close(null)
                        }
                    }
                }
                awaitClose {
                    logDebug("Firestore document stream ($collection/$document) closed")
                    listener.remove()
                }
            }
        }

    suspend inline fun <reified T> watchCollection(collection: String,
                                                   filters: Map<String, Any> = mapOf(),
                                                   greaterFilter: Map<String, Any> = mapOf(),
                                                   exclude: Pair<String, Any?>? = null): Flow<Result<List<T>, FirestoreError>> =
        withContext(dispatcher) {
            callbackFlow {
                var docRef = db.collection(collection).limit(20)
                filters.forEach { filter ->
                    docRef = docRef.whereEqualTo(filter.key, filter.value)
                }
                filters.forEach { filter ->
                    docRef = docRef.whereGreaterThan(filter.key, filter.value)
                }
                docRef = exclude?.let { docRef.whereNotEqualTo(it.first, it.second) }?:docRef
                val listener = docRef.addSnapshotListener { result, _ ->
                    if (result == null) {
                        logDebug("Firestore collection stream ($collection) couldn't start")
                        trySend(Result.Error(FirestoreError.EmptyFirestoreError))
                    } else {
                        try {
                            logDebug("Firestore collection stream ($collection) received" +
                                    "\n-- Type ${T::class.java}, Size ${result.size()}\n-- Filters ${filters}")
                            trySend(Result.Success(result.toObjects(T::class.java)))
                        } catch (e: Throwable) {
                            logDebug("Firestore collection stream ($collection) received error\n--$e\n--Closing")
                            trySend(Result.Error(FirestoreError.GeneralFirestoreError))
                            close(null)
                        }
                    }
                }
                awaitClose {
                    logDebug("Firestore collection stream ($collection) closed")
                    listener.remove()
                }
            }
        }


    suspend inline fun <reified T> readCollection(collection: String): Result<List<T>, FirestoreError> =
        withContext(dispatcher) {
            suspendCancellableCoroutine { cont ->
                val docRef = db.collection(collection)
                docRef.get().addOnSuccessListener { result ->
                    if (result != null) {
                        logDebug("Firestore collection ($collection) returned \n" +
                                "-- Type ${T::class.java}, Size ${result.size()}")
                        cont.resume(Result.Success(result.toObjects(T::class.java)))
                    } else {
                        logDebug("Firestore collection ($collection) returned error: not found")
                        cont.resume(Result.Error(FirestoreError.NotFoundError))
                    }
                }
                    .addOnFailureListener { exception ->
                        logDebug("Firestore collection ($collection) returned error: \n--${exception}")
                        cont.resume(Result.Error(FirestoreError.GeneralFirestoreError))
                    }
            }
        }

    suspend inline fun <reified T : Any> writeData(
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
                    logDebug("Firestore document ($destination) written\n--${T::class.java}\n--${data}")
                    cont.resume(Result.Success(Unit))
                }.addOnFailureListener {
                    logDebug("Firestore document ($destination) write failed\n--${T::class.java}\n--${it}")
                    cont.resume(Result.Error(DatabaseError.GENERAL_ERROR))
                }
        }
    }

    suspend fun deleteData(
        destination: String,
        name: String,
    ): Result<Unit, DatabaseError> = withContext(dispatcher) {
        suspendCancellableCoroutine { cont ->
            db.collection(destination).document(name)
                .delete()
                .addOnSuccessListener {
                    logDebug("Firestore document ($destination) deleted\n")
                    cont.resume(Result.Success(Unit))
                }.addOnFailureListener {
                    logDebug("Firestore document ($destination) delete failed\n")
                    cont.resume(Result.Error(DatabaseError.GENERAL_ERROR))
                }
        }
    }

    suspend inline fun <reified T : Any> addListValue(
        destination: String,
        name: String,
        child: String,
        data: T): Result<Unit, DatabaseError> = withContext(dispatcher) {
        suspendCancellableCoroutine { cont ->
            val document = db.collection(destination).document(name)
            document.update(child, FieldValue.arrayUnion(listOf(data))).addOnSuccessListener {
                logDebug("Firestore document ($destination$name) appended \n--${T::class.java}\n--${data}")
                cont.resume(Result.Success(Unit))
            }.addOnFailureListener { updateValueException ->
                logDebug("Firestore document ($destination$name) updated failed\n--${updateValueException}")
                cont.resume(Result.Error(DatabaseError.GENERAL_ERROR))
            }
        }
    }
    suspend inline fun <reified T : Any> addListValue(
        destination: String,
        name: String,
        child: String,
        data: List<T>) {
        val document = db.collection(destination).document(name)
        document.update(child, FieldValue.arrayUnion(data))
    }

    suspend fun updateValues(
        destination: String,
        name: String,
        params: List<String>,
        expectedValue: List<Any?>? = null,
        targetValue: List<Any?>,
        throws: List<Error>? = null
    ): Result<Unit, Error> = withContext(dispatcher) {
        suspendCancellableCoroutine { cont ->
            data class FirestoreUpdateError(val error: Error) : Exception()
            val document = db.collection(destination).document(name)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(document)
                val currentValues = List(params.size) { i -> snapshot.get(params[i]) }
                for (i in params.indices) {
                    if (expectedValue?.get(i) == null || currentValues?.get(i) == expectedValue?.get(i)) {
                        targetValue?.get(i).let { target -> transaction.update(document, params[i], target?:FieldValue.delete()) }
                    } else if (throws?.get(i) != null) {
                        throw FirestoreUpdateError(throws[i])
                    }
                }
            }.addOnSuccessListener {
                logDebug("Firestore document ($destination$name) updated\n--${targetValue}")
                cont.resume(Result.Success(Unit))
            }.addOnFailureListener { updateValueException ->
                logDebug("Firestore document ($destination$name) updated failed\n--${updateValueException}")
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
}
