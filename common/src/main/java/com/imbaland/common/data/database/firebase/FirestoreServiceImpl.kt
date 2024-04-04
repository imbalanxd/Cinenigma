package com.imbaland.common.data.database.firebase

import com.google.firebase.Firebase
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.imbaland.common.domain.Result
import com.imbaland.common.domain.database.DatabaseError
import com.imbaland.common.domain.database.FirestoreError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

abstract class FirestoreServiceImpl(val dispatcher: CoroutineDispatcher = Dispatchers.IO) {
     val db = Firebase.firestore
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

    suspend inline fun <reified T> readDocument(
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
