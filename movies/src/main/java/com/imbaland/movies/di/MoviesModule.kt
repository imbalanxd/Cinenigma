package com.imbaland.movies.di

import android.app.Application
import android.content.Context
import com.imbaland.movies.data.remote.CinenigmaFirestore
import com.imbaland.movies.data.remote.MoviesRemoteService
import com.imbaland.movies.data.repository.MoviesRepositoryImpl
import com.imbaland.movies.domain.repository.MoviesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MoviesModule() {
    @Provides
    internal fun bindsMoviesRepository(
        moviesRemoteService: MoviesRemoteService,
    ): MoviesRepository {
        return MoviesRepositoryImpl(moviesRemoteService)
    }
    @Provides
    internal fun bindsCinenigmaFirestore(
    ): CinenigmaFirestore {
        return CinenigmaFirestore()
    }
    @Singleton
    @Provides
    internal fun provideMovieRemoteService(
        @ApplicationContext appContext: Context
    ): MoviesRemoteService {
        return MoviesRemoteService()
            .apply {
                launchService(appContext)
        }
    }
}
