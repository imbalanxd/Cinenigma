package com.imbaland.movies.domain.repository

import com.imbaland.movies.domain.model.Movie

interface MoviesRepository {
    suspend fun getMovies(): List<Movie>
}