package com.imbaland.movies.domain.model

data class Actor(
    val adult: Boolean,
    val gender: Int,
    override val id: Int,
    val known_for: List<Movie>,
    val known_for_department: String,
    val name: String,
    val original_name: String,
    override val popularity: Double,
    val profile_path: String
): Media(id = id, popularity = popularity, media_type = "person")