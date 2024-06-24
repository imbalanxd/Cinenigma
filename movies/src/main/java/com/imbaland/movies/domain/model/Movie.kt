package com.imbaland.movies.domain.model

import com.google.firebase.firestore.Exclude

data class Movie(
    val adult: Boolean = false,
    override val id: Int = 0,
    val original_language: String = "",
    @get:Exclude val poster_path: String? = "",
    @get:Exclude val title: String = "",
    override val name: String = title,
    override val popularity: Double = 0.0,
    val department: String = "",
    override val media_type: String = "movie",
    override val image: String = if(poster_path.isNullOrBlank()) "" else "https://image.tmdb.org/t/p/w500$poster_path"
): Media()
