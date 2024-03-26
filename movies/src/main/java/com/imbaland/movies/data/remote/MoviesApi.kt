package com.imbaland.movies.data.remote;

import com.imbaland.movies.domain.model.Movie
import com.imbaland.movies.domain.model.MovieDetails
import com.imbaland.movies.domain.model.SearchResult
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

public interface MoviesApi {
    @GET(value = "authentication")
    suspend fun authenticate(): Unit

    @GET(value = "movie/top_rated?language=en-US&page=3")
    suspend fun topRated(): SearchResult<Movie>

    @GET(value = "https://api.themoviedb.org/3/movie/{movie_id}")
    suspend fun details(@Path("movie_id") id: Int): MovieDetails
}