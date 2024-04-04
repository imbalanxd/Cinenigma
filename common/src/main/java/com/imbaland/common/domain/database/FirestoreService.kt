package com.imbaland.common.domain.database

import com.imbaland.common.domain.Error
import com.imbaland.common.domain.Result

interface FirestoreService {
    suspend fun writeDocument(collection: String, document: String? = null, data: HashMap<String, Any>, merge: Boolean = false): Result<Unit, FirestoreError>
    suspend fun <T>readDocument(collection: String, document: String): Result<T, FirestoreError>
    suspend fun <T>readCollection(collection: String): Result<T, FirestoreError>
}

enum class FirestoreError: Error {
    GENERAL_ERROR,
    NOT_FOUND
}