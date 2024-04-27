package com.imbaland.movies.domain.model

data class Movie(
    val adult: Boolean = false,
    val backdrop_path: String = "",
    val genre_ids: List<Int> = listOf(),
    override val id: Int = 0,
    val original_language: String = "",
    val original_title: String = "",
    val overview: String = "",
    override val popularity: Double = 0.0,
    val poster_path: String = "",
    val release_date: String = "",
    val title: String = "",
    val video: Boolean = false,
    val vote_average: Double = 0.0,
    val vote_count: Int = 0,
    override val media_type: String = "movie",
    override val image: String = "https://image.tmdb.org/t/p/w500$poster_path"
): Media()
