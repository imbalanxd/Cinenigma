package com.imbaland.movies.domain.model

data class Movie(
    val adult: Boolean,
    val backdrop_path: String,
    val genre_ids: List<Int>,
    override val id: Int,
    val original_language: String,
    val original_title: String,
    val overview: String,
    override val popularity: Double,
    val poster_path: String,
    val release_date: String,
    val title: String,
    val video: Boolean,
    val vote_average: Double,
    val vote_count: Int,
    override val media_type: String = "movie",
    override val image: String = "https://image.tmdb.org/t/p/w500$poster_path"
): Media()