package com.imbaland.movies.data.remote

import android.content.Context
import com.imbaland.common.data.remote.RemoteService
import com.imbaland.movies.BuildConfig
import retrofit2.Retrofit

class MoviesRemoteService: RemoteService<MoviesApi>() {
    override val baseUrl: String
        get() = "https://api.themoviedb.org/3/"
    val api: MoviesApi
        get() = apiService

    override fun createApiService(retrofit: Retrofit): MoviesApi {
        return retrofit.create(MoviesApi::class.java)
    }

    override fun configureHeaders(context: Context) {
        super.configureHeaders(context)
        addHeader("Authorization", "Bearer ${BuildConfig.TMDB_KEY}")
    }
}