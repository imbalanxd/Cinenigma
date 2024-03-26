package com.imbaland.movies.domain.model

/**
 * Class containing only the additional details of a movie
 * This is a seperate class that can be used to add to the screen, rather than replacing the previous lite movie class
 * and causing a full screen recomp... in theory?
 */
data class MovieDetails(
    val belongs_to_collection: Collection?,
    val budget: Int,
    val genres: List<Genre>,
    val homepage: String,
    val imdb_id: String,
    val production_companies: List<ProductionCompany>,
    val production_countries: List<ProductionCountry>,
    val revenue: Int,
    val runtime: Int,
    val spoken_languages: List<SpokenLanguage>,
    val status: String,
    val tagline: String,
)