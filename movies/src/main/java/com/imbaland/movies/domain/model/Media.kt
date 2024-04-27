package com.imbaland.movies.domain.model

abstract class Media(
) {
    abstract val media_type: String
    abstract val image: String
    abstract val id: Int
    abstract val popularity: Double
}