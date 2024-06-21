package com.imbaland.movies.domain.model

import com.google.firebase.firestore.Exclude

data class Movie(
    val adult: Boolean = false,
    val backdrop_path: String? = "",
    val genre_ids: List<Int> = listOf(),
    override val id: Int = 0,
    val original_language: String = "",
    val original_title: String = "",
    val overview: String = "",
    override val popularity: Double = 0.0,
    val poster_path: String? = "",
    val release_date: String? = "",
    val title: String = "",
    @Exclude override val name: String = title,
    val video: Boolean = false,
    val vote_average: Double = 0.0,
    val vote_count: Int = 0,
    val department: String = "",
    override val media_type: String = "movie",
    override val image: String = if(poster_path.isNullOrBlank()) "" else "https://image.tmdb.org/t/p/w500$poster_path"
): Media()
