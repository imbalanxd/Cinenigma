package com.imbaland.movies.domain.repository

import com.imbaland.movies.domain.model.Movie
import com.imbaland.movies.domain.model.MovieDetails

interface MoviesRepository {
    suspend fun getMovies(): List<Movie>
    suspend fun getMovieDetails(id: Int): MovieDetails
}