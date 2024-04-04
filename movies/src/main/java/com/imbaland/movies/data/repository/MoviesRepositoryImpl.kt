package com.imbaland.movies.data.repository

import com.imbaland.movies.data.remote.MoviesRemoteService
import com.imbaland.movies.domain.model.Movie
import com.imbaland.movies.domain.model.MovieDetails
import com.imbaland.movies.domain.repository.MoviesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MoviesRepositoryImpl @Inject constructor(
    private val moviesRemoteService: MoviesRemoteService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
): MoviesRepository {
    override suspend fun getMovies(): List<Movie> = withContext(dispatcher) {
        moviesRemoteService.api.topRated().results
    }

    override suspend fun getMovieDetails(id: Int): MovieDetails = withContext(dispatcher)  {
        moviesRemoteService.api.details(id)
    }
}