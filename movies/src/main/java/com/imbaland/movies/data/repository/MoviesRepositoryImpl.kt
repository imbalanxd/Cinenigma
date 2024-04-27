package com.imbaland.movies.data.repository

import com.imbaland.common.domain.Result
import com.imbaland.common.domain.RootError
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
    override suspend fun getMovies(page: Int): Result<List<Movie>, MovieError> = withContext(dispatcher) {
        val result = moviesRemoteService.api.topRated(page)
        if(result.results.isEmpty()) {
            Result.Error(MovieError.NoMoviesFoundError)
        } else {
            Result.Success(result.results)
        }
//        when(val result = moviesRemoteService.api.topRated()) {
//            is Result.Error -> {
//                Result.Error(MovieError.GeneralMovieError)
//            }
//            is Result.Success -> {
//                if(result.data.results.isEmpty()) {
//                    Result.Error(MovieError.NoMoviesFoundError)
//                } else {
//                    Result.Success(result.data.results)
//                }
//            }
//        }
    }
    override suspend fun discover(page: Int): Result<List<Movie>, MovieError> = withContext(dispatcher) {
        val result = moviesRemoteService.api.discover(page)
        if(result.results.isEmpty()) {
            Result.Error(MovieError.NoMoviesFoundError)
        } else {
            Result.Success(result.results)
        }
    }

    override suspend fun getMovieDetails(id: Int): Result<MovieDetails, MovieError> = withContext(dispatcher)  {
        Result.Success(moviesRemoteService.api.details(id))
//        when(val result = moviesRemoteService.api.details(id)) {
//            is Result.Error -> {
//                Result.Error(MovieError.GeneralMovieError)
//            }
//            is Result.Success -> {
//                Result.Success(result.data)
//            }
//        }
    }
}

sealed class MovieError: RootError {
    data object GeneralMovieError: MovieError()
    data object NoMoviesFoundError: MovieError()
}