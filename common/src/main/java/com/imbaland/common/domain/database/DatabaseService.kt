package com.imbaland.common.domain.database

import com.imbaland.common.domain.Error
import com.imbaland.common.domain.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

abstract class DatabaseService(protected val dispatcher: CoroutineDispatcher = Dispatchers.IO) {
    protected abstract suspend fun writeData(destination: String, name: String?, data:Map<String, Any>, merge: Boolean = false): Result<Unit, DatabaseError>
    protected abstract suspend fun <T>readData():Result<T, DatabaseError>
}

enum class DatabaseError: Error {
    GENERAL_ERROR,
    NOT_FOUND
}