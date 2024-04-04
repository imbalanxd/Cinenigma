package com.imbaland.common.domain

typealias RootError = Error
sealed interface Result<out Data, out Error: RootError> {
    data class Success<out Data, out Error: RootError>(val data: Data): Result<Data, Error>
    data class Error<out Data, out Error: RootError>(val error: Error): Result<Data, Error>
}

interface Error