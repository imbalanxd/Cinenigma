package com.imbaland.movies.di

import android.content.Context
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
    internal fun providesMoviesRepository(
        moviesRemoteService: MoviesRemoteService,
    ): MoviesRepository {
        return MoviesRepositoryImpl(moviesRemoteService)
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
