package com.imbaland.movies.domain.model

abstract class Media(
    val media_type: String,
    open val id: Int,
    open val popularity: Double,
) {

}