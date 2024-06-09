package com.imbaland.movies.domain.model

/**
 * Class containing only the additional details of a movie
 * This is a seperate class that can be used to add to the screen, rather than replacing the previous lite movie class
 * and causing a full screen recomp... in theory?
 */
data class MovieDetails(
    val adult: Boolean = false,
    val backdrop_path: String? = "",
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
    override val image: String = "https://image.tmdb.org/t/p/w500$poster_path",
    val belongs_to_collection: Collection? = null,
    val budget: Int = 0,
    val genres: List<Genre> = listOf(),
    val homepage: String = "",
    val imdb_id: String = "",
    val production_companies: List<ProductionCompany> = listOf(),
    val production_countries: List<ProductionCountry> = listOf(),
    val revenue: Int = 0,
    val runtime: Int = 0,
    val spoken_languages: List<SpokenLanguage> = listOf(),
    val status: String = "",
    val tagline: String = "",
): Media()