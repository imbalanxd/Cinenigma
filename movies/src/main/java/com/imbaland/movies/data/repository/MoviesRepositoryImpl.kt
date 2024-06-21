package com.imbaland.movies.data.repository

import com.imbaland.common.domain.Result
import com.imbaland.common.domain.RootError
import com.imbaland.movies.data.remote.MoviesRemoteService
import com.imbaland.movies.domain.model.Movie
import com.imbaland.movies.domain.model.MovieDetails
import com.imbaland.movies.domain.model.Person
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
    }
    override suspend fun discover(page: Int): Result<List<Movie>, MovieError> = withContext(dispatcher) {
        val result = moviesRemoteService.api.discover(page)
        if(result.results.isEmpty()) {
            Result.Error(MovieError.NoMoviesFoundError)
        } else {
            Result.Success(result.results)
        }
    }

    override suspend fun search(query: String): Result<List<Movie>, MovieError> = withContext(dispatcher) {
        val result = moviesRemoteService.api.search(query)
        if(result.results.isEmpty()) {
            Result.Error(MovieError.NoMoviesFoundError)
        } else {
            Result.Success(result.results.filter { movie -> movie.original_language.contains("en") }.distinctBy { it.name })
        }
    }

    override suspend fun getMovieDetails(id: Int): Result<MovieDetails, MovieError> = withContext(dispatcher)  {
        val details = moviesRemoteService.api.details(id)
        val actingCredits = details.actors?.map { actor ->
            val personDetails = getPersonDetails(actor.id)
            if(personDetails is Result.Success) {
                actor.copy(filmography = personDetails.data.movie_credits.actingCredits)
            } else {
                actor
            }
        }?:listOf()
        Result.Success(details.copy(actors = actingCredits))
    }

    override suspend fun getPersonDetails(id: Int): Result<Person, MovieError> = withContext(dispatcher)  {
        val details = moviesRemoteService.api.personDetails(id)
        Result.Success(details)
    }
}

sealed class MovieError: RootError {
    data object GeneralMovieError: MovieError()
    data object NoMoviesFoundError: MovieError()
}