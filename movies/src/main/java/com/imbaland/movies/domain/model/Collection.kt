package com.imbaland.movies.domain.model

data class Collection(
    val adult: Boolean = false,
    val backdrop_path: String?,
    val id: Int,
    val name: String,
    val original_language: String?,
    val original_name: String?,
    val overview: String?,
    val poster_path: String?
)