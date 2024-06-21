package com.imbaland.movies.data.remote;

import com.imbaland.common.domain.Result
import com.imbaland.movies.domain.model.Movie
import com.imbaland.movies.domain.model.MovieDetails
import com.imbaland.movies.domain.model.Person
import com.imbaland.movies.domain.model.SearchResult
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

public interface MoviesApi {
    @GET(value = "authentication")
    suspend fun authenticate(): Unit
    @GET(value = "discover/movie")
    suspend fun discover(
        @Query("page") page: Int,
        @Query("sort_by") sortBy: String = "vote_count.desc",
        @Query("primary_release_date.gte") releaseDateMin: String = "1980-09-12",
        @Query("with_original_language") originalLanguage: String = "en",
        @Query("include_video") includeVideo: Boolean = false,
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("language") language: String = "en-US"): SearchResult<Movie>

    @GET(value = "search/movie")
    suspend fun search(
        @Query("query") query: String,
        @Query("language") language: String = "en-US"): SearchResult<Movie>
    @GET(value = "movie/top_rated")
    suspend fun topRated(@Query("page") page: Int,@Query("language") language: String = "en-US"): SearchResult<Movie>

    @GET(value = "https://api.themoviedb.org/3/movie/{movie_id}?append_to_response=credits")
    suspend fun details(@Path("movie_id") id: Int): MovieDetails
    @GET(value = "https://api.themoviedb.org/3/person/{person_id}?append_to_response=movie_credits")
    suspend fun personDetails(@Path("person_id") id: Int): Person

}