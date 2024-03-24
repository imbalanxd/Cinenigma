package com.imbaland.movies.data.repository

import com.imbaland.movies.data.remote.MoviesRemoteService
import com.imbaland.movies.domain.model.Movie
import com.imbaland.movies.domain.repository.MoviesRepository
import javax.inject.Inject

class MoviesRepositoryImpl @Inject constructor(
    private val moviesRemoteService: MoviesRemoteService
): MoviesRepository {
    override suspend fun getMovies(): List<Movie> {
        return moviesRemoteService.api.topRated().results
    }
}