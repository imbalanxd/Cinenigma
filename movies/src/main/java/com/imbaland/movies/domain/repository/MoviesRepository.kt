package com.imbaland.movies.domain.repository

import com.imbaland.common.domain.Result
import com.imbaland.movies.data.repository.MovieError
import com.imbaland.movies.domain.model.Movie
import com.imbaland.movies.domain.model.MovieDetails
import com.imbaland.movies.domain.model.Person

interface MoviesRepository {
    suspend fun getMovies(page: Int): Result<List<Movie>, MovieError>
    suspend fun discover(page: Int = 1): Result<List<Movie>, MovieError>
    suspend fun discoverRandom(count: Int = 1, range: Int = 100): Result<List<Movie>, MovieError>
    suspend fun search(query: String): Result<List<Movie>, MovieError>
    suspend fun getMovieDetails(id: Int): Result<MovieDetails, MovieError>
    suspend fun getPersonDetails(id: Int): Result<Person, MovieError>
}